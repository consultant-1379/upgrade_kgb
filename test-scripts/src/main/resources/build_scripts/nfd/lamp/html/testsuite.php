<?php
#
# script to display and process testsuite form
#
#	Apr 2012 edavmax

include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;
$dbi = &new cDatabasei;
$mysqli=$dbi->getLink();

if ( ! isset($_GET['mode']) ) 
	$mode="new";
else
	$mode=$_GET['mode'];

$java_onloadfunc="onload=\"toggleFields('" . $mode . "')\"";


# function to return author info from given auth id
function getAuthInfo(&$db, $auth_id) {
	if ( $auth_id == 0 )
		return FALSE;
	$auth_info = array();
	# get usecase name
	$sql = "SELECT id, firstname, lastname, signum FROM author WHERE id=" . $auth_id; 
	if (!($result = &$db->doQuery($sql)) || mysql_num_rows($result) == 0 ) {
		FatalError(101, "Failed to get author details for auth id=$auth_id", __file__, __line__);
	}
	$row = mysql_fetch_object($result);
	return array("id" => $row->id, "fname" => $row->firstname, "lname" => $row->lastname, "signum" => $row->signum);
}

function generateTSSpecFile(&$db, $tmpspecfilepath, $ts_id, $auth_id, $name, $slogan, $exemode, $selected_tcs, $timeout) {
	if ( ! ($tmpfname = tempnam($tmpspecfilepath, "FOO") ) ) {
		return FALSE;
	}
	if ( ! ($fh = fopen($tmpfname, 'w') ) ) {
		return FALSE;
	}
	date_default_timezone_set('Europe/Dublin');
	$date=date('l jS \of F Y G:i:s');
	$slogan = addslashes($slogan);
	$name = addslashes($name);
	$auth_info = getAuthInfo(&$db, $auth_id);
	$auth_signum = $auth_info['signum'];
	$ts_tcidstr="(";
	foreach ($selected_tcs as $id => $tc_info) { 
		$ts_tcidstr .= "$id ";
	}
	$ts_tcidstr = trim( $ts_tcidstr, " ");
	$ts_tcidstr .= ")";
	if ( $exemode == "dependent" ) {
		$spec_independent = 0;
	} else {
		$spec_independent = 1;
	}

	$contents = <<<EOC
#!/bin/bash

# TEST SUITE SPEC FILE
# This file is generated/updated using the test framework web interface.
# Manual updates on test servers are lost when the test package is re-installed.
#
# Last modified: $date
#
SPEC_TS_NAME=$name
SPEC_TS_ID=$ts_id
SPEC_TS_SLOGAN="$slogan"
SPEC_TS_TIMEOUT=$timeout
SPEC_TS_AUTHOR="$auth_signum"
SPEC_TS_INDEPENDENT=$spec_independent
SPEC_TS_TC_IDS=$ts_tcidstr

EOC;
	fwrite($fh, $contents);
	fclose( $fh );
	return $tmpfname;

}

function getTSNameFromID( &$db, $ts_id ) {
	if ( ! isset( $ts_id ) ) {
		return '*unknown*'; 
	}
	$sql = "SELECT name FROM testsuite WHERE id=" . $ts_id; 
	if (!($result = &$db->doQuery($sql)) || mysql_num_rows($result) == 0 ) {
		FatalError(101, "Failed to get test suite name for ts id=$ts_id", __file__, __line__);
	}
	$row = mysql_fetch_object($result);
	return $row->name;

}

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
# Function to generate form objects for the TS selection
#
#

