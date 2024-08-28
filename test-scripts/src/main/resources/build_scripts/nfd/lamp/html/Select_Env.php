<html> <head> <title>OSS Environment Configuration</title></head>
<body>
<?php
	$ENV_FILE_PATH="/var/lib/jenkins/cominf_build/build_scripts/nfd/etc/";
	$ENV_FILE_NAME=$ENV_FILE_PATH . $_POST['ENV_FILE_NAME'];
	$G_REMOTE_TEST_HARNESS_DIR="/var/tmp/platform/taf";
	$CI_ADMIN1_IPV4_ADDRESS = $_POST['CI_ADMIN1_IPV4_ADDRESS'];
	$CI_ADMIN2_IPV4_ADDRESS = $_POST['CI_ADMIN2_IPV4_ADDRESS'];
	$CI_ADMIN1_HOSTNAME = $_POST['CI_ADMIN1_HOSTNAME'];
	$CI_ADMIN2_HOSTNAME = $_POST['CI_ADMIN2_HOSTNAME'];
	$CI_ADMIN1_ROOTPW = $_POST['CI_ADMIN1_ROOTPW'];
	$CI_ADMIN2_ROOTPW = $_POST['CI_ADMIN2_ROOTPW'];
	$CI_ADMIN1_ILO_NAME = $_POST['CI_ADMIN1_ILO_NAME'];
	$CI_ADMIN2_ILO_NAME = $_POST['CI_ADMIN2_ILO_NAME'];
	$CI_ADMIN1_ILO_ROOTPW = $_POST['CI_ADMIN1_ILO_ROOTPW'];
	$CI_ADMIN2_ILO_ROOTPW = $_POST['CI_ADMIN2_ILO_ROOTPW'];
	$CI_RELEASE = $_POST['CI_RELEASE'];
	$CI_SHIPMENT = $_POST['CI_SHIPMENT'];
	$CI_TYPE = $_POST['CI_TYPE'];
	$CI_PLATFORM = $_POST['CI_PLATFORM'];
	$CI_ENVIRONMENT = $_POST['CI_ENVIRONMENT'];	
	$CI_BUILD_TYPE = $_POST['CI_BUILD_TYPE'];
	$CI_SHIPMENT = $_POST['CI_SHIPMENT'];	
	$CI_SHIPMENT = $_POST['CI_SHIPMENT'];
	$CI_SHIPMENT = $_POST['CI_SHIPMENT'];	
	$G_HARNESS_HOST = $_POST['harnesshost'];
	print "ENV_FILE_NAME:=$ENV_FILE_NAME<br>";	
	if (file_exists($ENV_FILE_NAME)) {
		exit ("ERROR: File already exists: ($ENV_FILE_NAME)"); 
	}

	$harnessparam = $_POST['harnessparam'];
	if ( $harnessparam != null )
	{
		switch($harnessparam)
		{
			case "testcases" :  $harnesshostarg="-i";break;
			case "testsuites" : $harnesshostarg="-s";break;
			case "usecases" : $harnesshostarg="-u";break;
		}
	}
	else { echo ("Invalid Entry for harnesshostarg."); }

	$testselection = $_POST['testselection'];
	if ( $testselection !=null )
	{
		$harnesshostarg = $harnesshostarg . " " . $testselection;
	}
	else  { exit ("Invalid Entry for testselection i.e. no $harnessparam specified.<br>");  }
	if ( $G_HARNESS_HOST != null )
	{
		switch($G_HARNESS_HOST)
		{
			case "targetserver" : 
				$harnesscommand="G_REMOTE_TEST_HARNESS_COMMAND=\"$G_REMOTE_TEST_HARNESS_DIR/harness/bin/atcominf.bsh $harnesshostarg -j \${G_BUILD_NUMBER} -S \"";
				break;
			case "localhost" :  
				$harnesscommand="G_LOCAL_TEST_HARNESS_COMMAND=\"/harness/bin/atcominf.bsh $harnesshostarg -j \${G_BUILD_NUMBER} -S\"";
				break;
		}
		echo ("Running harness on $G_HARNESS_HOST <br>");
	}
	else { echo ("Invalid Entry for G_HARNESS_HOST.<br>"); }
	if ( $CI_ADMIN1_IPV4_ADDRESS == null)
		{ echo ("Invalid Entry for CI_ADMIN1_IPV4_ADDRESS.<br>");  }
	if ( $CI_ADMIN2_IPV4_ADDRESS == null)
		{ echo ("Invalid Entry for CI_ADMIN2_IPV4_ADDRESS.<br>");  }


	$fh = fopen($ENV_FILE_NAME, 'w') or die("can't open file");
	$stringData = "#!/bin/bash\n";
	fwrite($fh, $stringData);
	fclose($fh);
	$fh = fopen($ENV_FILE_NAME, 'a') or die("can't open file");
	$stringData = "G_HARNESS_HOST=$G_HARNESS_HOST\n";
	fwrite($fh, $stringData);	
	$stringData = "$harnesscommand\n";
	fwrite($fh, $stringData);
	$stringData = "CI_ADMIN1_IPV4_ADDRESS=$CI_ADMIN1_IPV4_ADDRESS\n";
	fwrite($fh, $stringData);
	$stringData = "CI_ADMIN2_IPV4_ADDRESS=$CI_ADMIN2_IPV4_ADDRESS\n";
	fwrite($fh, $stringData);
	$stringData = "CI_ADMIN1_HOSTNAME=$CI_ADMIN1_HOSTNAME\n";
	fwrite($fh, $stringData);
	$stringData = "CI_ADMIN2_HOSTNAME=$CI_ADMIN2_HOSTNAME\n";
	fwrite($fh, $stringData);
	$stringData = "CI_ADMIN1_ROOTPW=$CI_ADMIN1_ROOTPW\n";
	fwrite($fh, $stringData);
	$stringData = "CI_ADMIN2_ROOTPW=$CI_ADMIN2_ROOTPW\n";
	fwrite($fh, $stringData);
	$stringData = "CI_ADMIN1_ILO_NAME=$CI_ADMIN1_ILO_NAME\n";
	fwrite($fh, $stringData);
	$stringData = "CI_ADMIN2_ILO_NAME=$CI_ADMIN2_ILO_NAME\n";
	fwrite($fh, $stringData);
	$stringData = "CI_ADMIN1_ILO_ROOTPW=$CI_ADMIN1_ILO_ROOTPW\n";	
	fwrite($fh, $stringData);
	$stringData = "CI_ADMIN2_ILO_ROOTPW=$CI_ADMIN2_ILO_ROOTPW\n";	
	fwrite($fh, $stringData);
	$stringData = "CI_RELEASE=$CI_RELEASE\n";	
	fwrite($fh, $stringData);
	$stringData = "CI_SHIPMENT=$CI_SHIPMENT\n";	
	fwrite($fh, $stringData);
	$stringData = "CI_TYPE=$CI_TYPE\n";	
	fwrite($fh, $stringData);
	$stringData = "CI_PLATFORM=$CI_PLATFORM\n";
	fwrite($fh, $stringData);
	$stringData = "CI_ENVIRONMENT=$CI_ENVIRONMENT\n";
	fwrite($fh, $stringData);
	$stringData = "CI_BUILD_TYPE=$CI_BUILD_TYPE\n";
	fwrite($fh, $stringData);
	$stringData = "CI_MWS_JUMP=YES\n";  				// always YES when calling master_wrapper from here
	fwrite($fh, $stringData);		
	$stringData = "CI_PRE_INI=YES\n"; 					// always YES when calling master_wrapper from here
	fwrite($fh, $stringData);		
	$stringData = "CI_DMR=NO\n"; 						// always NO when calling master_wrapper from here, Separate test case configures DMR
	fwrite($fh, $stringData);
	$stringData = "### Below variable names are now deprecated. Do not use them in test cases. ###\n";
	fwrite($fh, $stringData);
	$stringData = "CI_MACHINE=\${CI_ADMIN1_HOSTNAME}\n";	//refactor required to replace this variable
	fwrite($fh, $stringData);
	$stringData = "CI_SERVER_HOSTNAME[0]=\${CI_MACHINE}\n";
	fwrite($fh, $stringData);	
	$stringData = "CI_SERVER_IP[0]=${CI_ADMIN1_IPV4_ADDRESS}\n";
	fwrite($fh, $stringData);	
	$stringData = "CI_SERVER_INST_TYPE[0]=system\n";    //refactor required to replace this variable
	fwrite($fh, $stringData);	
	$stringData = "CI_SERVER_CONFIG[0]=system\n"; 		//refactor required to replace this variable
	fwrite($fh, $stringData);	
	$stringData = "CI_SERVER_ROOTPW[0]=shroot\n";		//refactor required to replace this variable
	fwrite($fh, $stringData);	
	$stringData = "\n";
	fwrite($fh, $stringData);	
	
	fclose($fh);
	$fh = fopen($ENV_FILE_NAME, 'r'); // read current file
		while ($line = fgets($fh)) {
			print "$line <br>"  ; 
	}
	fclose($fh);

?>
</body></html>