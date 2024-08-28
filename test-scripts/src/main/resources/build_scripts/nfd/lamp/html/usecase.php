<?php
#
# script to display and process usecase form
#
#	Apr 2012 edavmax
include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;

#
# function to check if directory is empty
#

function isDirEmpty( $dirpath ) {
	$files = @scandir($dirpath);
	if ( count($files) > 2 )  {
		return FALSE;
	} else {
		return TRUE;
	}
}

#
# function to get FA name from passed fa_id
#

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

function moveTCsInGit( &$db, $old_fa_name, $old_uc_name, $uc_id, &$transaction_errmsg='' ) {
	global $cloned_repo_path;
	global $git;
	global $gitlog;
	$transaction_error=0;
	$sql = "SELECT id, slogan FROM testcase WHERE uc_id=$uc_id";
	$result = &$db->doQuery($sql);
	if (mysql_num_rows($result) > 0 ) {
		$cmd="cd $cloned_repo_path; $git pull >> $gitlog  2>&1";
		system($cmd, $retcode);
		if ( $retcode != 0 ) {
			$transaction_error=1;
			$transaction_errmsg="Failed to git pull in workspace";
		} else {
			while ( ($row =  mysql_fetch_object($result)) ) {
				$tc_id = $row->id;
				$slogan = $row->slogan;
				$oldtcpath = getOldTCDirPath (&$db, $old_fa_name, $old_uc_name, $uc_id, $tc_id);
				$newtcpath = getTCDirPath( &$db, $uc_id, $tc_id );
				if ( ! file_exists( $oldtcpath ) ) {
					$transaction_error=1;
					$transaction_errmsg.="Old TC dir $oldtcpath does not exist";
				} else {
					
					if ( ! file_exists( dirname($newtcpath) ) ) {
						if ( ! mkdir( dirname($newtcpath), 0755, true) ) {
							$transaction_error=1;
							$transaction_errmsg.="Failed to make dir $newtcpath";
						}
					}
					if ( $transaction_error == 0 ) {
						
						$cmd="cd $cloned_repo_path; $git mv $oldtcpath $newtcpath >> $gitlog 2>&1";
						system($cmd, $retcode);
						if ( $retcode != 0 ) {
							$transaction_error=1;
							$transaction_errmsg.="git move for TC $tc_id failed";
						} else {
							$cmd = "cd $cloned_repo_path; $git commit -m \"Git move of TC with ID=$tc_id triggered by UC rename\" >> $gitlog 2>&1";
							system($cmd, $retcode);
							if ( $retcode != 0 ) {
								$transaction_error=1;
								$transaction_errmsg.="Failed to commit moved";
							} else {
								$cmd="cd $cloned_repo_path; $git push >> $gitlog 2>&1";
								system($cmd, $retcode);
								if ( $retcode != 0 ) {
									$transaction_error=1;
									$transaction_errmsg.="Failed to git push in workspace";
								} else {
									if ( file_exists( dirname($oldtcpath) ) && isDirEmpty( dirname($oldtcpath) ) ) {
										rmdir( dirname($oldtcpath) ); 
									}
								}
							}
						}
					}
				} 
			}
		}
	}
	if ( $transaction_error == 0 ) {
		return true;
	} else {
		return false;
	}
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

function getOldTCDirPath(&$db, $old_fa_name, $old_uc_name, $uc_id, $tc_id) {
	global $cloned_repo_sourcedir;
	$uc_info = array();
	if ( ! isset( $uc_id) || ! isset( $tc_id ) ) {
		return '*unknown*'; 
	}

	$fa_name = getFunctionalAreaFromUsecase(&$db, $uc_id);

	$tcpath="$cloned_repo_sourcedir/FA/$old_fa_name/$old_uc_name/$tc_id";
	return $tcpath;
}

$submitbtntxt="Save";
$uc_name_err="";
$slogan_err="";
$steps_err="";
$fa_id_err=0;
$auth_id_err=0;
$transaction_error=0;
$transaction_errmsg='';
$cloned_repo_path="/var/lib/jenkins/cominf_build/workspace/cominf_test";
$cloned_repo_sourcedir=$cloned_repo_path . "/SOURCES";
$tc_script_template=$cloned_repo_sourcedir . "/harness/etc/test_callbacks.lib.template";
$git="/usr/bin/git";
$gitlog="/var/log/gitlog";
$harness_tgtdir="/opt/ericsson/cominf_test";


$fa_id_def=0;
$auth_id_def=0;
$uc_id_def=0;
$uc_name_def="";
$slogan_def="";
$steps_def="";
$gitmove=0;



if ( ! isset($_GET['mode']) ) 
	$mode="new";
else
	$mode=$_GET['mode'];



if ( $mode == "new" ) {
	$fa_id=$fa_id_def;
	$auth_id=$auth_id_def;
	$uc_id=$uc_id_def;
	$uc_name=$uc_name_def;
	$slogan=$slogan_def;
	$steps=$steps_def;

} else {

	if ( ! isset ($_POST['submitusecase']) ) {
		# we are in view|edit mode and not submitting form
		if ( ! isset ($_GET['uc_id']) ) 
			FatalError(105,'usecase id not found', __file__, __line__); 
		$uc_id=$_GET['uc_id'];
		$sql = "SELECT funcarea.name AS fa_name, funcarea.id as fa_id, usecase.name AS uc_name, usecase.id AS uc_id, slogan, steps, author.id as auth_id, author.signum as auth_signum FROM funcarea, usecase, author WHERE funcarea.id=usecase.fa_id AND author.id=usecase.auth_id AND usecase.id = " . $uc_id;
		$result = &$db->doQuery($sql);
		if( mysql_num_rows($result) == 0)
			FatalError(105,'usecase record not found', __file__, __line__);
		$row = mysql_fetch_object($result);
		$fa_name = $row->fa_name;
		$fa_id = $row->fa_id;
		$auth_signum = $row->auth_signum;
		$auth_id = $row->auth_id;
		$uc_name = $row->uc_name;
		$slogan = $row->slogan;
		$steps = $row->steps;
	}
		

}


$formerrors=0;
	
if ( isset ($_POST['submitusecase']) &&  $_POST['submitusecase'] == "$submitbtntxt" ) {
	# process the form
    	$uc_id = $_POST['uc_id'];
    	$fa_id = $_POST['fa_id'];
    	$auth_id = $_POST['auth_id'];
	$uc_name = trim($_POST['uc_name']);
	$uc_name_tokens = explode("_", $uc_name );
	$slogan = trim($_POST['slogan']);
	$steps = $_POST['steps'];

	
	if ( $_POST['fa_id'] == 0 ) {
		$formerrors++;
		$fa_id_err = "Please choose a functional area";
		$fa_id = $fa_id_def;
	} else {
    		$fa_id = $_POST['fa_id'];;
	}

	$fa_name = getFANameFromID(&$db, $fa_id);

	if ( $uc_name == '' ) {
		$formerrors++;
		$uc_name_err = "Please enter a name.";
	} else if ( strlen( $uc_name ) > 50 ) {
		$formerrors++;
		$uc_name_err="Usecase name cannot be more than 50 characters";
	} else if ( ! preg_match('/^[a-zA-Z0-9_]+$/', $uc_name ) ) {
		$formerrors++;
		$uc_name_err="Usecase can only be composed of alphanumeric and _ chars ";

	} else if ( $fa_name != "*unknown" && $uc_name_tokens[0] != $fa_name ) {
		$formerrors++;
		$uc_name_err="First part of usecase name must match selected functional area $fa_name";
	}
	if ( $slogan == '' ) {
		$formerrors++;
		$slogan_err = "Please enter a slogan.";
	}
	if ( $steps == '' ) {
		$formerrors++;
		$steps_err = "Please enter some steps.";
	}

	if ( $formerrors == 0 ) {

		# update the DB
		if ( $mode == "new" ) {
			$sql = "INSERT INTO usecase (fa_id, auth_id, name, slogan, steps) VALUES (" . $fa_id . "," . $auth_id . ",\"" . addslashes($uc_name) . "\",\"" . addslashes($slogan) . "\",\"" . addslashes($steps) . "\")";

				
		} else {
			# get old functional are and usecase name. If usecase name changes and there are
			# TCs associated with the usecase, a GIT move must be done on the TCs 
			$sql = "SELECT usecase.name AS uc_name, funcarea.name AS fa_name from usecase, funcarea WHERE usecase.id=$uc_id AND funcarea.id = usecase.fa_id";
			$result = &$db->doQuery($sql);
			if( mysql_num_rows($result) == 0)
				FatalError(105,'usecase record not found', __file__, __line__);
			$row = mysql_fetch_object($result);
			$old_uc_name=$row->uc_name;
			$old_fa_name=$row->fa_name;

			# update the DB
			$sql = "UPDATE usecase SET fa_id=" . $fa_id . ", auth_id=" . $auth_id . ", name=\"" . addslashes($uc_name) . "\", slogan=\"" . addslashes($slogan) . "\", steps=\"" . addslashes($steps) . "\" WHERE id=" . $uc_id;

		}
		if (!($result = &$db->doQuery($sql))) {
			$transaction_error=1;
			$transaction_errmsg="Failed to add/update usecase";
		}
		if ( $mode == "new" ) {
			$uc_id=mysql_insert_id();
		}
		# check if GIT move of TCs is needed
		if ( $mode == "edit" && ( $uc_name != $old_uc_name || $fa_name != $old_fa_name ) ) {
			if ( ! moveTCsInGit( &$db, $old_fa_name, $old_uc_name, $uc_id, $transaction_errmsg ) ) {
				$transaction_error=1;
				$transaction_errmsg.="<p>GIT move of usecase TCs failed";
			}
		}
		# if new tc and transaction OK reload form in edit mode
		if ( $transaction_error == 0 && $mode == "new" ) {
			$url=$_SERVER['PHP_SELF'] . "?mode=edit&uc_id=" . $uc_id . "&newrec=1";
			header("Location: $url");
		}
	}
}

?>
<script language="Javascript">
function toggleFields(mode)
{
        if ( mode == 'view' ) {
                disabled_state=true;
        } else {
                disabled_state=false;
        }
        document.usecaseform.fa_id.disabled=disabled_state;
        document.usecaseform.auth_id.disabled=disabled_state;
        document.usecaseform.uc_name.disabled=disabled_state;
        document.usecaseform.slogan.disabled=disabled_state;
        document.usecaseform.steps.disabled=disabled_state;
        document.usecaseform.submitusecase.disabled=disabled_state;
        return true;
}

/* function to enable fields before submit so
 they appear in POST info */
function enableAllFieldsBeforeSubmit()
{
        document.usecaseform.fa_id.disabled=false;
        document.usecaseform.auth_id.disabled=false;
        document.usecaseform.uc_name.disabled=false;
        document.usecaseform.slogan.disabled=false;
        document.usecaseform.steps.disabled=false;
        document.usecaseform.submitusecase.disabled=false;
}

function OnDeleteClick(uc_id)
{
	if (confirm("Are you sure you want to delete the usecase?" )) { 
		return true;
	} else {
		return false
	}
}


-->
</script>

<?php
# output the page
$java_onloadfunc="onload=\"toggleFields('" . $mode . "')\"";
include "header.php";

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness&gt;</a><a href=\"usecases.php\">Usecases&gt;</a>" . $mode . " usecase\n";

# check for and report form submission or transaction errors
if (  isset ($_POST['submitusecase']) ) {
	if ( $formerrors > 0 ) {
		echo "<p>NOTE: There are problems with the information provided as indicated in red below. Please correct the problems and click <strong>" . $submitbtntxt . "</strong>\n";
	} else {
		if ( $transaction_error == 1 ) {
			echo "<p><strong>$transaction_errmsg</strong></p>\n";
		
		} else {
			if ( $mode == "new" ) $action="added";
			if ( $mode == "edit" ) $action="updated";
			echo "<p><strong>Usecase $action successfully</strong></p>\n";
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
	echo "<p><strong>Usecase successfully added</strong>\n";
}

if ( $mode == "new" || $mode == "edit" )
	echo "<p><span class=\"fieldnote\">* = required field</span>\n";

		

if ( $mode == "edit") {
	$actionurl = $_SERVER['PHP_SELF'] . "?mode=edit&uc_id=" . $uc_id;
} else {
	$actionurl = $_SERVER['PHP_SELF'] . "?mode=new";
}
$form = new HtmlForm("usecaseform", '', $actionurl, 500, "onSubmit='enableAllFieldsBeforeSubmit()'");

$fld = new SelectList(&$db, '* Functional Area :', 'fa_id', array($fa_id), '', $fa_id_err, 0, 0, "");
$fld->SetDBOptions('funcarea', 'id', 'name', '', 'name', array('', 'Select'));
$form->AddField($fld);

$fld = new SelectList(&$db, '* Author :', 'auth_id', array($auth_id), '', $auth_id_err, 0, 0, "");
$fld->SetDBOptions('author', 'id', 'signum', '', 'signum', array('', 'Select'));
$form->AddField($fld);
		
$fld = new TextField('* Name:', 'uc_name', 'text', stripslashes($uc_name), '&lt;FA&gt;_&lt;OBJECT&gt;_&lt;ACTION&gt; e.g. LDAP_USER_ADD. Max 50 chars. No spaces allowed.', 55, 64, $uc_name_err) ;
$form->AddField($fld);
	
$fld = new TextField('* Slogan:', 'slogan', 'text', stripslashes($slogan), 'e.g. Add a user to LDAP', 100, 100, $slogan_err) ;
$form->AddField($fld);
	
$fld = new TextArea('* Steps:', 'steps', stripslashes($steps), '', 20, 64, $steps_err);
$form->AddField($fld);

$fld = new Hidden('uc_id', $uc_id);
$form->AddHidden($fld);
	
$fld = new Button('submitusecase', $submitbtntxt,'submit');
$form->AddButton($fld);
	
$form->Draw();

if ( $mode == "new" || $mode == "edit" ) {
	echo "<p><a href=./" . $_SERVER['REQUEST_URI'] . ">Discard Edits</a>\n";
	echo "<p><a href=./testcase.php?mode=new&fa_id=$fa_id&uc_id=$uc_id>New testcase</a>\n";
}

if ( $mode != "new" ) {
	echo "<p><a href=./testcases.php?fa_id=$fa_id&uc_id=$uc_id>List testcases</a>\n";
	echo "<p><a href=./" . $_SERVER['PHP_SELF'] . "?mode=new>Create new usecase</a>\n";
}
if ( $mode == "edit" )
	echo "<p><a href=./usecase_delete.php?uc_id=$uc_id onclick=\" return(OnDeleteClick($uc_id))\">Delete this usecase</a>\n";

include "harness_footer.php"
?>