function createTCSelection(&$db, $available_testcases, $selected_testcases, $errortext='') {
	$html = "<table style=\"width: 100%\" border=0>\n";
	$html .= "<tr>\n";
	$html .= "<td class=\"formfield\" style=\"width: 48%\">* Available TCs\n";
	$html .= "</td>\n";
	$html .= "<td style=\"width: 30px\">\n";
	$html .= "</td>\n";
	$html .= "<td class=\"formfield\" style=\"width: 48%\">Selected TCs\n";
	$html .= "</td>\n";
	$html .= "</tr>\n";
	$html .= "<tr>\n";
	$html .= "<td>\n";
	$html .= "<p style=\"font-family: courier;font-size: 10pt; color: #999999;\">" . sprintf_nbsp("%-30s %6s %s", "Usecase", "TC ID", "TC slogan\n");  
	$html .= "<select name=\"available_tcs[]\" size=\"20\" multiple onChange=\"\" ondblclick=\"\" style=\"font-family: courier; width: 100%\">\n";
	foreach ($available_testcases as $id => $tc_info) { 
		$html .= "<option value=\"$id\">" . sprintf_nbsp("%-30s %06d %s", $tc_info[0], $id, $tc_info[1]) . "</option>\n";
	}	
	$html .= "</select>\n";
	$html .= "</td>\n";
	$html .= "<td align=center>\n";
	$html .= "<table>\n";
	$html .= "<tr>\n";
	$html .= "<td><input type=\"button\" name=\"move_a2s\" value=\"--&gt;\" onclick=\"moveOptions(document.testsuiteform.elements['available_tcs[]'], document.testsuiteform.elements['selected_tcs[]'])\"></td>\n";
	$html .= "</tr>\n";
	$html .= "<tr>\n";
	$html .= "<td><input type=\"button\" name=\"move_s2a\" value=\"&lt;--\" onclick=\"moveOptions(document.testsuiteform.elements['selected_tcs[]'], document.testsuiteform.elements['available_tcs[]'])\"></td>\n";
	$html .= "</tr>\n";
	$html .= "</table>\n";
	$html .= "</td>\n";
	$html .= "<td>\n";
	$html .= "<p style=\"font-family: courier;font-size: 10pt; color: #999999;\">" . sprintf_nbsp("%-30s %6s %s", "Usecase", "TC ID", "TC slogan\n");  
	$html .= "<select name=\"selected_tcs[]\" size=\"20\" multiple onChange=\"\" ondblclick=\"\" style=\"font-family: courier; width: 100%\">\n";
	foreach ($selected_testcases as $id => $tc_info) { 
		$html .= "<option value=\"$id\">" . sprintf_nbsp("%-30s %06d %s", $tc_info[0], $id, $tc_info[1]) . "</option>\n";
	}
	$html .= "</select>\n";
	$html .= "<p class=\"fielderror\">$errortext</p>\n";
	$html .= "</td>\n";
	$html .= "</tr>\n";
	$html .= "</table>\n";
	return $html;
}


#
# Function to get entire list of testcases
#
function getAllTestcases(&$db) {
	$all_tcs = array();

	$sql = "SELECT testcase.id, usecase.name AS uc_name, testcase.slogan AS tc_slogan FROM testcase, usecase WHERE testcase.uc_id = usecase.id ORDER by uc_name, testcase.id";
	$result = &$db->doQuery($sql);
	if (mysql_num_rows($result) > 0 ) {
		while ( ($row =  mysql_fetch_object($result)) ) {
			$all_tcs[$row->id] = array($row->uc_name, $row->tc_slogan);
		}
	}
	return $all_tcs;
}

#
# Function to get a list of the selected TCs
#
function getSelectedTestcases(&$db, $ts_id) {
	$sel_tcs = array();
	$sql = "SELECT tcid, usecase.name AS uc_name, testcase.slogan AS tc_slogan FROM tstcmapping, testcase, usecase WHERE testcase.id=tstcmapping.tcid AND testcase.uc_id=usecase.id AND tsid=$ts_id";
	$result = &$db->doQuery($sql);
	if (mysql_num_rows($result) > 0 ) {
		while ( ($row =  mysql_fetch_object($result)) ) {
			$sel_tcs[$row->tcid] = array($row->uc_name, $row->tc_slogan);
			
		}
	}
	return $sel_tcs;
}

#
# Function to get a list of the available TCs
#
function getAvailableTestcases(&$db, $sel_tcs) {
	$avail_tcs = array();
	$all_tcs = getAllTestcases( &$db );

	foreach ($all_tcs as $id => $tc_info) {
		if ( ! array_key_exists( $id, $sel_tcs ) ) {
			$avail_tcs[$id] = $tc_info;
		}
	}

	return $avail_tcs;
	
}

