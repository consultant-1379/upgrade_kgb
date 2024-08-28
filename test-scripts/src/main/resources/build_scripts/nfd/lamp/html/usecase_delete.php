<?php

# Delete  usecase
#
# edavmax May 2012
#
#
#
#
#
include_once "inc/cdialog.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;
include "header.php";


if ( ! isset($_GET['uc_id']) ) {
	FatalError(105, 'usecase id not found', __file__, __line__);
} else {
	$uc_id=$_GET['uc_id'];
}
	
echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness&gt;</a><a href=\"usecases.php\">Usecases&gt;</a>Delete usecase\n";

# check that there are not testcases associated with the usecase
$sql="SELECT * from testcase WHERE uc_id=". $uc_id;
$result = &$db->doQuery($sql);
if( mysql_num_rows($result) > 0) {
	echo "<p><strong>Unable to delete usecase since the following testcases are associated with it:</strong>";
	echo "<ul>\n";
	while ( ($row =  mysql_fetch_object($result)) ) {
		$tc_id=$row->id;
		$tc_name=$row->slogan;
		echo "<li><a href=testcase.php?mode=edit&tc_id=" . $tc_id . ">" . $tc_name . "</a>\n";
	
	}
	echo "</ul>\n";
} else {
	# delete the record
	$sql="DELETE FROM usecase WHERE id=" . $uc_id;
	if ( ($result = mysql_query($sql)) ) {
		echo "<p>Usecase deleted successfully";
	} else {
		FatalError(101, 'Failed to delete usecase', __file__, __line__);
	}

}
	
  
include "footer.php";
?>
