<?php

# Delete  FA
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

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness</a>&gt;<a href=\"fas.php\">Functional Areas</a>&gt;</a>Delete functional area\n";

if ( ! isset($_GET['fa_id']) ) {
	FatalError(105, 'fa id not found', __file__, __line__);
} else {
	$fa_id=$_GET['fa_id'];
}

$related_records=false;
	
$sql="SELECT id,name from usecase where fa_id=". $fa_id;
$result = &$db->doQuery($sql);
if( mysql_num_rows($result) > 0) {
	$related_records=true;
	echo "<p><strong>Unable to delete functional area since the following use cases are associated with it:</strong>";
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
	$sql="DELETE FROM funcarea WHERE id=" . $fa_id;
	if (  ! ($result = $dbi->doQuery($sql)) ) {
		$transaction_error = 1;
		$transaction_errmsg="Failed to delete functional area record ";
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
		echo "<p><strong>Functional Area ID=$fa_id deleted successfully</strong>\n";

	} else {
		echo "<p><strong>Failed to delete functional area. More info:</strong>\n";
		echo "<p><strong>$transaction_errmsg</strong>\n";
	}
}

  
include "footer.php";
?>
