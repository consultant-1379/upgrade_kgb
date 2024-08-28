<?php
#
# script to display FAs
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
	document.faform.action = "fa.php?mode=new"
	document.faform.submit();             // Submit the page
	return true;
}

function OnViewButton()
{
	if ( document.faform.fa_id.value == 0 ) {
		alert("Please select a functional area");
		return false;
	}
	document.faform.action = "fa.php?mode=view&fa_id=" +  document.faform.fa_id.value;
	document.faform.submit();             // Submit the page
	return true;
}

function OnEditButton()
{
	if ( document.faform.fa_id.value == 0 ) {
		alert("Please select a functional area");
		return false;
	}
	document.faform.action = "fa.php?mode=edit&fa_id=" +  document.faform.fa_id.value;
	document.faform.submit();             // Submit the page

	return true;
}

function OnDeleteButton()
{
	if ( document.faform.fa_id.value == 0 ) {
		alert("Please select a functional area");
		return false;
	}
	if (confirm("Are you sure you want to delete the functional area?" )) { 
		document.faform.action = "fa_delete.php?fa_id=" +  document.faform.fa_id.value;
		document.faform.submit();             // Submit the page
	} else {
		return false
	}
	return true;
}

-->
</script>
<noscript>You need Javascript enabled for this to work</noscript>
<?php


echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=\"harness.php\">Harness&gt;</a>Functional Areas\n";

$fa_id=0;
$sql = "SELECT * FROM funcarea ORDER by name";
$result = &$db->doQuery($sql);

$fa_names=array();
$fa_ids=array();

if (mysql_num_rows($result) > 0 ) {
	while ( ($row =  mysql_fetch_object($result)) ) {
		$id = $row->id;
		$name=$row->name;
		array_push($fa_ids, $id);
		array_push($fa_names, $name);
	}
} 


$form = new HtmlForm("faform", '', "", "maxw");
$fld_name = "Functional Areas (". count($fa_ids) . ")";
$fld = new SelectList(&$db, $fld_name, 'fa_id', array($fa_id), '', "", 30, 0, "", "", "style=\"width:50%; height:100%\"", "OnEditButton()");
$fld->AddOptions($fa_names, $fa_ids);
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
