<?php

# Delete  testcase
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

function getUsecaseIDFromTestcase( &$db, $tc_id ) {
	if ( $tc_id == 0 ) {
		return FALSE;
	}
	$sql = "SELECT usecase.id AS uc_id FROM usecase, testcase WHERE testcase.id=$tc_id AND testcase.uc_id=usecase.id"; 
	if (!($result = &$db->doQuery($sql)) || mysql_num_rows($result) == 0 ) {
		FatalError(101, "Failed to get usecase id for tc=$tc_id", __file__, __line__);
	}
	$row = mysql_fetch_object($result);
	return $row->uc_id;


}

# function to return usecase name from given uc id
function getUCInfo(&$db, $uc_id) {
	# get usecase name
	$sql = "SELECT usecase.name AS uc_name, usecase.id AS uc_id FROM usecase WHERE usecase.id=" . $uc_id; 
	if (!($result = &$db->doQuery($sql)) || mysql_num_rows($result) == 0 ) {
		FatalError(101, "Failed to get usecase name for uc id=$uc_id", __file__, __line__);
	}
	$row = mysql_fetch_object($result);
	return array("uc_id" => $row->uc_id, "uc_name" => $row->uc_name);
}

function getFunctionalAreaFromUsecase(&$db, $uc_id) {
	if ( $uc_id == 0 ) {
		return "*unknown*";
	}
	$sql = "SELECT funcarea.name AS fa_name FROM funcarea, usecase WHERE usecase.id=$uc_id AND funcarea.id=usecase.fa_id"; 
	if (!($result = &$db->doQuery($sql)) || mysql_num_rows($result) == 0 ) {
		FatalError(101, "Failed to get functional area for uc=$uc_id", __file__, __line__);
	}
	$row = mysql_fetch_object($result);
	return $row->fa_name;

}

function getTCDirPath(&$db, $uc_id, $tc_id) {
	global $cloned_repo_sourcedir;
	$uc_info = array();
	if ( ! isset( $uc_id) || ! isset( $tc_id ) ) {
		return '*unknown*'; 
	}

	$uc_info = getUCInfo(&$db, $uc_id);
	$uc_name = $uc_info['uc_name'];
	$fa_name = getFunctionalAreaFromUsecase(&$db, $uc_id);

	$tcpath="$cloned_repo_sourcedir/FA/$fa_name/$uc_name/$tc_id";
        return $tcpath;
}

function getTCSpecPath($tcpath, $tc_id) {
	if ( ! isset( $tcpath ) || ! isset( $tc_id ) || $tcpath == '' ) {
		return '*unknown*';
	}
	return $tcpath . "/" . $tc_id . "_spec.bsh";
}

function getTCPath($tcpath, $tc_id) {
	if ( ! isset( $tcpath ) || ! isset( $tc_id ) || $tcpath == '' ) {
		return '*unknown*';
	}
	return $tcpath . "/" . $tc_id . "_callbacks.lib";
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

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness</a>&gt;<a href=\"usecases.php\">Usecases</a>&gt;<a href=\"testcases.php\">Testcases&gt;</a>Delete testcase\n";

if ( ! isset($_GET['tc_id']) ) {
	FatalError(105, 'testcase id not found', __file__, __line__);
} else {
	$tc_id=$_GET['tc_id'];
}
	

# check that there are not testcases associated with the usecase
$sql="SELECT testsuite.id as ts_id, testsuite.name FROM tstcmapping, testsuite WHERE tstcmapping.tcid=$tc_id AND testsuite.id=tstcmapping.tsid GROUP BY testsuite.name";
$result = &$db->doQuery($sql);
if( mysql_num_rows($result) > 0) {
	echo "<p><strong>Unable to delete testcases since the following test suites are associated with it:</strong>";
	echo "<ul>\n";
	while ( ($row =  mysql_fetch_object($result)) ) {
		$ts_id=$row->ts_id;
		$ts_name=$row->name;
		echo "<li><a href=testsuite.php?mode=edit&ts_id=" . $ts_id . ">" . $ts_name . "</a>\n";
	
	}
	echo "</ul>\n";
} else {
	
	# delete the record
	$uc_id = getUsecaseIDFromTestcase( &$db, $tc_id );
	$tcpath = getTCDirPath( &$db, $uc_id, $tc_id );
	$specfilepath = getTCSpecPath( $tcpath, $tc_id );
	$tcfilepath = getTCPath( $tcpath, $tc_id );
	mysqli_autocommit($mysqli, FALSE);
	$sql="DELETE FROM testcase WHERE id=" . $tc_id;
	if (  ! ($result = $dbi->doQuery($sql)) ) {
		$transaction_error = 1;
		$transaction_errmsg="Failed to delete TC record ";
	} else {

		# delete the TC in GIT
		if ( file_exists( $tcpath ) ) {
			$cmd="cd $cloned_repo_path; $git pull >> $gitlog  2>&1";
			system($cmd, $retcode);
			if ( $retcode != 0 ) {
				$transaction_error=1;
				$transaction_errmsg.="Failed to git pull in workspace";
			} else {
				$cmd="cd $cloned_repo_path; $git rm -r $tcpath >> $gitlog  2>&1";
				system($cmd, $retcode);
				if ( $retcode != 0 ) {
					$transaction_error=1;
					$transaction_errmsg .= "Failed to mark directory $tcpath for GIT deletion";
				} else {
					$cmd = "cd $cloned_repo_path; $git commit -m \"deleted TC with ID=$tc_id\" >> $gitlog 2>&1";
					system($cmd, $retcode);
					if ( $retcode != 0 ) {
						$transaction_error=1;
						$transaction_errmsg.="Failed to commit TC deleted files";
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
			$transaction_errmsg .= "<p>Warning - unable to find TC dir $tcpath. GIT delete not done.";
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
		echo "<p><strong>Testcase ID=$tc_id deleted successfully</strong>\n";

	} else {
		echo "<p><strong>Failed to delete testcase. More info:</strong>\n";
		echo "<p><strong>$transaction_errmsg</strong>\n";
	}

}
  
include "footer.php";
?>
