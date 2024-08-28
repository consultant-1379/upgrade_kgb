#!/usr/bin/perl -w

use strict;
use warnings;
use VMware::VIRuntime;
use VMware::VILib;

use Data::Dumper;

our $DEBUG = 0;

sub myFilter {
    my ($hash) = @_;
    my @keys = sort keys %{$hash};
    my @result = ();
    foreach my $key ( @keys ) {
	if ( $key ne "vim" && $key ne "declaredAlarmState" ) {
	    push @result, $key;
	}
    }
    return \@result;
}

sub getStatus($) {
        my ($taskRef) = @_;

        my $task_view = Vim::get_view(mo_ref => $taskRef);
        my $taskinfo = $task_view->info->state->val;
        my $continue = 1;
        while ($continue) {
                my $info = $task_view->info;
                if ($info->state->val eq 'success') {
                        $continue = 0;
                } elsif ($info->state->val eq 'error') {
                        my $soap_fault = SoapFault->new;
                        $soap_fault->name($info->error->fault);
                        $soap_fault->detail($info->error->fault);
                        $soap_fault->fault_string($info->error->localizedMessage);
                        die "$soap_fault\n";
                }
                sleep 1;
                $task_view->ViewBase::update_view_data();
        }
}

sub getFolder($$);
sub getFolder($$) {
    my ($folder_view, $r_folderParts) = @_;

    my $folderName = shift @{$r_folderParts};
    foreach my $child ( @{$folder_view->childEntity} ) {
	if ( $child->{'type'} eq 'Folder' ) {
	    my $child_view = Vim::get_view(mo_ref => $child);
	    if ( $child_view->{'name'} eq $folderName ) {
		if ( $#{$r_folderParts} == -1 ) {
		    return $child_view->{'mo_ref'};
		} else {
		    return getFolder($child_view,$r_folderParts);
		}
	    }
	}
    }

    return undef;
}

sub findParentType($$);

sub findParentType($$) {
    my ($view,$type) = @_;
    if ( $view->{'parent'} ) {
	$view = Vim::get_view(mo_ref => $view->{'parent'});
	if ( $view->isa($type) ) {
	    return $view;
	} else {
	    return findParentType($view,$type);
	}
    } else {
	return undef;
    } 
}

sub create($$$) {
    my ($vappName,$resPool,$folderRef ) = @_;

    if ( $DEBUG > 1 ) { print "create vappName=$vappName\n"; }

    my $dataCenter_view = findParentType($resPool,'Datacenter');
    if ( ! $dataCenter_view ) {
	print "ERROR: Failed to get Datacenter\n";
	Util::disconnect();
	exit 1;
    }
    
    my $sharesLevel = SharesLevel->new('normal');
    my $cpuShares = SharesInfo->new(shares => -1, level => $sharesLevel);
    my $memShares = SharesInfo->new(shares => -1, level => $sharesLevel);
    my $cpuAlloc =
	ResourceAllocationInfo->new(shares => $cpuShares,
				    reservation => 0,
				    expandableReservation => 1, 
				    limit => -1				    
	);
    my $memAlloc = 
	ResourceAllocationInfo->new(shares => $cpuShares,
				    reservation => 0,
				    expandableReservation => 1, 
				    limit => -1
	);
    my $resSpec = ResourceConfigSpec->new(cpuAllocation => $cpuAlloc,
					  memoryAllocation => $memAlloc);

    my $configSpec = VAppConfigSpec->new();

    my $vmFolder = $dataCenter_view->{vmFolder};
    if ( $folderRef ) {
	$vmFolder = $folderRef;
    }

    $resPool->CreateVApp(name => $vappName, 
			 resSpec => $resSpec, 
			 configSpec => VAppConfigSpec->new(), 
			 vmFolder => $vmFolder );
}

