<?php

# Delete  author
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
$dbi = &new cDatabasei;
$mysqli=$dbi->getLink();
$transaction_error=0;
$transaction_errmsg='';


include "header.php";

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness</a>&gt;<a href=\"authors.php\">Authors</a>&gt;</a>Delete author\n";

if ( ! isset($_GET['auth_id']) ) {
	FatalError(105, 'author id not found', __file__, __line__);
} else {
	$auth_id=$_GET['auth_id'];
}

$related_records=false;
$sql="SELECT id,slogan from testcase where auth_id=". $auth_id;
$result = &$db->doQuery($sql);
if( mysql_num_rows($result) > 0) {
	$related_records=true;
	echo "<p><strong>Unable to delete author since the following test cases are associated with him/her:</strong>";
	echo "<ul>\n";
	while ( ($row =  mysql_fetch_object($result)) ) {
		$tc_id=$row->id;
		$tc_slogan=$row->slogan;
		echo "<li><a href=testcase.php?mode=edit&tc_id=" . $tc_id . ">" . $tc_slogan . "</a>\n";
	
	}
	echo "</ul>\n";
}
	
$sql="SELECT id,name from usecase where auth_id=". $auth_id;
$result = &$db->doQuery($sql);
if( mysql_num_rows($result) > 0) {
	$related_records=true;
	echo "<p><strong>Unable to delete author since the following use cases are associated with him/her:</strong>";
	echo "<ul>\n";
	while ( ($row =  mysql_fetch_object($result)) ) {
		$uc_id=$row->id;
		$uc_name=$row->name;
		echo "<li><a href=usecase.php?mode=edit&uc_id=" . $uc_id . ">" . $uc_name . "</a>\n";
	
	}
	echo "</ul>\n";
}

if ( ! $related_records ) {
	
	# delete the record
	mysqli_autocommit($mysqli, FALSE);
	$sql="DELETE FROM author WHERE id=" . $auth_id;
	if (  ! ($result = $dbi->doQuery($sql)) ) {
		$transaction_error = 1;
		$transaction_errmsg="Failed to delete author record ";
	} 
	
	if ( $transaction_error == 0 ) {
		if (! mysqli_commit($mysqli) ) {
			$transaction_error=1;
			$transaction_errmsg.="failed to commit mysql transaction";
		} else {
			if ( ! mysqli_rollback($mysqli) ) {
				$transaction_errmsg .= "Unable to rollback mysql transacion";
			}
		}
	} 
	if ( $transaction_error == 0 ) {
		echo "<p><strong>Author ID=$auth_id deleted successfully</strong>\n";

	} else {
		echo "<p><strong>Failed to delete author. More info:</strong>\n";
		echo "<p><strong>$transaction_errmsg</strong>\n";
	}
}

  
include "footer.php";
?>
