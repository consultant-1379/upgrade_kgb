#!/usr/bin/perl -w
use strict;
use warnings;
use VMware::VIRuntime;
use VMware::VILib;

Opts::parse();
Opts::validate();
Util::connect();

my $vm_view = Vim::find_entity_views(view_type => 'VirtualMachine');
my $op = $ARGV[0];

foreach(@$vm_view) {
	my $vm_name = $_->summary->config->name;
	my $devices =$_->config->hardware->device;
	my $mac_string;
	foreach(@$devices) {
		if($_->isa("VirtualEthernetCard")) {
			$mac_string .= "\t[" . $_->deviceInfo->label . "] : " . $_->macAddress . "\n";
		}
	}
        if ( $op eq $vm_name )
	{
		print $vm_name . "\n" . $mac_string . "\n";
	}
}

Util::disconnect();