sub colocate($$) {
    my ($vappName, $vapp_view) = @_;

    if ( ! $vapp_view->vAppConfig->entityConfig ) {
	print "ERROR: Could not find an VMs in vApp $vappName\n";
	Util::disconnect();
	exit 2;
    }

    my $ruleName = "Co-locate_" . $vappName;
    
    my $ccr = Vim::get_view(mo_ref => $vapp_view->owner);
    my $cars;
    if ( $ccr->configuration->rule ) {
	foreach my $ruleInfo ( @{$ccr->configuration->rule} ) {
	    if ( $ruleInfo->name eq $ruleName ) {
		$cars = $ruleInfo;
	    }
	}
    }
    
    my $op;
    if ( $cars ) {
	$op = ArrayUpdateOperation->new('edit');
	$cars->{vm} = $vapp_view->vm;
    } else {
	$op = ArrayUpdateOperation->new('add');
	$cars = ClusterAffinityRuleSpec->new(vm => $vapp_view->vm, 
					     mandatory => 1,
					     name => $ruleName,
					     enabled => 1);   
    }

    my $crs = ClusterRuleSpec->new( operation => $op, info => $cars );
    my $ccse = ClusterConfigSpecEx->new( rulesSpec => [ $crs ] );
    
    
    eval{
	my $task_ref = $ccr->ReconfigureComputeResource_Task(spec => $ccse, modify => 1);
	&getStatus($task_ref);
    };
    if($@) {
	print "Error: " . $@ . "\n";
    }
}

sub addVM($$) {
    my ($vapp_view, $vm_view) = @_;

    eval{ $vapp_view->MoveIntoResourcePool(list => [ $vm_view ]); };
    if($@) {
	print "Error: " . $@ . "\n";
    }    
}

sub order($$$$$) { 
    my ($vapp_view, $vm_view, $order,$stopAction,$stopDelay) = @_;

    foreach my $ec ( @{$vapp_view->vAppConfig->entityConfig} ) {	
	if ( $ec->key->value eq $vm_view->{mo_ref}->value ) {
	    $ec->{'startOrder'} = $order;
	    $ec->{'stopAction'} = $stopAction;
	    $ec->{'stopDelay'} = $stopDelay;
	    $ec->{'waitingForGuest'} = 1;
	}
    }

    my $vAppConfigSpec = VAppConfigSpec->new();
    $vAppConfigSpec->{'entityConfig'} = $vapp_view->vAppConfig->entityConfig;

    $vapp_view->UpdateVAppConfig(spec => $vAppConfigSpec);
}

sub moveHost($$) {
    my ($vapp_view,$host_view) = @_;

    my $relocate_spec = VirtualMachineRelocateSpec->new(host => $host_view->{mo_ref});

    foreach my $vm_ref ( @{$vapp_view->vm} ) {
	my $vm_view = Vim::get_view(mo_ref => $vm_ref);
	print $vm_view->name . "....";
	
	eval{
	    my $task_ref = $vm_view->RelocateVM_Task( spec => $relocate_spec );
	    &getStatus($task_ref);
	};
	if($@) {
	    print "Error: " . $@ . "\n";
	} else {
	    print "Done\n";
	}
    }
}

sub moveDatastore($$) {
    my ($vapp_view,$ds_view) = @_;


    my $relocate_spec = VirtualMachineRelocateSpec->new(datastore => $ds_view);

    foreach my $vm_ref ( @{$vapp_view->vm} ) {
	my $vm_view = Vim::get_view(mo_ref => $vm_ref);
	print $vm_view->name . "....";
	
	eval{
	    my $task_ref = $vm_view->RelocateVM_Task( spec => $relocate_spec );
	    &getStatus($task_ref);
	};
	if($@) {
	    print "Error: " . $@ . "\n";
	    exit 1;
	} else {
	    print "Done\n";
	}
    }
}

sub regVM($$$) {
    my ($vapp_view,$vmxFile,$vmName) = @_;

    my $vmFolder = Vim::get_view(mo_ref => $vapp_view->parentFolder);

    my $task_ref;
    eval {
	$task_ref = $vmFolder->RegisterVM_Task(name => $vmName, path => $vmxFile, asTemplate => 0, pool => $vapp_view->{parent});
	&getStatus($task_ref);
    };
    if($@) {
	print "Error: " . $@ . "\n";
    } else {	
	my $task_view = Vim::get_view(mo_ref => $task_ref);
	my $vm_view = Vim::get_view(mo_ref => $task_view->{info}->{result});
	eval{ $vapp_view->MoveIntoResourcePool(list => [ $vm_view ]); };
	if($@) {
	    print "Error: " . $@ . "\n";
	}    
    }
}

