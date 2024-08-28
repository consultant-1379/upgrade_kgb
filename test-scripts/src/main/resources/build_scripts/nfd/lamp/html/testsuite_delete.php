<?php

# Delete  test suite
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

function getTSDirPath(&$db, $ts_id) {
	global $cloned_repo_sourcedir;
	if ( ! isset( $ts_id ) ) {
		return '*unknown*'; 
	}
        return "$cloned_repo_sourcedir/suites/$ts_id";
}

function getTSSpecPath($tsdir, $ts_id) {
	if ( ! isset( $ts_id ) ) {
		return '*unknown*'; 
	}
	return $tsdir . "/" . $ts_id . "_spec.bsh";
}

function getTSScriptPath($tsdir, $ts_id) {
	if ( ! isset( $ts_id ) ) {
		return '*unknown*'; 
	}
	return $tsdir . "/" . $ts_id . "_callbacks.lib";
}

#
# Function to delete the list of selected TCs for a TS
#
function deleteTSTestcases(&$dbi, $ts_id) {
	if ( ! isset( $ts_id ) ) {
		return FALSE;
	}
	$sql = "DELETE FROM tstcmapping WHERE tsid=$ts_id";
	if ( ! ($result = $dbi->doQuery($sql)) ) {
		return FALSE;
	} 
	return TRUE;
}


$db = &new cDatabase;
$dbi = &new cDatabasei;
$mysqli=$dbi->getLink();
$cloned_repo_path="/var/lib/jenkins/cominf_build/workspace/cominf_test";
$cloned_repo_sourcedir=$cloned_repo_path . "/SOURCES";
$tc_script_template=$cloned_repo_sourcedir . "/harness/etc/test_callbacks.lib.template";
$git="/usr/bin/git";
$gitlog="/var/log/gitlog";
$transaction_error=0;
$transaction_errmsg='';


include "header.php";

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness</a>&gt;<a href=\"usecases.php\">Usecases</a>&gt;<a href=\"testsuites.php\">Test suites&gt;</a>Delete test suite\n";

if ( ! isset($_GET['ts_id']) ) {
	FatalError(105, 'test suite id not found', __file__, __line__);
} else {
	$ts_id=$_GET['ts_id'];
}
	
# delete the record
$tspath = getTSDirPath( &$db, $ts_id );
$specfilepath = getTSSpecPath( $tspath, $ts_id );
$tssfilepath = getTSScriptPath( $tspath, $ts_id );
mysqli_autocommit($mysqli, FALSE);
$sql="DELETE FROM testsuite WHERE id=" . $ts_id;
if (  ! ($result = $dbi->doQuery($sql)) ) {
	$transaction_error = 1;
	$transaction_errmsg="Failed to delete TS record ";
} else {
	if ( ! deleteTSTestcases(&$dbi, $ts_id) ) {
		$transaction_error = 1;
		$transaction_errmsg="Failed to delete TS records from TSTC mapping table ";
	} else {

		# delete the TC in GIT
		if ( file_exists( $tspath ) ) {
			$cmd="cd $cloned_repo_path; $git pull >> $gitlog  2>&1";
			system($cmd, $retcode);
			if ( $retcode != 0 ) {
				$transaction_error=1;
				$transaction_errmsg.="Failed to git pull in workspace";
			} else {
				$cmd="cd $cloned_repo_path; $git rm -r $tspath >> $gitlog  2>&1";
				system($cmd, $retcode);
				if ( $retcode != 0 ) {
					$transaction_error=1;
					$transaction_errmsg .= "Failed to mark directory $tspath for GIT deletion";
				} else {
					$cmd = "cd $cloned_repo_path; $git commit -m \"deleted TS with ID=$ts_id\" >> $gitlog 2>&1";
					system($cmd, $retcode);
					if ( $retcode != 0 ) {
						$transaction_error=1;
						$transaction_errmsg.="Failed to commit TS deleted files";
					} else {
						$cmd="cd $cloned_repo_path; $git push >> $gitlog 2>&1";
						system($cmd, $retcode);
						if ( $retcode != 0 ) {
							$transaction_error=1;
							$transaction_errmsg.="Failed to git push in workspace";
						}
					}
				}
			}
		} else {
			$transaction_error=1;
			$transaction_errmsg .= "<p>Warning - unable to find TS dir $tspath. GIT delete not done.";
		}
	}
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
	echo "<p><strong>Test suite ID=$ts_id deleted successfully</strong>\n";

} else {
	echo "<p><strong>Failed to delete test suite. More info:</strong>\n";
	echo "<p><strong>$transaction_errmsg</strong>\n";
}
  
include "footer.php";
?>
