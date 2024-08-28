<?php
#
# script to display and process testcase form
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

function generateTCSpecFile(&$db, $tmpspecfilepath, $uc_id, $tc_id, $slogan, $type, $polarity, $priority, $automated, $dependent, $timeout,
				 $passcode, $auth_id, $cont_id, $preconditions, $manual_steps, $postconditions, $disabled ) {
	if ( ! ($tmpfname = tempnam($tmpspecfilepath, "FOO") ) ) {
		return FALSE;
	}
	if ( ! ($fh = fopen($tmpfname, 'w') ) ) {
		return FALSE;
	}
	$fa_name = getFunctionalAreaFromUsecase(&$db, $uc_id);
	$uc_info = getUCInfo(&$db, $uc_id);
	$uc_name = $uc_info['uc_name'];
	$auth_info = getAuthInfo(&$db, $auth_id);
	$auth_signum = $auth_info['signum'];
	$cont_info = getAuthInfo(&$db, $cont_id);
	$cont_signum = $cont_info['signum'];
	$preconditions=str_replace("\r", "", $preconditions);
	$manual_steps=str_replace("\r", "", $manual_steps);
	$postconditions=str_replace("\r", "", $postconditions);
	$preconditions="#" . str_replace("\n", "\n#", $preconditions);
	$manual_steps="#" . str_replace("\n", "\n#", $manual_steps );
	$postconditions="#" . str_replace("\n", "\n#", $postconditions);
	date_default_timezone_set('Europe/Dublin');
	$date=date('l jS \of F Y G:i:s');
	$slogan = addslashes($slogan);

	$contents = <<<EOC
#!/bin/bash

# TEST CASE SPEC FILE
# This file is generated/updated using the test framework web interface.
# Manual updates on test servers are lost when the test package is re-installed.
#
# Last modified: $date
#

# These settings must be in BASH script format, they are sourced by other scripts.
SPEC_FA_NAME="$fa_name"
SPEC_UC_NAME="$uc_name"
SPEC_TC_ID=$tc_id
SPEC_TC_SLOGAN="$slogan"
SPEC_TC_TYPE="$type"
SPEC_TC_POLARITY="$polarity"
SPEC_TC_PRIORITY="$priority"
SPEC_TC_AUTOMATED=$automated
SPEC_TC_DEPENDENT_ONLY=$dependent
SPEC_TC_TIMEOUT=$timeout
SPEC_TC_TEST_PASSCODE=$passcode
SPEC_TC_AUTHOR="$auth_signum"
SPEC_TC_AUTOMATOR="$cont_signum"
# this parameter is not interpreted by harness
SPEC_TC_DISABLED=$disabled

# Please do not remove the comment lines below.
# They are used as section locators by the test harness.

# BEGIN_PRE_CONDITIONS
$preconditions
# END_PRE_CONDITIONS


# BEGIN_MANUAL_STEPS
$manual_steps
# END_MANUAL_STEPS

# BEGIN_POST_CONDITIONS
$postconditions
# END_POST_CONDITIONS

EOC;
	fwrite($fh, $contents);
	fclose( $fh );
	return $tmpfname;

}

# function to return list of usecases for selected 
# functional area

function getUsecaseList(&$db, $fa_id, &$usecase_names, &$usecase_ids) {
	# get usecases for selected FA

	$sql = "SELECT name, id FROM usecase";
	if ( $fa_id != 0 )
		$sql .= " WHERE fa_id=" . $fa_id;
	$sql .= " ORDER BY name";
	$result = &$db->doQuery($sql);
	if (mysql_num_rows($result) > 0 ) {
		while ( ($row =  mysql_fetch_object($result)) ) {
			$id = $row->id;
			$name=$row->name;
			array_push($usecase_ids, $id);
			array_push($usecase_names, $name);
		}
	}
	return TRUE;
		

}

#
# function to get FA name from passed fa_id
#

function getFANameFromID( &$db,  $fa_id ) {
	if ( $fa_id == 0 ) 
		return "*unknown*";
	$sql = "SELECT name FROM funcarea WHERE ID=$fa_id";
	$result = &$db->doQuery($sql);
	if (mysql_num_rows($result) > 0 ) {
		$row =  mysql_fetch_object($result);
		return $row->name;
	}
}


$submitbtntxt="Save";
$uc_name_err="";
$slogan_err="";
$steps_err="";
$fa_id_err="";
$auth_id_err="";
$cont_id_err="";
$uc_id_err="";
$type_err="";
$priority_err=0;
$polarity_err=0;
$timeout_err='';
$passcode_err='';
$automated_err='';
$dependent_err='';
$expect_plugin_err='';
$preconditions_err='';
$postconditions_err='';
$manual_steps_err='';
$disabled_err='';