# function to take a list of tc ids ("tcid1" "tcid2" ... ) and return a list of tcs 
# in the form ( ("tcid1" ("uc name1" "tc slogan1") ) ("tcid2" ("uc_name1" "slogan2") ) ....
function getSelectedTSInfo(&$db, $sel_tcids ) {
	if ( ! isset ( $sel_tcids ) ) {
		return FALSE;
	}
	$sel_tcinfo=array();
	$sel_str="";
	foreach( $sel_tcids as $tc_id ) { 
		$sel_str .= "$tc_id,";
	}
	$sel_str = trim( $sel_str, "," );
	$sql = "SELECT testcase.id AS tcid, usecase.name AS uc_name, testcase.slogan AS tc_slogan FROM testcase, usecase WHERE usecase.id = testcase.uc_id AND testcase.id IN (" . $sel_str . ") ORDER BY  FIND_IN_SET(testcase.id, '" . $sel_str . "')" ;
	$result = &$db->doQuery($sql);
	if (mysql_num_rows($result) > 0 ) {
		while ( ($row =  mysql_fetch_object($result)) ) {
			$sel_tcinfo[$row->tcid] = array($row->uc_name, $row->tc_slogan);
			
		}
	}

	return $sel_tcinfo;
}

#
# Function to save the list of selected TCs for a TS
#
function saveSelectedTSTestcases(&$dbi, $ts_id, $selected_tcs) {
	if ( ! isset( $ts_id ) || count($selected_tcs) == 0 ) {
		return FALSE;
	}
	$sql = "INSERT INTO tstcmapping (tsid, tcid) VALUES ";
	foreach ($selected_tcs as $id => $tc_info) { 
		$sql .= "(" . $ts_id . " ," . $id . "), ";
	}
	$sql = trim($sql, ", ");
	if ( ! ($result = $dbi->doQuery($sql)) ) {
		return FALSE;
	} 
	return TRUE;
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

#
# Function to save the list of selected TCs
#
function sprintf_nbsp() {
	$args = func_get_args();
	return str_replace(' ', '&nbsp;', vsprintf(array_shift($args), array_values($args)));
}

$submitbtntxt="Save";
$name_err="";
$slogan_err="";
$exemode_err="";
$auth_id_err="";
$selected_tcs_err="";
$timeout_err='';

$auth_id_def=0;
$ts_id_def=0;
$slogan_def="";
$name_def="";
$specfilepath_def="*unknown*";
$tssfilepath_def="*unknown*";
$exemode_def="dependent";
$selected_tcs_def=array();
$harness_tgtdir="/opt/ericsson/cominf_test";
$timeout_def=300;


$all_tcs = getAllTestcases(&$db, 1);

$formerrors=0;
$gitmove=0;
$tmpspecfileloc="/tmp";
$transaction_error=0;
$transaction_errmsg="";
$cloned_repo_path="/var/lib/jenkins/cominf_build/workspace/cominf_test";
$cloned_repo_sourcedir=$cloned_repo_path . "/SOURCES";
$ts_script_template=$cloned_repo_sourcedir . "/harness/etc/test_suite_callbacks.lib.template";
$git="/usr/bin/git";
$gitlog="/var/log/gitlog";

# if in new/edit mode and GIT not installed 
# we have big problem
if ( ( $mode == "new" || $mode == "edit" ) && ! is_executable ($git) ) { 
	FatalError(105,'git is not installed or executable on server', __file__, __line__); 
}

if ( $mode == "new" ) {
	$specfilepath='*new*';
	$tssfilepath='*new*';
	if ( ! isset ($_POST['submittestsuite']) ) {
		$auth_id=$auth_id_def;
		$ts_id=$ts_id_def;
		$name=$name_def;
		$slogan=$slogan_def;
		$specfilepath=$specfilepath_def;
		$tssfilepath=$tssfilepath_def;
		$selected_tcs=$selected_tcs_def;
		$exemode=$exemode_def;
		$timeout=$timeout_def;
	}
	$selected_tcs=$selected_tcs_def;
	$available_tcs=$all_tcs;

} else {

	if ( ! isset ($_POST['submittestsuite']) ) {
		# we are in view|edit mode and not submitting form
		if ( ! isset ($_GET['ts_id']) ) 
			FatalError(105,'testsuite id not found', __file__, __line__); 
		$ts_id=$_GET['ts_id'];
		$sql = "SELECT id, auth_id, name, slogan, exemode, timeout FROM testsuite WHERE id=$ts_id";
		$result = &$db->doQuery($sql);
		if( mysql_num_rows($result) == 0)
			FatalError(105,'testcase record not found', __file__, __line__);
		$row = mysql_fetch_object($result);
		$ts_id=$row->id;
		$name=$row->name;
		$slogan=$row->slogan;
		$auth_id = $row->auth_id;
		$exemode = $row->exemode;
		$timeout = $row->timeout;

		# get TS paths
		if ( isset($ts_id) && $ts_id > 0 ) {  
			$tspath = getTSDirPath( &$db, $ts_id );
			$specfilepath = getTSSpecPath( $tspath, $ts_id );
			$tssfilepath = getTSScriptPath( $tspath, $ts_id );
		}
		$selected_tcs = getSelectedTestcases(&$db, $ts_id);
		$available_tcs = getAvailableTestcases( &$db, getSelectedTestcases( &$db, $ts_id ) );
	}
}

if ( isset ($_POST['submittestsuite']) &&  $_POST['submittestsuite'] == "$submitbtntxt" ) {
	# process the form
#print("<pre>");
#print_r($_POST);
#print("</pre>");
	if ( isset( $_GET['ts_id'] ) ) {
		$ts_id =$_GET['ts_id'];
	} else if ( isset($_POST['ts_id']) ) {
		$ts_id = $_POST['ts_id'];
	} else {
		$ts_id = $ts_id_def;
	}
	$auth_id = $_POST['auth_id'];
	$name =  $_POST['name'];
	$slogan = $_POST['slogan'];
	$exemode = $_POST['exemode'];
	$timeout = $_POST['timeout'];
	

	if ( $auth_id  == 0 ) {
		$formerrors++;
		$auth_id_err = "Please choose an author.";
	}

	if ( $timeout =='' ) {
		$formerrors++;
		$timeout_err="Please specify a timeout.";
	}

	if ( $name == '' ) {
		$formerrors++;
		$name_err = "Please enter a name.";
	} else if ( ! preg_match('/^[a-zA-Z0-9_]+$/', $name ) ) {
                $formerrors++;
                $name_err="Test suite name can only be composed of alphanumeric and _ chars ";
	} else if ( strlen( $name ) > 30 ) {
		$formerrors++;
		$name_err = "Test suite name cannot exceed 30 chars";
	}

	if ( $slogan == '' ) {
		$formerrors++;
		$slogan_err = "Please enter a slogan.";
	}


	if ( ! isset( $_POST['selected_tcs'] ) ) {
		$formerrors++;
		$selected_tcs_err = "Please select some test cases";
		$selected_tcs = $selected_tcs_def;
	} else {
		$selected_tcs = getSelectedTSInfo(&$db, $_POST['selected_tcs']);
	}
	$available_tcs = getAvailableTestcases( &$db, $selected_tcs );
	
	if ( $formerrors == 0 ) {
		mysqli_autocommit($mysqli, FALSE);
		if ( $mode == "new" ) {
			# create new testsuite record
			$sql = "INSERT INTO testsuite (auth_id, name, slogan, exemode, timeout) VALUES (" . $auth_id . ",\"" . addslashes($name) . "\", \"" . addslashes($slogan) . "\", \"" . $exemode . "\", " . $timeout . " )"; 
			if ( ! ($result = $dbi->doQuery($sql)) ) {
				$transaction_error=1;
				$transaction_errmsg .= "Unable to create new TS in database"; 
			} else {
				$ts_id=mysqli_insert_id($mysqli);
			}
			# update the ts/tc mapping table
		 	if ( ! saveSelectedTSTestcases( &$dbi, $ts_id, $selected_tcs ) ) {
				$transaction_error=1;
				$transaction_errmsg .= "Unable to save TS testcases";
			}
			
		} else {
			# update existing testcase record
			$sql = "UPDATE testsuite SET auth_id=" . $auth_id . ", name=\"" . addslashes($name)  . "\", slogan=\"" . addslashes($slogan) . "\", exemode=\"" . $exemode . "\", timeout=" . $timeout . " WHERE id=" . $ts_id;
			if (!($result = $dbi->doQuery($sql))) {
				$transaction_error=1;
				$transaction_errmsg="Unable to update TS record (TS ID=" . $ts_id . ")";
			} else { 
				# update the ts/tc mapping table
		 		if ( ! deleteTSTestcases( &$dbi, $ts_id) ) {
					$transaction_error=1;
					$transaction_errmsg .= "Unable to delete existing TS testcases";
				}
		 		if ( ! saveSelectedTSTestcases( &$dbi, $ts_id, $selected_tcs ) ) {
					$transaction_error=1;
					$transaction_errmsg .= "Unable to save TS testcases";
				}
			}

		}
	}
	# get TS paths
	if ( isset($ts_id) && $ts_id > 0 ) {  
		$tspath = getTSDirPath( &$db, $ts_id );
		$specfilepath = getTSSpecPath( $tspath, $ts_id );
		$tssfilepath = getTSScriptPath( $tspath, $ts_id );
	} else {
		$specfilepath = $specfilepath_def;
		$tssfilepath = $tssfilepath_def;
	}
	if ( $formerrors == 0 && $transaction_error == 0 ) {
		# generate the spec file in a temporary location
		if ( ! ( $tmp_specfile = generateTSSpecFile(&$db, $tmpspecfileloc, $ts_id, $auth_id, $name, $slogan, $exemode, $selected_tcs, $timeout ) ) ) {
			$transaction_error=1;
			$transaction_errmsg="Failed to create temp TS spec file";
		} else { 
			# update the file system and commit to git
			if ( $mode == "new" ) {
				# create directory structure for the TS
				if ( ! file_exists($tspath) ) {
					if ( ! mkdir($tspath, 0755, true) ) {
						$transaction_error=1;
						$transaction_errmsg="Failed to create TS dir $tspath";
					} else {
						# copy specfile to new dir
						if ( ! copy( $tmp_specfile, $specfilepath ) ) {
							$transaction_error=1;
							$transaction_errmsg="Failed to copy TS spec file to $tspath";
						} 
						# copy script template to new dir
						if ( ! copy( $ts_script_template, $tssfilepath ) ) {
							$transaction_error=1;
							$transaction_errmsg="Failed to copy TS script file to $tspath";
						} else {
							$cmd = "chmod +x $tssfilepath > /dev/null 2>&1";
							system($cmd, $retcode);
							if ($retcode != 0) {
								$transaction_error=1;
								$transaction_errmsg="Failed to set permissions on TS script";
							}
						
						}  
						if ( $transaction_error == 0 ) {
							$cmd="cd $cloned_repo_path; $git pull >> $gitlog  2>&1";
							system($cmd, $retcode);
							if ( $retcode != 0 ) {
								$transaction_error=1;
								$transaction_errmsg="Failed to git pull in workspace";
							} else { 
								$cmd = "cd $cloned_repo_path; $git add $tspath/* >> $gitlog 2>&1";
								system($cmd, $retcode);
								if ( $retcode != 0 ) {
									$transaction_error=1;
									$transaction_errmsg="Failed to git add TS files";
								} else {
									$auth_info=getAuthInfo(&$db, $auth_id);
									$cmd = "cd $cloned_repo_path; $git commit -m \"created new TS with ID=$ts_id, author= " . 
										$auth_info['fname'] . " " . $auth_info['lname'] . " (" . $auth_info['signum'] . ")\" >> $gitlog 2>&1";
									system($cmd, $retcode);
									if ( $retcode != 0 ) {
										$transaction_error=1;
										$transaction_errmsg="Failed to commit TS files";
									} else {
										$cmd="cd $cloned_repo_path; $git push >> $gitlog 2>&1";
										system($cmd, $retcode);
										if ( $retcode != 0 ) {
											$transaction_error=1;
											$transaction_errmsg="Failed to git push in workspace";
										}
									}
									
								}
							}
						}
					}
				} else {
					$transaction_error=1;
					$transaction_errmsg="TS directory $tspath already exists";
	
				}
			} else {
				# edit mode
				# check TS dir already exists
				if ( ! file_exists($tspath) ) {
					$transaction_error=1;
					$transaction_errmsg="TS dir $tspath does not exist for existing TS";
				} else {
					$needupdate=FALSE;
					$cmd="cmp -s $tmp_specfile $specfilepath";
					system($cmd, $retcode);
					if ( $retcode != 0 ) {
						$needupdate=TRUE;
					}
					if ( $needupdate ) {
		 				$cmd="cd $cloned_repo_path; $git pull >> $gitlog  2>&1";
                                  		system($cmd, $retcode);
                                       		if ( $retcode != 0 ) {
                                       			$transaction_error=1;
                                       			$transaction_errmsg="Failed to git pull in workspace";
						} 
						# copy updated specfile to TS dir
						if ( $transaction_error == 0 ) {
							if ( ! copy( $tmp_specfile, $specfilepath ) ) {
								$transaction_error=1;
								$transaction_errmsg="Failed to copy TS spec file to $tspath";
							} else {
								if ( $transaction_error == 0 ) {
									$cmd = "cd $cloned_repo_path; $git add $tspath/*; $git add $tspath/.* >> $gitlog 2>&1";
									system($cmd, $retcode);
									if ( $retcode != 0 ) {
										$transaction_error=1;
										$transaction_errmsg="Failed to git add TS files";
							 		} else {
										# commit changes
										$auth_info=getAuthInfo(&$db, $auth_id);
										$cmd = "cd $cloned_repo_path; $git commit -m \"updated TS ID=$ts_id spec file, author=" .
									   			$auth_info['fname'] . " " . $auth_info['lname'] . " (" . $auth_info['signum'] . ")\" >> $gitlog 2>&1"; 
										system($cmd, $retcode);
										if ( $retcode != 0 ) {
											$transaction_error=1;
											$transaction_errmsg="Failed to commit TS files";
										} else {
											$cmd="cd $cloned_repo_path; $git push >> $gitlog 2>&1";
											system($cmd, $retcode);
											if ( $retcode != 0 ) {
												$transaction_error=1;
												$transaction_errmsg="Failed to git push in workspace";
											}
										}
									}
								}
							} 
						} 
					}
				}
			} 
			# if no errors we commit transaction, otherwise rollback
			if ( $transaction_error == 0 ) {
				if (! mysqli_commit($mysqli) ) {
					$transaction_error=1;
					$transaction_errmsg="failed to commit mysql transaction";
				}
			} else {
				if ( ! mysqli_rollback($mysqli) ) {
					$transaction_errmsg .= "Unable to rollback mysql transacion";
				}
			}
	
			# if new tc and transaction OK reload form in edit mode
			if ( $transaction_error == 0 && $mode == "new" ) {
				$url=$_SERVER['PHP_SELF'] . "?mode=edit&ts_id=" . $ts_id . "&newrec=1";
				header("Location: $url");
			}

			# remove temp specfile
			if ( file_exists($tmp_specfile) ) {
				unlink( $tmp_specfile );
			}
					
		}
	}

}

# output the page

include "header.php";
?>
<script language="Javascript">
<!--
function toggleFields(mode)
{
	if ( mode == 'view' ) {
		disabled_state=true;
	} else {
		disabled_state=false;
	}
	document.testsuiteform.ts_id.disabled=true;
	document.testsuiteform.specfilepath.disabled=true;
	document.testsuiteform.tssfilepath.disabled=true;
	document.testsuiteform.name.disabled=disabled_state;
	document.testsuiteform.auth_id.disabled=disabled_state;
	document.testsuiteform.slogan.disabled=disabled_state;
	document.testsuiteform.exemode[0].disabled=disabled_state;
	document.testsuiteform.exemode[1].disabled=disabled_state;
	document.testsuiteform.timeout.disabled=disabled_state;
	avail_tc_selectlist = document.testsuiteform.elements['available_tcs[]'];
	sel_tc_selectlist = document.testsuiteform.elements['selected_tcs[]'];
	avail_tc_selectlist.disabled=disabled_state;
	sel_tc_selectlist.disabled=disabled_state;
	document.testsuiteform.submittestsuite.disabled=disabled_state;
	return true;
}


/* function to enable fields before submit so
 they appear in POST info */
function enableAllFieldsBeforeSubmit() 
{
	document.testsuiteform.ts_id.disabled=false;
	document.testsuiteform.specfilepath.disabled=false;
	document.testsuiteform.tssfilepath.disabled=false;
	document.testsuiteform.name.disabled=false;
	document.testsuiteform.auth_id.disabled=false;
	document.testsuiteform.slogan.disabled=false;
	document.testsuiteform.timeout.disabled=false;
	document.testsuiteform.exemode[0].disabled=false;
	document.testsuiteform.exemode[1].disabled=false;
	document.testsuiteform.submittestsuite.disabled=false;
	avail_tc_selectlist = document.testsuiteform.elements['available_tcs[]'];
	sel_tc_selectlist = document.testsuiteform.elements['selected_tcs[]'];
	avail_tc_selectlist.disabled=false;
	sel_tc_selectlist.disabled=false;
	selectAllOptions(sel_tc_selectlist);
	return true;
}

var NS4 = (navigator.appName == "Netscape" && parseInt(navigator.appVersion) < 5);

function addOption(theSel, theText, theValue)
{
  var newOpt = new Option(theText, theValue);
  var selLength = theSel.length;
  theSel.options[selLength] = newOpt;
}

function deleteOption(theSel, theIndex)
{ 
  var selLength = theSel.length;
  if(selLength>0)
  {
    theSel.options[theIndex] = null;
  }
}

function moveOptions(theSelFrom, theSelTo)
{
  
  var selLength = theSelFrom.length;
  var selectedText = new Array();
  var selectedValues = new Array();
  var selectedCount = 0;
  
  var i;
  
  // Find the selected Options in reverse order
  // and delete them from the 'from' Select.
  for(i=selLength-1; i>=0; i--)
  {
    if(theSelFrom.options[i].selected)
    {
      selectedText[selectedCount] = theSelFrom.options[i].text;
      selectedValues[selectedCount] = theSelFrom.options[i].value;
      deleteOption(theSelFrom, i);
      selectedCount++;
    }
  }
  
  // Add the selected text/values in reverse order.
  // This will add the Options to the 'to' Select
  // in the same order as they were in the 'from' Select.
  for(i=selectedCount-1; i>=0; i--)
  {
    addOption(theSelTo, selectedText[i], selectedValues[i]);
  }
  
  if(NS4) history.go(0);
}

function selectAllOptions(selObj)
{
  for (var i=0; i<selObj.options.length; i++) {
    selObj.options[i].selected = true;
  }
}

function OnDeleteClick(tc_id)
{
	if (confirm("Are you sure you want to delete the test suite?" )) { 
		return true;
	} else {
		return false
	}
}


//-->
</script>


<?php
echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness</a>&gt;<a href=\"usecases.php\">Usecases</a>&gt;<a href=\"testsuites.php\">Test suites&gt;</a>" . $mode . " test suite\n";
if ( $mode == "view" ) {
	echo "(<a href=./" . $_SERVER['PHP_SELF'] . "?mode=edit&ts_id=" . $ts_id . ">Edit</a>)\n";
}



# check some testcases are defined otherwise bail out
if ( count( $all_tcs ) == 1 ) {
	echo "<p><strong>No testcases are defined. <a href=\"testcase.php?mode=new\">Add</a> some testcases and try again.</strong>\n";
	include "harness_footer.php";
	exit;
}

$html="";



$html="";

# check for and report form submission or transaction errors
if (  isset ($_POST['submittestsuite']) ) {
	if ( $formerrors > 0 ) {
		$html .= "<p>NOTE: There are problems with the information provided as indicated in red below. Please correct the problems and click <strong>" . $submitbtntxt . "</strong>\n";
	} else {
		if ( $transaction_error == 1 ) {
			$html .= "<p><strong>Error occured adding/editing test suite, record NOT saved. More info:</strong>\n";
			$html .= "<p><strong>" . $transaction_errmsg . "</strong>\n";
		} else {
			if ( $mode == "new" ) $action="added, TS ID=" . $ts_id;
			if ( $mode == "edit" ) $action="updated";
			$html .= "<p><strong>Test suite successfully " . $action . "</strong>\n"; 
		} 
	}
}

# if new TS rec and no transaction errors output a confirmation message
if ( isset( $_GET['newrec'] ) ) {
	$newrec=$_GET['newrec'];
} else {
	$newrec=0;
} 
if ( $newrec == 1 ) {
	$html .= "<p><strong>Test suite successfully added, TS ID=" . $ts_id . "</strong>\n"; 
}

if ( $mode == "new" || $mode == "edit" )
 	$html .= "<p><span class=\"fieldnote\">* = required field</span>\n";

echo $html;

	
if ( $mode == "edit") {
	$actionurl = $_SERVER['PHP_SELF'] . "?mode=edit&ts_id=" . $ts_id;
} else {
	$actionurl = $_SERVER['PHP_SELF'] . "?mode=new";
}
$form = new HtmlForm("testsuiteform", '', $actionurl, 500, "onSubmit='enableAllFieldsBeforeSubmit()'");

$fld = new TextField('ID:', 'ts_id', 'text', $ts_id, 'test suite ID', 10, 64, '') ;
$form->AddField($fld);

$fld = new SelectList(&$db, '* Author :', 'auth_id', array($auth_id), 'test suite author', $auth_id_err, 0, 0, '');
$fld->SetDBOptions('author', 'id', 'signum', '', 'signum', array('', 'Select'));
$form->AddField($fld);

$fld = new TextField('* Name:', 'name', 'text', stripslashes($name), 'e.g. SMRS_ALL_NETWORKS. Max 30 chars. No spaces allowed.', 40, 64, $name_err) ;
$form->AddField($fld);

$fld = new TextField('* Slogan:', 'slogan', 'text', stripslashes($slogan), 'short sentence describing test suite', 120, '', $slogan_err) ;
$form->AddField($fld);
$fld = new OptionList('radio', 'exemode', '* Execution mode:', array('Dependent'=>'dependent', 'Independent'=>'independent'), array($exemode), "", $exemode_err);
$form->AddField($fld);
$fld = new TextField('* Timeout:', 'timeout', 'text', $timeout, 'TS callback timeout in seconds', 30, 64, $timeout_err) ;
$form->AddField($fld);

$specfilepath_disp=str_replace($cloned_repo_sourcedir, $harness_tgtdir, $specfilepath);
$fld = new TextField('TS spec file path:', 'specfilepath', 'text', $specfilepath_disp, '', 100, 100, '') ;
$form->AddField($fld);
$tssfilepath_disp=str_replace($cloned_repo_sourcedir, $harness_tgtdir, $tssfilepath);
$fld = new TextField('TS script file path:', 'tssfilepath', 'text', $tssfilepath_disp, '', 100, 100, '') ;
$form->AddField($fld);

$selection_html=createTCSelection(&$db, $available_tcs, $selected_tcs, $selected_tcs_err);
$fld = new CustomHTML($selection_html);
$form->AddField($fld);

$fld = new Button('submittestsuite', $submitbtntxt,'submit');
$form->AddButton($fld);

$form->Draw();

if ( $mode == "view" )
	echo "<p><a href=./" . $_SERVER['PHP_SELF'] . "?mode=edit&ts_id=$ts_id>Edit this test suite</a>\n";
if ( $mode == "new" || $mode == "edit" )
	echo "<p><a href=./" . $_SERVER['REQUEST_URI'] . ">Discard Edits</a>\n";
if ( $mode != "new" )
	echo "<p><a href=./" . $_SERVER['PHP_SELF'] . "?mode=new>Create new test suite</a>\n";
if ( $mode == "edit" )
	echo "<p><a href=./testsuite_delete.php?ts_id=$ts_id onclick=\" return(OnDeleteClick($ts_id))\">Delete this test suite</a>\n";




include "harness_footer.php";
?>


