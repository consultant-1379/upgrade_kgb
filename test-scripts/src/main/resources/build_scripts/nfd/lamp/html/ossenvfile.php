<?php
#
# script to display and process env file form
#
#	Dec 2012 eeidle
include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;

$submitbtntxt="Save";
$firstname_err="";
$lastname_err="";
$signum_err="";
$transaction_error=0;


$file_id_def=0;
$firstname_def="";
$lastname_def="";
$signum_def="";
?>
<script language="Javascript">
<!--


function OnSelectingDirectory()
{
	if ( document.envfileform.file_name.value == "" ) {
		alert("Please select an envfile");
		return false;
	}
	document.envfileform.action = "ossenvfile.php?mode=view&file_name=/var/lib/jenkins/cominf_build/build_scripts/nfd/etc/" +  document.envfileform.file_name.value;
	//document.write(document.envfileform.file_name);
	document.envfileform.submit();             // Submit the page
	return true;
}
-->
</script>
<noscript>You need Javascript enabled for this to work</noscript>
<?php
if ( ! isset($_GET['mode']) ) 
	exit("Error: mode variable not set.  should be eg. view, new, edit or delete.<br> Todo, should really redirect back to calling page."); 
else
	$mode=$_GET['mode'];
	print "In $mode mode.<br>";
	if ( ! isset ($_POST['submitfile']) ) {
		# we are in view|edit mode and not submitting form
		if ( ! isset ($_GET['file_name']) ) 
			FatalError(105,'file name not found', __file__, __line__); 
		$file_name=$_GET['file_name'];
		if ( is_file($file_name) )
			print "File $file_name selected. <br>";
		else {	
			if ( is_dir($file_name) ) {
				$dir_name=$file_name;
				print "Directory $file_name selected. <br>";
					$_SESSION['dir_name'] = $dir_name;
					//opendir($dir_name);
					   header( "Location: http://atclvm243.athtem.eei.ericsson.se/ossenvfiles.php?dir_name=$dir_name" ) ;
					//exec ('/usr/local/bin/php -f /var/www/html/ossenvfiles.php?dir_name=$dir_name') ;
					return;
			}
			print " $file_name shouldn't be here.. <br>";
		}
	}
	if ( $mode == "view" ) {	
		$file = fopen($file_name, 'r'); // read current file
		while ($line = fgets($file)) {
			print "$line <br>"  ; 
	}
}

if ( $mode == "edit" ) {
	$file = fopen($file_name, 'rw');
	while ($line = fgets($file)) {
		parse_str($line);
		print "$line <br>";
	}
}
include "harness_footer.php"
?>