$uc_id_def=0;
$auth_id_def=0;
$cont_id_def=0;
$timeout_def=300;
$passcode_def=0;
$expect_plugin_def=0;
$tc_id_def=0;
$fa_name_def="*new*";
$slogan_def='';
$preconditions_def='';
$manual_steps_def='';
$postconditions_def='';
$polarity_def="positive";
$priority_def="high";
$type_def="FT";
$automated_def=0;
$dependent_def=0;
$disabled_def=0;
$specfilepath_def = "*unknown*";
$tcfilepath_def = "*unknown*";
$formerrors=0;
$gitmove=0;
$tmpspecfileloc="/tmp";
$transaction_error=0;
$transaction_errmsg="";
$cloned_repo_path="/var/lib/jenkins/cominf_build/workspace/cominf_test";
$cloned_repo_sourcedir=$cloned_repo_path . "/SOURCES";
$tc_script_template=$cloned_repo_sourcedir . "/harness/etc/test_callbacks.lib.template";
$git="/usr/bin/git";
$gitlog="/var/log/gitlog";
$harness_tgtdir="/opt/ericsson/cominf_test";

# if in new/edit mode and GIT not installed 
# we have big problem
if ( ( $mode == "new" || $mode == "edit" ) && ! is_executable ($git) ) { 
	FatalError(105,'git is not installed or executable on server', __file__, __line__); 
}






if ( $mode == "new" ) {
	$specfilepath='*new*';
	$tcfilepath='*new*';
	if ( ! isset ($_POST['submittestcase']) ) {
		if ( isset( $_GET['uc_id'] ) ) {
			$uc_id = $_GET['uc_id'];
		} else {
			$uc_id=$uc_id_def;
		}
		$auth_id=$auth_id_def;
		$cont_id=$cont_id_def;
		$tc_id=$tc_id_def;
		$slogan=$slogan_def;
		$manual_steps=$manual_steps_def;
		$preconditions=$preconditions_def;
		$postconditions=$postconditions_def;
		$polarity=$polarity_def;
		$type=$type_def;
		$priority=$priority_def;
		$passcode=$passcode_def;
		$automated=$automated_def;
		$dependent=$dependent_def;
		$disabled=$disabled_def;
		$timeout=$timeout_def;
 		$expect_plugin=$expect_plugin_def;
		$fa_name=$fa_name_def;
		$specfilepath=$specfilepath_def;
		$tcfilepath=$tcfilepath_def;
	}

} else {

	if ( ! isset ($_POST['submittestcase']) ) {
		# we are in view|edit mode and not submitting form
		if ( ! isset ($_GET['tc_id']) ) 
			FatalError(105,'testcase id not found', __file__, __line__); 
		$tc_id=$_GET['tc_id'];
		$sql = "SELECT usecase.name AS uc_name, author.signum AS auth_signum, contact.signum AS cont_signum, testcase.id, testcase.uc_id, testcase.auth_id, testcase.cont_id, testcase.slogan, polarity, priority, passcode, type, automated, dependent, timeout, expect_plugin, preconditions, manual_steps, postconditions, approved, disabled FROM testcase,usecase, author, author AS contact WHERE testcase.id=$tc_id AND usecase.id=testcase.uc_id AND author.id=testcase.auth_id AND contact.id=testcase.cont_id";
		$result = &$db->doQuery($sql);
		if( mysql_num_rows($result) == 0)
			FatalError(105,'testcase record not found', __file__, __line__);
		$row = mysql_fetch_object($result);
		$tc_id=$row->id;
		$uc_id=$row->uc_id;
		$uc_name=$row->uc_name;
		$auth_id=$row->auth_id;
		$cont_id=$row->cont_id;
		$auth_signum=$row->auth_signum;
		$cont_signum=$row->cont_signum;
		$slogan=$row->slogan;
		$polarity=$row->polarity;
		$priority=$row->priority;
		$passcode=$row->passcode;
		$type=$row->type;
		$automated=$row->automated;
		$dependent=$row->dependent;
		$timeout=$row->timeout;
		$preconditions=$row->preconditions;
		$manual_steps=$row->manual_steps;
		$postconditions=$row->postconditions;
		$disabled=$row->disabled;
		$approved=$row->approved;
		$expect_plugin=$row->expect_plugin;

		# get TC paths
		if ( isset($uc_id) && $uc_id > 0 && isset($tc_id) && $tc_id > 0 ) {  
			$tcpath = getTCDirPath( &$db, $uc_id, $tc_id );
			$specfilepath = getTCSpecPath( $tcpath, $tc_id );
			$tcfilepath = getTCPath( $tcpath, $tc_id );
		}
	}
}