sub renameVMs($$) {
    my ($vapp_view,$replace) = @_;
 
    my $vappName = $vapp_view->{name};

    foreach my $vm_ref ( @{$vapp_view->vm} ) {
	my $vm_view = Vim::get_view(mo_ref => $vm_ref);
	my $newName = $vm_view->name;
	$newName =~ s/^$replace/$vappName/;

	my $vmChangespec = VirtualMachineConfigSpec->new(name => $newName );
	eval{
	    print "Renaming ", $vm_view->name, " to ", $newName, "\n";
	    my $task_ref = $vm_view->ReconfigVM_Task(spec => $vmChangespec);
	    &getStatus($task_ref);
	};
	if($@) {
	    print "Error: " . $@ . "\n";
	}
    }
}

sub setNameVar($) {
    my ($vapp_view) = @_;

    my $vappName = $vapp_view->{name};
    foreach my $vm_ref ( @{$vapp_view->vm} ) {
	my $vm_view = Vim::get_view(mo_ref => $vm_ref);
	#print $vm_view->{name} . "....";

	my $vappNameVal = OptionValue->new(key => "machine.id", value => $vappName . ":" . $vm_view->{name});

	my $spec = VirtualMachineConfigSpec->new(extraConfig => [$vappNameVal]);

	eval{
	    my $task_ref = $vm_view->ReconfigVM_Task(spec => $spec);
	    &getStatus($task_ref);
	};
	if($@) {
	    print "Error: " . $@ . "\n";
	    Util::disconnect();
	    exit 1;
	} 
    }    
}

sub mkSnapshotVM($$$) {
    my ($vm_view, $snapname, $snapdesc) = @_;

    my $result_ref;
    foreach my $snap ( @{$vm_view->{'snapshot'}->{'rootSnapshotList'}} ) {
	if ( $snap->{'name'} eq $snapname ) {
	    $result_ref = $snap->{'snapshot'};
	}
    }

    if ( ! $result_ref ) {
	    my $task_ref;
	    eval{
		$task_ref = $vm_view->CreateSnapshot_Task(name => $snapname, 
							  description => $snapdesc,
							  memory => 0, 
							  quiesce => 0);
		
		&getStatus($task_ref);
	    };
	    if($@) {
		print "Error: " . $@ . "\n";
		Util::disconnect();
		exit 1;
	    } 
	    my $task_view = Vim::get_view(mo_ref => $task_ref);
	    $result_ref = $task_view->{'info'}->{'result'};
    }

    return $result_ref;
}

sub lClone($$) {
    my ($vapp_view, $cloneName) = @_;
    if ( $DEBUG > 1 ) { print "lClone src $vapp_view->{'name'} dest $cloneName\n"; }
    if ( $DEBUG > 9 ) { print Dumper($vapp_view); }
   
    my $respool_view = Vim::get_view( mo_ref => $vapp_view->{parent});
    create($cloneName, $respool_view, $vapp_view->{'parentFolder'});
    my $lcvapp_view = Vim::find_entity_view(view_type => 'VirtualApp', filter => {"name" => $cloneName});

    my $vappName = $vapp_view->{name};

    foreach my $vm_ref ( @{$vapp_view->vm} ) {
	my $vm_view = Vim::get_view(mo_ref => $vm_ref);
	#print Dumper($vm_view->config->hardware->device);
	my $lcvm_name = $vm_view->{name};
	$lcvm_name =~ s/^$vappName/$cloneName/;
	print "\t$lcvm_name ....";

	my $lcSnapRef = mkSnapshotVM($vm_view, "linkedclone", "Snapshot for linkedclone");

	my $relocSpec = VirtualMachineRelocateSpec->new( diskMoveType => "createNewChildDiskBacking" );
	my $cloneSpec = VirtualMachineCloneSpec->new( powerOn => 0, template => 0, location => $relocSpec, snapshot => $lcSnapRef );

	my $task_ref;
	eval{
	    print "\tClone";

	    $task_ref = 
		$vm_view->CloneVM_Task(folder => $vapp_view->{parentFolder},
				       name => $lcvm_name,
				       spec => $cloneSpec,
		);
	    &getStatus($task_ref);
	};
	if($@) {
	    print "Error: " . $@ . "\n";
	    Util::disconnect();
	    exit 1;
	} 
	my $task_view = Vim::get_view(mo_ref => $task_ref);
	my $lcvm_view = $task_view->{'info'}->{'result'};
	addVM($lcvapp_view,$lcvm_view);

	print "\tDone\n";
    }
}

