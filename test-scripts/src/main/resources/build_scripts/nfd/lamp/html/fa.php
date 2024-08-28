<?php
#
# script to display and process FA form
#
#	Apr 2012 edavmax
include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;

$submitbtntxt="Save";
$name_err="";
$transaction_error=0;


$fa_id_def=0;
$name_def="";



if ( ! isset($_GET['mode']) ) 
	$mode="new";
else
	$mode=$_GET['mode'];



if ( $mode == "new" ) {
	
	$fa_id=$fa_id_def;
	$name=$name_def;

} else {

	if ( ! isset ($_POST['submitfa']) ) {
		# we are in view|edit mode and not submitting form
		if ( ! isset ($_GET['fa_id']) ) 
			FatalError(105,'FA id not found', __file__, __line__); 
		$fa_id=$_GET['fa_id'];
		$sql = "SELECT * from funcarea WHERE id=". $fa_id;
		$result = &$db->doQuery($sql);
		if( mysql_num_rows($result) == 0)
			FatalError(105,'FA record not found', __file__, __line__);
		$row = mysql_fetch_object($result);
		$name = $row->name;
	}
		

}


$formerrors=0;
	
if ( isset ($_POST['submitfa']) &&  $_POST['submitfa'] == "$submitbtntxt" ) {
	# process the form
    	$fa_id = $_POST['fa_id'];
	$name = trim($_POST['name']);

	
	if ( $name == '' ) {
		$formerrors++;
		$name_err = "Please enter a first name.";
	} else if ( ! preg_match('/^[a-zA-Z0-9_]+$/', $name ) ) {
		$formerrors++;
		$name_err="First name can only be composed of alphanumeric and _ chars ";
	}

	if ( $formerrors == 0 ) {

		# update the DB
		if ( $mode == "new" ) {
			$sql = "INSERT INTO funcarea (name) VALUES (\"" . addslashes($name) . "\")";

				
		} else {

			# update the DB
			$sql = "UPDATE funcarea SET name=\"" . addslashes($name) . "\" WHERE id=" . $fa_id;

		}
		if (!($result = &$db->doQuery($sql))) {
			$transaction_error=1;
			$transaction_errmsg="Failed to add/update functional area";
		}
		if ( $mode == "new" ) {
			$fa_id=mysql_insert_id();
		}
		# if new tc and transaction OK reload form in edit mode
		if ( $transaction_error == 0 && $mode == "new" ) {
			$url=$_SERVER['PHP_SELF'] . "?mode=edit&fa_id=" . $fa_id . "&newrec=1";
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
        document.faform.name.disabled=disabled_state;
        return true;
}

/* function to enable fields before submit so
 they appear in POST info */
function enableAllFieldsBeforeSubmit()
{
        document.faform.name.disabled=false;
}

-->
</script>

<?php
# output the page
$java_onloadfunc="onload=\"toggleFields('" . $mode . "')\"";
include "header.php";

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness&gt;</a><a href=\"fas.php\">Functional Areas&gt;</a>" . $mode . " functional area\n";

# check for and report form submission or transaction errors
if (  isset ($_POST['submitfa']) ) {
	if ( $formerrors > 0 ) {
		echo "<p>NOTE: There are problems with the information provided as indicated in red below. Please correct the problems and click <strong>" . $submitbtntxt . "</strong>\n";
	} else {
		if ( $transaction_error == 1 ) {
			echo "<p><strong>$transaction_errmsg</strong></p>\n";
		
		} else {
			if ( $mode == "new" ) $action="added";
			if ( $mode == "edit" ) $action="updated";
			echo "<p><strong>Functional Area $action successfully</strong></p>\n";
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
	echo "<p><strong>Functional Area successfully added</strong>\n";
}

if ( $mode == "new" || $mode == "edit" )
	echo "<p><span class=\"fieldnote\">* = required field</span>\n";

		

if ( $mode == "edit") {
	$actionurl = $_SERVER['PHP_SELF'] . "?mode=edit&fa_id=" . $fa_id;
} else {
	$actionurl = $_SERVER['PHP_SELF'] . "?mode=new";
}
$form = new HtmlForm("faform", '', $actionurl, 'maxw', "onSubmit='enableAllFieldsBeforeSubmit()'");

$fld = new TextField('* Name:', 'name', 'text', stripslashes($name), '', 55, 64, $name_err) ;
$form->AddField($fld);
	
$fld = new Hidden('fa_id', $fa_id);
$form->AddHidden($fld);
	
$fld = new Button('submitfa', $submitbtntxt,'submit');
$form->AddButton($fld);
	
$form->Draw();

if ( $mode == "new" || $mode == "edit" ) {
	echo "<p><a href=./" . $_SERVER['REQUEST_URI'] . ">Discard Edits</a>\n";
}

if ( $mode != "new" ) {
	echo "<p><a href=./" . $_SERVER['PHP_SELF'] . "?mode=new>Create new functional area</a>\n";
}
#if ( $mode == "edit" )
#echo "<p><a href=./author_delete.php?uc_id=$uc_id onclick=\" return(OnDeleteClick($uc_id))\">Delete this author</a>\n";

include "harness_footer.php"
?>