if ( isset ($_POST['submittestcase']) &&  $_POST['submittestcase'] == "$submitbtntxt" ) {
	# process the form
#print("<pre>");
#print_r($_POST);
#print("</pre>");
	if ( isset( $_GET['tc_id'] ) ) {
		$tc_id =$_GET['tc_id'];
	} else if ( isset($_POST['tc_id']) ) {
		$tc_id = $_POST['tc_id'];
	} else {
		$tc_id = $tc_id_def;
	}

	if ( ! isset( $_POST['fa_name'] ) ) {
		$fa_name=$fa_name_def;
	}

	if ( ! isset( $_POST['uc_id'] ) || $_POST['uc_id'] == 0 ) {
		$formerrors++;
		$uc_id_err = "Please choose a usecase";
		$uc_id = $uc_id_def;
	} else {
    		$uc_id = $_POST['uc_id'];;
	}
	if ( ! isset( $_POST['auth_id'] ) || $_POST['auth_id'] == 0 ) {
		$formerrors++;
		$auth_id_err = "Please choose an author.";
		$auth_id = $auth_id_def;
	} else {
    		$auth_id = $_POST['auth_id'];
	}
	if ( ! isset( $_POST['cont_id'] ) || $_POST['cont_id'] == 0 ) {
		$formerrors++;
		$cont_id_err = "Please choose a contact.";
		$cont_id = $cont_id_def;
	} else {
    		$cont_id = $_POST['cont_id'];
	}
	if ( ! isset( $_POST['type'] ) ) {
		$formerrors++;
		$type_err = "Please enter a type.";
		$type = $type_def;
	} else {
    		$type = $_POST['type'];
	}
	if ( ! isset( $_POST['polarity'] ) ) {
		$formerrors++;
		$polarity_err = "Please enter a polarity.";
		$polarity = $polarity_def;
	} else {
    		$polarity = $_POST['polarity'];
	}
	if ( ! isset( $_POST['priority'] ) ) {
		$formerrors++;
		$priority_err = "Please enter a priority.";
		$priority = $priority_def;
	} else {
    		$priority = $_POST['priority'];
	}
	if ( ! isset( $_POST['slogan'] ) || $_POST['slogan'] == '' ) {
		$formerrors++;
		$slogan_err = "Please enter a slogan.";
		$slogan=$slogan_def;
	} else {
		$slogan = $_POST['slogan'];
	}

	if ( ! isset( $_POST['automated'] ) ) {
		$automated=$automated_def;
	} else {
    		$automated = $_POST['automated'];
	}

	if ( ! isset( $_POST['dependent'] ) ) {
		$dependent=$dependent_def;
	} else {
    		$dependent = $_POST['dependent'];
	}

	if ( ! isset( $_POST['disabled'] ) ) {
		$disabled=$disabled_def;
	} else {
    		$disabled = $_POST['disabled'];
	}

	if ( ! isset( $_POST['passcode'] ) || $_POST['passcode'] == '') {
		if ( $automated == 1 ) {
			$formerrors++;
			$passcode_err = "Please enter a pass code";
		}
		$passcode=$passcode_def;
	} else if ( $_POST['passcode'] < 0 || $_POST['passcode'] > 255 )  {
		if ( $automated == 1 ) {
			$formerrors++;
			$passcode_err = "Passcode out of range";
		}
		$passcode=$_POST['passcode'];
	} else {
		$passcode = $_POST['passcode'];
	}
	if (  ! isset( $_POST['timeout'] ) || $_POST['timeout'] == '' ) {
		if ( $automated == 1 ) {
			$formerrors++;
			$timeout_err = "Please enter a timeout";
		}
		$timeout=$timeout_def;
	} else {
		$timeout = $_POST['timeout'];
	}
	if ( ! isset( $_POST['expect_plugin'] ) ) {
		$expect_plugin = $expect_plugin_def;
	} else {
    		$expect_plugin = $_POST['expect_plugin'];
	}
	if ( ! isset( $_POST['preconditions'] ) ||  $_POST['preconditions'] == '' ) {
		$formerrors++;
		$preconditions_err="Please enter some preconditions";
		$preconditions=$preconditions_def;
	} else {
		$preconditions = $_POST['preconditions'];
	}
	if ( ! isset( $_POST['manual_steps'] ) ||  $_POST['manual_steps'] == '' ) {
		$formerrors++;
		$manual_steps_err="Please enter some manual steps";
		$manual_steps=$manual_steps_def;
	} else {
		$manual_steps = $_POST['manual_steps'];
	}
	if ( ! isset( $_POST['postconditions'] ) ||  $_POST['postconditions'] == '' ) {
		$formerrors++;
		$postconditions_err="Please enter some post-conditions";
		$postconditions=$postconditions_def;
	} else {
		$postconditions = $_POST['postconditions'];
	}
		
	
	if ( $formerrors == 0 ) {
		mysqli_autocommit($mysqli, FALSE);
		if ( $mode == "new" ) {
			# create new testcase record
			$sql = "INSERT INTO testcase (uc_id, auth_id, cont_id, slogan, polarity, type, priority, dependent, automated, timeout, passcode, expect_plugin, preconditions, postconditions, manual_steps, disabled) VALUES (" . $uc_id . "," . $auth_id . "," . $cont_id . ",\"" . addslashes($slogan) . "\",\"" . $polarity . "\",\"" . $type . "\",\"" . $priority  . "\"," . $dependent . "," . $automated . "," . $timeout . "," . $passcode . "," . $expect_plugin . ",\"" . addslashes($preconditions) . "\",\"" . addslashes($postconditions) . "\",\"" . addslashes($manual_steps) . "\"," . $disabled . ")";  
			if ( ! ($result = $dbi->doQuery($sql)) ) {
				$transaction_error=1;
				$transaction_errmsg .= "Unable to create new TC in database"; 
			} else {
				$tc_id=mysqli_insert_id($mysqli);
			}
		} else {
			# update existing testcase record

			# store old usecase name before updating record since 
			# changing usecase needs to trigger GIT move event
			$sql="SELECT uc_id FROM testcase WHERE id=$tc_id";
			$result = &$db->doQuery($sql);
			if( mysql_num_rows($result) == 0)
				FatalError(105,"unable to determine existing usecase id for tc=$tc_id", __file__, __line__);
			$row = mysql_fetch_object($result);
			$old_uc_id=$row->uc_id;
			$uc_info = getUCInfo(&$db, $old_uc_id);
			$old_uc_name = $uc_info['uc_name'];
			$old_tcpath = getTCDirPath( &$db, $old_uc_id, $tc_id );
			$old_specfilepath = getTCSpecPath( $old_tcpath, $tc_id );
			$old_tcfilepath = getTCPath( $old_tcpath, $tc_id );

			
			$sql = "UPDATE testcase SET uc_id=" . $uc_id . ", auth_id=" . $auth_id . ", cont_id=" . $cont_id . ", slogan=\"" . addslashes($slogan) . "\", polarity=\"" . $polarity . "\", type=\"" . $type . "\", priority=\"" . $priority . "\", dependent=" . $dependent . ", automated=" . $automated . ", timeout=" . $timeout . ", passcode=" . $passcode . ", expect_plugin=" . $expect_plugin . ", preconditions=\"" . addslashes($preconditions) . "\", manual_steps=\"" . addslashes($manual_steps) . "\", postconditions=\"" . addslashes($postconditions) . "\", disabled=" . $disabled . " WHERE id=" . $tc_id;
			if (!($result = $dbi->doQuery($sql))) {
				$transaction_error=1;
				$transaction_errmsg="Unable to update TC record (TC ID=" . $tc_id . ")";
			} else {
				$uc_info = getUCInfo(&$db, $uc_id);
				$new_uc_name = $uc_info['uc_name'];
				$tcpath = getTCDirPath( &$db, $uc_id, $tc_id );
				$specfilepath = getTCSpecPath( $tcpath, $tc_id );
	
				if ( $old_specfilepath != $specfilepath ) {
					$gitmove=1;
				}
			}

		}
	}
	# get TC paths
	if ( isset($uc_id) && $uc_id && isset($tc_id) && $tc_id > 0 ) {  
		$tcpath = getTCDirPath( &$db, $uc_id, $tc_id );
		$specfilepath = getTCSpecPath( $tcpath, $tc_id );
		$tcfilepath = getTCPath( $tcpath, $tc_id );
	} else {
		$specfilepath = $specfilepath_def;
		$tcfilepath = $tcfilepath_def;
	}
	if ( $formerrors == 0 && $transaction_error == 0 ) {
		# generate the spec file in a temporary location
		if ( ! ( $tmp_specfile = generateTCSpecFile(&$db, $tmpspecfileloc, $uc_id, $tc_id, $slogan, $type, $polarity, $priority, $automated, $dependent, $timeout,
								 $passcode, $auth_id, $cont_id, $preconditions, $manual_steps, $postconditions, $disabled) ) ) {
			$transaction_error=1;
			$transaction_errmsg="Failed to create temp TC spec file";
		} else { 
			# update the file system and commit to git
			if ( $mode == "new" ) {
				# create directory structure for the TC
				if ( ! file_exists($tcpath) ) {
					if ( ! mkdir($tcpath, 0755, true) ) {
						$transaction_error=1;
						$transaction_errmsg="Failed to create TC dir $tcpath";
					} else {
						# copy specfile to new dir
						if ( ! copy( $tmp_specfile, $specfilepath ) ) {
							$transaction_error=1;
							$transaction_errmsg="Failed to copy TC spec file to $tcpath";
						} 
						# copy script template to new dir
						if ( ! copy( $tc_script_template, $tcfilepath ) ) {
							$transaction_error=1;
							$transaction_errmsg="Failed to copy TC script file to $tcpath";
						} else {
							$cmd = "chmod +x $tcfilepath > /dev/null 2>&1";
							system($cmd, $retcode);
							if ($retcode != 0) {
								$transaction_error=1;
								$transaction_errmsg="Failed to set permissions on TC script";
							}
						
						}  

						if ( $transaction_error == 0 ) {
							$cmd="cd $cloned_repo_path; $git pull >> $gitlog  2>&1";
							system($cmd, $retcode);
							if ( $retcode != 0 ) {
								$transaction_error=1;
								$transaction_errmsg="Failed to git pull in workspace";
							} else { 
								$cmd = "cd $cloned_repo_path; $git add $tcpath/* >> $gitlog 2>&1";
								system($cmd, $retcode);
								if ( $retcode != 0 ) {
									$transaction_error=1;
									$transaction_errmsg="Failed to git add TC files";
								} else {
									$auth_info=getAuthInfo(&$db, $auth_id);
									$cmd = "cd $cloned_repo_path; $git commit -m \"created new TC with ID=$tc_id, author= " . 
										$auth_info['fname'] . " " . $auth_info['lname'] . " (" . $auth_info['signum'] . ")\" >> $gitlog 2>&1";
									system($cmd, $retcode);
									if ( $retcode != 0 ) {
										$transaction_error=1;
										$transaction_errmsg="Failed to commit TC files";
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
					$transaction_errmsg="TC directory $tcpath already exists";
	
				}
			} else {
				# edit mode
				# check TC dir already exists
				if ( ! file_exists($old_tcpath) ) {
					$transaction_error=1;
					$transaction_errmsg="TC dir $old_tcpath does not exist for existing TC";
				} else {
					$needupdate=FALSE;
					$cmd="cmp -s $tmp_specfile $old_specfilepath";
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
						} else { 
							# move testcase files to new location if needed
							if ( $gitmove == 1 ) {
									$dest_dir=dirname($tcpath);
									$cmd="cd $cloned_repo_path; $git mv $old_tcpath $dest_dir";
									system($cmd, $retcode);
									if ( $retcode != 0 ) {
										$transaction_error=1;
										$transaction_errmsg="git move for TC $tc_id failed";
									} 
							} 
						} 
						# copy updated specfile to TC dir
						if ( $transaction_error == 0 ) {
							# compare new and existing spec file to determine if we need to update
							if ( ! copy( $tmp_specfile, $specfilepath ) ) {
								$transaction_error=1;
								$transaction_errmsg="Failed to copy TC spec file to $tcpath";
							} else {
								# check disabled status
								if (  $disabled == 1 )  {
									if ( ! file_exists( "$tcpath/.disabled" ) ) {
										$cmd = "touch $tcpath/.disabled";
										system($cmd, $retcode);
										if ( $retcode != 0 ) {
											$transaction_error=1;
											$transaction_errmsg="Failed to disable TC $tc_id - unable to create .disabled file";
										} 
									} 
								} else {
									if ( file_exists( "$tcpath/.disabled" ) ) {
										$cmd = "cd $cloned_repo_path; $git rm $tcpath/.disabled >> $gitlog 2>&1";
										system($cmd, $retcode);
										if ( $retcode != 0 ) {
											$transaction_error=1;
											$transaction_errmsg="Failed to enable TC $tc_id - unable to remove .disabled file";
										} 
									}
							        }
								if ( $transaction_error == 0 ) {
									$cmd = "cd $cloned_repo_path; $git add $tcpath/*; $git add $tcpath/.* >> $gitlog 2>&1";
									system($cmd, $retcode);
									if ( $retcode != 0 ) {
										$transaction_error=1;
										$transaction_errmsg="Failed to git add TC files";
							 		} else {
										# commit changes 
										$auth_info=getAuthInfo(&$db, $auth_id);
										$cont_info=getAuthInfo(&$db, $cont_id);
										$cmd = "cd $cloned_repo_path; $git commit -m \"updated TC ID=$tc_id spec file, author=" .
									   			$auth_info['fname'] . " " . $auth_info['lname'] . " (" . $auth_info['signum'] . "), contact=" .
									   			$cont_info['fname'] . " " . $cont_info['lname'] . " (" . $cont_info['signum'] . ")\" >> $gitlog 2>&1";
										system($cmd, $retcode);
										if ( $retcode != 0 ) {
											$transaction_error=1;
											$transaction_errmsg="Failed to commit TC files";
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
				$url=$_SERVER['PHP_SELF'] . "?mode=edit&tc_id=" . $tc_id . "&newrec=1";
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
	disabled_colour='#EBEBE4';
	enabled_colour='#FFFFFF';
	if ( mode == 'view' ) {
		color=disabled_colour;
		/* disable all fields */
		document.testcaseform.tc_id.disabled=true;
		document.testcaseform.fa_name.disabled=true;
		document.testcaseform.auth_id.disabled=true;
		document.testcaseform.cont_id.disabled=true;
		document.testcaseform.uc_id.disabled=true;
		document.testcaseform.slogan.disabled=true;
		document.testcaseform.type[0].disabled=true;
		document.testcaseform.type[1].disabled=true;
		document.testcaseform.priority[0].disabled=true;
		document.testcaseform.priority[1].disabled=true;
		document.testcaseform.polarity[0].disabled=true;
		document.testcaseform.polarity[1].disabled=true;
		document.testcaseform.preconditions.disabled=true;
		document.testcaseform.automated.disabled=true;
		document.testcaseform.dependent.disabled=true;
		document.testcaseform.manual_steps.disabled=true;
		document.testcaseform.postconditions.disabled=true;
		document.testcaseform.timeout.disabled=true;
		document.testcaseform.passcode.disabled=true;
		document.testcaseform.expect_plugin.disabled=true;
		document.testcaseform.specfilepath.disabled=true;
		document.testcaseform.tcfilepath.disabled=true;
		document.testcaseform.disabled.disabled=true;
		document.testcaseform.submittestcase.disabled=true;
	} else {

		document.testcaseform.tc_id.disabled=true;
		document.testcaseform.fa_name.disabled=true;
		document.testcaseform.specfilepath.disabled=true;
		document.testcaseform.tcfilepath.disabled=true;

		if ( mode == 'new' ) {
			document.testcaseform.automated.disabled=true;
			document.testcaseform.disabled.disabled=true;
		}
	
		if ( document.testcaseform.automated.checked == false ) {
			document.testcaseform.preconditions.disabled=false;
			document.testcaseform.manual_steps.disabled=false;
			document.testcaseform.postconditions.disabled=false;
			document.testcaseform.timeout.disabled=true;
			document.testcaseform.passcode.disabled=true;
			document.testcaseform.expect_plugin.disabled=true;
		} else {
			document.testcaseform.preconditions.disabled=true;
			document.testcaseform.manual_steps.disabled=true;
			document.testcaseform.postconditions.disabled=true;
			document.testcaseform.timeout.disabled=false;
			document.testcaseform.passcode.disabled=false;
			document.testcaseform.expect_plugin.disabled=false;
		}
	}
	return true;
}

/* function to enable automation and disable fields on 
   successful new TC creation */
function enableFields()
{
	document.testcaseform.automated.disabled=false;
	document.testcaseform.disabled.disabled=false;

}


/* function to enable fields before submit so
 they appear in POST info */
function enableAllFieldsBeforeSubmit()
{
	document.testcaseform.tc_id.disabled=false;
	document.testcaseform.fa_name.disabled=false;
	document.testcaseform.auth_id.disabled=false;
	document.testcaseform.cont_id.disabled=false;
	document.testcaseform.uc_id.disabled=false;
	document.testcaseform.slogan.disabled=false;
	document.testcaseform.type[0].disabled=false;
	document.testcaseform.type[1].disabled=false;
	document.testcaseform.priority[0].disabled=false;
	document.testcaseform.priority[1].disabled=false;
	document.testcaseform.polarity[0].disabled=false;
	document.testcaseform.polarity[1].disabled=false;
	document.testcaseform.preconditions.disabled=false;
	document.testcaseform.automated.disabled=false;
	document.testcaseform.dependent.disabled=false;
	document.testcaseform.manual_steps.disabled=false;
	document.testcaseform.postconditions.disabled=false;
	document.testcaseform.timeout.disabled=false;
	document.testcaseform.passcode.disabled=false;
	document.testcaseform.expect_plugin.disabled=false;
	document.testcaseform.specfilepath.disabled=false;
	document.testcaseform.tcfilepath.disabled=false;
	document.testcaseform.disabled.disabled=false;
	document.testcaseform.submittestcase.disabled=false;
}

function OnDeleteClick(tc_id)
{
	if (confirm("Are you sure you want to delete the testcase?" )) { 
		return true;
	} else {
		return false
	}
}

-->
</script>
<?php
echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness</a>&gt;<a href=\"usecases.php\">Usecases</a>&gt;<a href=\"testcases.php\">Testcases&gt;</a>" . $mode . " testcase\n";
if ( $mode == "view" ) {
	echo "(<a href=./" . $_SERVER['PHP_SELF'] . "?mode=edit&tc_id=" . $tc_id . ">Edit</a>)\n";
}

# get functional area id
if ( isset( $_GET['fa_id'] ) ) {
	$fa_id = $_GET['fa_id'];
} else if ( isset( $_POST['fa_id'] ) ) {
	$fa_id = $_POST['fa_id'];
} else {
	$fa_id=0;
}

# check some usecases are defined otherwise bail out
$usecase_names=array("Select");
$usecase_ids=array(0);
getUsecaseList(&$db, $fa_id, $usecase_names, $usecase_ids);

if ( count( $usecase_names ) == 1 ) {
	echo "<p><strong>No usecases are defined. <a href=\"usecase.php?mode=new\">Add</a> a usecase and try again.</strong>\n";
	include "harness_footer.php";
	exit;
}

$html="";

# check for and report form submission or transaction errors
if (  isset ($_POST['submittestcase']) ) {
	if ( $formerrors > 0 ) {
		$html .= "<p>NOTE: There are problems with the information provided as indicated in red below. Please correct the problems and click <strong>" . $submitbtntxt . "</strong>\n";
	} else {
		if ( $transaction_error == 1 ) {
			$html .= "<p><strong>Error occured adding/editing test case, record NOT saved. More info:</strong>\n";
			$html .= "<p><strong>" . $transaction_errmsg . "</strong>\n";
		} else {
			if ( $mode == "new" ) $action="added, TC ID=" . $tc_id;
			if ( $mode == "edit" ) $action="updated";
			$html .= "<p><strong>Testcase successfully " . $action . "</strong>\n"; 
		} 
	}
}

# if new TC rec and no transaction errors output a confirmation message
if ( isset( $_GET['newrec'] ) ) {
	$newrec=$_GET['newrec'];
} else {
	$newrec=0;
} 
if ( $newrec == 1 ) {
	$html .= "<p><strong>Testcase successfully added, TC ID=" . $tc_id . "</strong>\n"; 
}

if ( $mode == "new" || $mode == "edit" )
 	$html .= "<p><span class=\"fieldnote\">* = required field</span>\n";

echo $html;

	
if ( $mode == "edit") {
	$actionurl = $_SERVER['PHP_SELF'] . "?mode=edit&tc_id=" . $tc_id;
} else {
	$actionurl = $_SERVER['PHP_SELF'] . "?mode=new";
}
$form = new HtmlForm("testcaseform", '', $actionurl, 500, "onSubmit='enableAllFieldsBeforeSubmit()'");

$fld = new TextField('ID:', 'tc_id', 'text', $tc_id, 'testcase ID', 10, 64, '') ;
$form->AddField($fld);

$fld = new SelectList(&$db, '* Author:', 'auth_id', array($auth_id), 'original test case author', $auth_id_err, 0, 0, '');
$fld->SetDBOptions('author', 'id', 'signum', '', 'signum', array('', 'Select'));
$form->AddField($fld);

$fld = new SelectList(&$db, '* Contact:', 'cont_id', array($cont_id), 'current test case contact e.g. test automater', $cont_id_err, 0, 0, '');
$fld->SetDBOptions('author', 'id', 'signum', '', 'signum', array('', 'Select'));
$form->AddField($fld);

if ( $mode == "new" ) {
	$fa_name = getFANameFromID( &$db, $fa_id );
} else {
	$fa_name = getFunctionalAreaFromUsecase(&$db, $uc_id);
}
$fld = new TextField('Functional Area:', 'fa_name', 'text', $fa_name, '', 30, 64, '') ;
$form->AddField($fld);
$fld = new Hidden('fa_id', $fa_id);
$form->AddField($fld);

$fld = new SelectList(&$db, '* Usecase:', 'uc_id', array($uc_id), '', $uc_id_err, 0, 0, "", "", "style=\"width:300px\"");
$fld->AddOptions($usecase_names, $usecase_ids);
#$fld->SetDBOptions('usecase', 'id', 'name', '', 'name', array('', 'Select'));
$form->AddField($fld);

$fld = new TextField('* Slogan:', 'slogan', 'text', stripslashes($slogan), 'short sentence describing testcase', 120, '', $slogan_err) ;
$form->AddField($fld);
$fld = new OptionList('radio','type', '* Type:', array('FT'=>'FT','Unit'=>'UNIT'), array($type), "", $type_err);
$form->AddField($fld);
$fld = new OptionList('radio','priority', '* Priority:', array('High'=>'high','Low'=>'low'), array($priority), "", $priority_err);
$form->AddField($fld);
$fld = new OptionList('radio','polarity', '* Polarity:', array('Positive'=>'positive','Negative'=>'negative'), array($polarity), "", $polarity_err);
$form->AddField($fld);
$fld = new CheckBox('dependent', 'Dependent', '1', $dependent, $dependent_err, 'check this box if the TC is dependent i.e. part of test suite and not meant to be run independently. If checked the harness will not run the tc_setup() and tc_cleanup() callbacks. ', "onChange=\"toggleFields()\"");
$form->AddField($fld);

$fld = new NonEditableField( "<tr><td colspan=2><hr></tr>\n");
$form->AddField($fld);

$fld = new CheckBox('automated', 'Automated', '1', $automated, $automated_err, 'check this box if the TC has been automated', "onChange=\"toggleFields()\"");
$form->AddField($fld);
$specfilepath_disp=str_replace($cloned_repo_sourcedir, $harness_tgtdir, $specfilepath);
$fld = new TextField('Spec file path:', 'specfilepath', 'text', $specfilepath_disp, '', 100, 100, '') ;
$form->AddField($fld);
$tcfilepath_disp=str_replace($cloned_repo_sourcedir, $harness_tgtdir, $tcfilepath);
$fld = new TextField('TC file path:', 'tcfilepath', 'text', $tcfilepath_disp, '', 100, 100, '') ;
$form->AddField($fld);

$fld = new TextField('* Timeout:', 'timeout', 'text', $timeout, 'automated TC callback timeout in seconds', 30, 64, $timeout_err) ;
$form->AddField($fld);
$fld = new TextField('Pass code:', 'passcode', 'text', $passcode, 'Return code from automated TC to be considered PASS result. Range: 0-255', 30, 64, $passcode_err) ;
$form->AddField($fld);

$fld = new CheckBox('expect_plugin', 'Expect Plugin:', '1', $expect_plugin, $expect_plugin_err, 'check this box if the automated TC is compatible with the expect plugin');
$form->AddField($fld);

$fld = new NonEditableField( "<tr><td colspan=2><hr></tr>\n");
$form->AddField($fld);

$fld = new TextArea('* Pre-conditions:', 'preconditions', stripslashes($preconditions), '', 10, 64, $preconditions_err);
$form->AddField($fld);
$fld = new TextArea('* Manual steps:', 'manual_steps', stripslashes($manual_steps), '', 10, 64, $manual_steps_err);
$form->AddField($fld);
$fld = new TextArea('* Post-conditions:', 'postconditions', stripslashes($postconditions), '', 10, 64, $postconditions_err);
$form->AddField($fld);

$fld = new CheckBox('disabled', 'Disabled', '1', $disabled, $disabled_err, '');
$form->AddField($fld);
	
$fld = new Button('submittestcase', $submitbtntxt,'submit');
$form->AddButton($fld);

$form->Draw();



if ( $mode == "view" )
	echo "<p><a href=./" . $_SERVER['PHP_SELF'] . "?mode=edit&tc_id=$tc_id>Edit this testcase</a>\n";
if ( $mode == "new" || $mode == "edit" )
	echo "<p><a href=./" . $_SERVER['REQUEST_URI'] . ">Discard Edits</a>\n";
if ( $mode != "new" )
	echo "<p><a href=./" . $_SERVER['PHP_SELF'] . "?mode=new>Create new testcase</a>\n";
if ( $mode == "edit" )
	echo "<p><a href=./testcase_delete.php?tc_id=$tc_id onclick=\" return(OnDeleteClick($tc_id))\">Delete this testcase</a>\n";




include "harness_footer.php";
?>