sub mkSnapshot($$) {
    my ($vapp_view,$snapname) = @_;

    foreach my $vm_ref ( @{$vapp_view->vm} ) {
	my $vm_view = Vim::get_view(mo_ref => $vm_ref);
	mkSnapshotVM($vm_view,$snapname,"");
    }
}

sub revertSnapshot($$) { 
    my ($vapp_view,$snapname) = @_;

    foreach my $vm_ref ( @{$vapp_view->vm} ) {
	my $vm_view = Vim::get_view(mo_ref => $vm_ref);
	print "$vm_view->{'name'}....";
	my $snap_ref;
	foreach my $snap ( @{$vm_view->{'snapshot'}->{'rootSnapshotList'}} ) {
	    if ( $snap->{'name'} eq $snapname ) {
		$snap_ref = $snap->{'snapshot'};
	    }
	}
	if ( ! $snap_ref ) {
	    print "ERROR: Could not find snapshot $snapname\n";
	    Util::disconnect();
	    exit 1;
	} 
	my $snap_view = Vim::get_view(mo_ref => $snap_ref);
	my $task_ref;
	eval{
	    $task_ref = $snap_view->RevertToSnapshot_Task();
	    &getStatus($task_ref);
	};
	if($@) {
	    print "Error: " . $@ . "\n";
	    Util::disconnect();
	    exit 1;
	} 
	print "Done\n";
    }
}    

sub rmSnapshot($$) {
    my ($vapp_view,$snapname) = @_;

    foreach my $vm_ref ( @{$vapp_view->vm} ) {
	my $vm_view = Vim::get_view(mo_ref => $vm_ref);
	print "$vm_view->{'name'}....";
	my $snap_ref;
	foreach my $snap ( @{$vm_view->{'snapshot'}->{'rootSnapshotList'}} ) {
	    if ( $snap->{'name'} eq $snapname ) {
		$snap_ref = $snap->{'snapshot'};
	    }
	}
	if ( ! $snap_ref ) {
	    print "ERROR: Could not find snapshot $snapname\n";
	    Util::disconnect();
	    exit 1;
	} 
	my $snap_view = Vim::get_view(mo_ref => $snap_ref);
	my $task_ref;
	eval{
	    $task_ref = $snap_view->RemoveSnapshot_Task(removeChildren => 'true');
	    &getStatus($task_ref);
	};
	if($@) {
	    print "Error: " . $@ . "\n";
	    Util::disconnect();
	    exit 1;
	} 
	print "Done\n";
    }
}    

sub listVM($) {
    my ($vapp_view) = @_;

    foreach my $vm_ref ( @{$vapp_view->vm} ) {
	my $vm_view = Vim::get_view(mo_ref => $vm_ref);
	print "$vm_view->{'name'}\n";
	my $snap_ref;
    }
}

sub powerOp($$) {
    my ($vapp_view,$powerOp) = @_;

    my $task_ref;
    if ( $powerOp == 1 ) {
	eval{ $task_ref = $vapp_view->PowerOnVApp_Task(); };
	if($@) {
	    print "Error: " . $@ . "\n";
	    Util::disconnect();
	    exit 1;
	} 
    }

    eval{ &getStatus($task_ref); };
    if($@) {
	print "Error: " . $@ . "\n";
	Util::disconnect();
	exit 1;
    } 
}

sub powerOpVM($$) {
    my ($vm_view,$powerOp) = @_;

    my $task_ref;
    if ( $powerOp == 0 ) {
        eval{ $task_ref = $vm_view->PowerOffVM_Task(); };
        if($@) {
            print "Error: " . $@ . "\n";
            Util::disconnect();
            exit 1;
        }
    }
    if ( $powerOp == 1 ) {
        eval{ $task_ref = $vm_view->PowerOnVM_Task(); };
        if($@) {
            print "Error: " . $@ . "\n";
            Util::disconnect();
            exit 1;
        }
    }
    if ( $powerOp == 2 ) {
        eval{ $task_ref = $vm_view->ResetVM_Task(); };
        if($@) {
            print "Error: " . $@ . "\n";
            Util::disconnect();
            exit 1;
        }
    }


    eval{ &getStatus($task_ref); };
    if($@) {
        print "Error: " . $@ . "\n";
        Util::disconnect();
        exit 1;
    }
}


