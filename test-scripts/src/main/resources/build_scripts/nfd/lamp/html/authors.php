<?php
#
# script to display authors
#
#	Apr 2012 edavmax
include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;
include_once "inc/chtmlform.inc";
include "header.php";
?>
<script language="Javascript">
<!--
function OnNewButton()
{
	document.authorform.action = "author.php?mode=new"
	document.authorform.submit();             // Submit the page
	return true;
}

function OnViewButton()
{
	if ( document.authorform.auth_id.value == 0 ) {
		alert("Please select an author");
		return false;
	}
	document.authorform.action = "author.php?mode=view&auth_id=" +  document.authorform.auth_id.value;
	document.authorform.submit();             // Submit the page
	return true;
}

function OnEditButton()
{
	if ( document.authorform.auth_id.value == 0 ) {
		alert("Please select an author");
		return false;
	}
	document.authorform.action = "author.php?mode=edit&auth_id=" +  document.authorform.auth_id.value;
	document.authorform.submit();             // Submit the page

	return true;
}

function OnDeleteButton()
{
	if ( document.authorform.auth_id.value == 0 ) {
		alert("Please select an author");
		return false;
	}
	if (confirm("Are you sure you want to delete the author?" )) { 
		document.authorform.action = "author_delete.php?auth_id=" +  document.authorform.auth_id.value;
		document.authorform.submit();             // Submit the page
	} else {
		return false
	}
	return true;
}

-->
</script>
<noscript>You need Javascript enabled for this to work</noscript>
<?php


echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=\"harness.php\">Harness&gt;</a>Authors\n";

$auth_id=0;
$sql = "SELECT * FROM author ORDER by lastname";
$result = &$db->doQuery($sql);

$auth_names=array();
$auth_ids=array();

if (mysql_num_rows($result) > 0 ) {
	while ( ($row =  mysql_fetch_object($result)) ) {
		$id = $row->id;
		$fn=$row->firstname;
		$ln=$row->lastname;
		$signum=$row->signum;
		array_push($auth_ids, $id);
		array_push($auth_names, $fn . " " . $ln . " (" . $signum . ")");
	}
} 


$form = new HtmlForm("authorform", '', "", "maxw");
$fld_name = "Authors (". count($auth_names) . ")";
$fld = new SelectList(&$db, $fld_name, 'auth_id', array($auth_id), '', "", 30, 0, "", "", "style=\"width:50%; height:100%\"", "OnEditButton()");
$fld->AddOptions($auth_names, $auth_ids);
$form->AddField($fld);

$fld = new Button('new', 'New', 'submit', '', 'return OnNewButton();');
$form->AddButton($fld);
$fld = new Button('view', 'View','submit', '', 'return OnViewButton();');
$form->AddButton($fld);
$fld = new Button('edit', 'Edit','submit', '', 'return OnEditButton();');
$form->AddButton($fld);
$fld = new Button('delete', 'Delete','submit', '', 'return OnDeleteButton();');
$form->AddButton($fld);
#$fld = new Button('tcs', 'TCs','submit', '', 'return OnTCsButton();');
#$form->AddButton($fld);

$form->Draw();


include "harness_footer.php"
?>
