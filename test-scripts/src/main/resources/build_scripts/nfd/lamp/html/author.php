<?php
#
# script to display and process author form
#
#	Apr 2012 edavmax
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


$auth_id_def=0;
$firstname_def="";
$lastname_def="";
$signum_def="";



if ( ! isset($_GET['mode']) ) 
	$mode="new";
else
	$mode=$_GET['mode'];



if ( $mode == "new" ) {
	
	$auth_id=$auth_id_def;
	$firstname=$firstname_def;
	$lastname=$lastname_def;
	$signum=$signum_def;

} else {

	if ( ! isset ($_POST['submitauthor']) ) {
		# we are in view|edit mode and not submitting form
		if ( ! isset ($_GET['auth_id']) ) 
			FatalError(105,'author id not found', __file__, __line__); 
		$auth_id=$_GET['auth_id'];
		$sql = "SELECT * from author WHERE id=". $auth_id;
		$result = &$db->doQuery($sql);
		if( mysql_num_rows($result) == 0)
			FatalError(105,'author record not found', __file__, __line__);
		$row = mysql_fetch_object($result);
		$firstname = $row->firstname;
		$lastname = $row->lastname;
		$signum = $row->signum;
	}
		

}


$formerrors=0;
	
if ( isset ($_POST['submitauthor']) &&  $_POST['submitauthor'] == "$submitbtntxt" ) {
	# process the form
    	$auth_id = $_POST['auth_id'];
	$firstname = trim($_POST['firstname']);
	$lastname = trim($_POST['lastname']);
	$signum = trim($_POST['signum']);

	
	if ( $firstname == '' ) {
		$formerrors++;
		$firstname_err = "Please enter a first name.";
	} 

	if ( $lastname == '' ) {
		$formerrors++;
		$lastname_err = "Please enter a last name.";
	} 

	if ( $signum == '' ) {
		$formerrors++;
		$signum_err = "Please enter a signum.";
	} else if ( ! preg_match('/^[a-zA-Z0-9_]+$/', $signum ) ) {
		$formerrors++;
		$signum_err="Signum can only be composed of alphanumeric and _ chars ";
	}

	if ( $formerrors == 0 ) {

		# update the DB
		if ( $mode == "new" ) {
			$sql = "INSERT INTO author (firstname, lastname, signum) VALUES (\"" . addslashes($firstname) . "\",\"" . addslashes($lastname) . "\",\"" . addslashes($signum) . "\")";

				
		} else {

			# update the DB
			$sql = "UPDATE author SET firstname=\"" . addslashes($firstname) . "\", lastname=\"" . addslashes($lastname) . "\", signum=\"" . addslashes($signum) . "\" WHERE id=" . $auth_id;

		}
		if (!($result = &$db->doQuery($sql))) {
			$transaction_error=1;
			$transaction_errmsg="Failed to add/update author";
		}
		if ( $mode == "new" ) {
			$auth_id=mysql_insert_id();
		}
		# if new tc and transaction OK reload form in edit mode
		if ( $transaction_error == 0 && $mode == "new" ) {
			$url=$_SERVER['PHP_SELF'] . "?mode=edit&auth_id=" . $auth_id . "&newrec=1";
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
        document.authorform.firstname.disabled=disabled_state;
        document.authorform.lastname.disabled=disabled_state;
        document.authorform.signum.disabled=disabled_state;
        return true;
}

/* function to enable fields before submit so
 they appear in POST info */
function enableAllFieldsBeforeSubmit()
{
        document.authorform.firstname.disabled=false;
        document.authorform.lastname.disabled=false;
        document.authorform.signum.disabled=false;
}

-->
</script>

<?php
# output the page
$java_onloadfunc="onload=\"toggleFields('" . $mode . "')\"";
include "header.php";

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness&gt;</a><a href=\"authors.php\">Authors&gt;</a>" . $mode . " author\n";

# check for and report form submission or transaction errors
if (  isset ($_POST['submitauthor']) ) {
	if ( $formerrors > 0 ) {
		echo "<p>NOTE: There are problems with the information provided as indicated in red below. Please correct the problems and click <strong>" . $submitbtntxt . "</strong>\n";
	} else {
		if ( $transaction_error == 1 ) {
			echo "<p><strong>$transaction_errmsg</strong></p>\n";
		
		} else {
			if ( $mode == "new" ) $action="added";
			if ( $mode == "edit" ) $action="updated";
			echo "<p><strong>Author $action successfully</strong></p>\n";
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
	echo "<p><strong>Author successfully added</strong>\n";
}

if ( $mode == "new" || $mode == "edit" )
	echo "<p><span class=\"fieldnote\">* = required field</span>\n";

		

if ( $mode == "edit") {
	$actionurl = $_SERVER['PHP_SELF'] . "?mode=edit&auth_id=" . $auth_id;
} else {
	$actionurl = $_SERVER['PHP_SELF'] . "?mode=new";
}
$form = new HtmlForm("authorform", '', $actionurl, 'maxw', "onSubmit='enableAllFieldsBeforeSubmit()'");

$fld = new TextField('* First Name:', 'firstname', 'text', stripslashes($firstname), '', 55, 64, $firstname_err) ;
$form->AddField($fld);
	
$fld = new TextField('* Last Name:', 'lastname', 'text', stripslashes($lastname), '', 55, 64, $lastname_err) ;
$form->AddField($fld);

$fld = new TextField('* Signum:', 'signum', 'text', stripslashes($signum), '', 55, 64, $signum_err) ;
$form->AddField($fld);

$fld = new Hidden('auth_id', $auth_id);
$form->AddHidden($fld);
	
$fld = new Button('submitauthor', $submitbtntxt,'submit');
$form->AddButton($fld);
	
$form->Draw();

if ( $mode == "new" || $mode == "edit" ) {
	echo "<p><a href=./" . $_SERVER['REQUEST_URI'] . ">Discard Edits</a>\n";
}

if ( $mode != "new" ) {
	echo "<p><a href=./" . $_SERVER['PHP_SELF'] . "?mode=new>Create new author</a>\n";
}
#if ( $mode == "edit" )
#echo "<p><a href=./author_delete.php?uc_id=$uc_id onclick=\" return(OnDeleteClick($uc_id))\">Delete this author</a>\n";

include "harness_footer.php"
?>