sub main() {
    $Data::Dumper::Sortkeys = \&myFilter;

    my @opNames = ( "coloc", "addvm", "order", "moveds", "movehost", 
		    "register", "rename", "setnamevar","create", "lclone",
		    "rmsnap", "mksnap", "revertsnap", "listvm", "poweron", "poweroffvm", "poweronvm", "resetvm" );
    my %opMap = ();
    foreach my $opName ( @opNames ) {
	$opMap{$opName} = 1;
    }
    my @sortedOps = sort keys %opMap;

    my %opts = 
	(
	 'vapp' => 
	 {
	     type => "=s",
	     help => "The name of the vApp to apply operation",
	     required => 0,
	 },
	 'op' => 
	 {
	     type => "=s",
	     help => "The operation to perform: " . join(",", @sortedOps),
	     required => 1,
	 },
	 'vm' => 
	 {
	     type => "=s",
	     help => "The VM",
	     required => 0,
	 },
	 'startorder' => {
	     type => "=s",
	     help => "The startOrder",
	     required => 0,
	 },
	 'stopdelay' => {
	     type => "=s",
	     help => "The allowed stop time",
	     required => 0,
	 },
	 'datastore' => {
	     type => "=s",
	     help => "The datastore",
	     required => 0,
	 },
	 'cluster' => {
	     type => "=s",
	     help => "The cluster name",
	     required => 0,
	 },
	 'host' => {
	     type => "=s",
	     help => "The hostname name",
	     required => 0,
	 },
	 'replace' => {
	     type => "=s",
	     help => "The string to replace in the VM name",
	     required => 0,
	 },
	 'vmx' => {
	     type => "=s",
	     help => "The path to the vmx file",
	     required => 0,
	 },	 
	 'clonevapp' => {
	     type => "=s",
	     help => "The name of the cloned vApp",
	     required => 0,
	 },	 
	 'folder' => {
	     type => "=s",
	     help => "The folder where to create the vApp",
	     required => 0,
	 },	 
	 'snap' => {
	     type => "=s",
	     help => "The name of the snapshot",
	     required => 0,
	 },	 
	 'debug' => {
	     type => "=s",
	     help => "debug level",
	     required => 0,
	 },	 	 
	);
    
    # validate options, and connect to the server
    Opts::add_options(%opts);

    # validate options, and connect to the server
    Opts::parse();
    Opts::validate();
    Util::connect();

    my $opName = Opts::get_option ('op');
    if ( ! exists $opMap{$opName} ) {
	print "ERROR: $opName is not a valid operation\n";
	print "Know operations are " . join(",", @sortedOps) . "\n";
	exit 1;
    }

    my $vappName = Opts::get_option ('vapp');
    my $vapp_view;

    my $vmName = Opts::get_option ('vm');
    my $vm_view;
    if ( $vmName && $opName ne "register") {
	$vm_view = Vim::find_entity_view(view_type => 'VirtualMachine', 
					 filter =>{ 'name' => $vmName});
	if ( ! $vm_view ) {
	    print "Unable to locate $vmName!\n";
	    Util::disconnect();
	    exit 1;
	}
    }

    my $dsName = Opts::get_option("datastore");
    my $ds_view ;
    if ( $dsName ) {
	$ds_view = Vim::find_entity_view(view_type => 'Datastore', 
				    filter =>{ 'name' => $dsName});
	if ( ! $ds_view ) {
	    print "Unable to locate $dsName!\n";
	    Util::disconnect();
	    exit 1;
	}
    }
    
    my $clusterName = Opts::get_option("cluster");
    my $cluster_view;
    if ( $clusterName ) {
	$cluster_view 
	    = Vim::find_entity_view(view_type => 'ClusterComputeResource', 
				    filter =>{ 'name' => $clusterName});
	if ( ! $cluster_view ) {
	    print "Unable to locate $clusterName!\n";
	    Util::disconnect();
	    exit 1;
	}
    }

    my $hostName = Opts::get_option("host");
    my $host_view;
    if ( $hostName ) {
	$host_view 
	    = Vim::find_entity_view(view_type => 'HostSystem', 
				    filter =>{ 'name' => $hostName});
	if ( ! $host_view ) {
	    print "Unable to locate $hostName!\n";
	    Util::disconnect();
	    exit 1;
	}
    }

    my $debugLevel = Opts::get_option ('debug');
    if ( $debugLevel ) {
	$DEBUG = $debugLevel;
    }

    if ( $opName eq "coloc" ) {
	colocate($vappName, $vapp_view);
    } elsif ( $opName eq "addvm" ) {
	addVM($vapp_view, $vm_view);
    } elsif ( $opName eq "order" ) {
	my $startOrder = Opts::get_option("startorder");
	my $stopDelay = Opts::get_option("stopdelay");
	if ( ! $stopDelay ) {
	    $stopDelay = 120;
	}
	order($vapp_view, $vm_view,$startOrder,"guestShutdown",$stopDelay);
    } elsif ( $opName eq "moveds" ) {
	moveDatastore($vapp_view, $ds_view);
    } elsif ( $opName eq "movehost" ) {
	moveHost($vapp_view, $host_view);
    } elsif ( $opName eq "register" ) {
	my $vmxFile = Opts::get_option("vmx");
	if ( defined $vmxFile ) {
	    regVM($vapp_view,$vmxFile,$vmName);
	} else {
	    print "ERROR: You need to specify the vmx file";
	    Util::disconnect();
	    exit 1;
	}	    
    } elsif ( $opName eq "rename" ) {
	my $replace = Opts::get_option("replace");
	if ( defined $replace ) {
	renameVMs($vapp_view,$replace);
	} else {
	    print "ERROR: You need to specified the replace string";
	    Util::disconnect();
	    exit 1;
	}	    
    } elsif ( $opName eq "setnamevar" ) {
	setNameVar($vapp_view);
    } elsif ( $opName eq "create" ) {
	if ( ! $cluster_view ) {
	    print "ERROR: cluster parameter is manditory\n";
	    Util::disconnect();
	    exit 1;
	}
	
	my $dataCenter_view = findParentType($cluster_view,'Datacenter');
	my $folder_ref = $dataCenter_view->{'vmFolder'};
	my $folderPath = Opts::get_option("folder");
	if ( $folderPath ) { 
	    my @folders = split(/\//, $folderPath);
	    $folder_ref = getFolder( Vim::get_view(mo_ref => $folder_ref), \@folders );       
	    if ( ! defined $folder_ref ) {
		print "ERROR: Cound not find $folderPath\n";
		exit 1;
	    }
	}
	my $resPool = Vim::get_view(mo_ref => $cluster_view->resourcePool);
	create($vappName,$resPool,$folder_ref);
    } elsif ( $opName eq "lclone" ) {
	my $lCloneName = Opts::get_option("clonevapp");
	lClone($vapp_view, $lCloneName );
    } elsif ( $opName eq "rmsnap" ) {
	my $snapname = Opts::get_option("snap");
	rmSnapshot($vapp_view, $snapname);
    } elsif ( $opName eq "mksnap" ) {
	my $snapname = Opts::get_option("snap");
	mkSnapshot($vapp_view, $snapname);
    } elsif ( $opName eq "revertsnap" ) {
	my $snapname = Opts::get_option("snap");
	revertSnapshot($vapp_view, $snapname);
    } elsif ( $opName eq "listvm" ) {
	listVM($vapp_view);
    } elsif ( $opName eq "poweron" ) {
	powerOp($vapp_view, 1);   
} elsif ( $opName eq "poweroffvm" ) {
        powerOpVM($vm_view, 0);
} elsif ( $opName eq "poweronvm" ) {
        powerOpVM($vm_view, 1);
} elsif ( $opName eq "resetvm" ) {
        powerOpVM($vm_view, 2);
    } else { 
	print "ERROR: unknown op $opName\n";
    }


    Util::disconnect();
}    

$| = 1;
main();
