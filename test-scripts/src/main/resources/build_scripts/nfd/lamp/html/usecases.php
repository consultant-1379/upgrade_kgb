<?php
#
# script to display usecases
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
	document.usecaseform.action = "usecase.php?mode=new"
	document.usecaseform.submit();             // Submit the page
	return true;
}

function OnViewButton()
{
	if ( document.usecaseform.uc_id.value == 0 ) {
		alert("Please select a usecase");
		return false;
	}
	document.usecaseform.action = "usecase.php?mode=view&uc_id=" +  document.usecaseform.uc_id.value;
	document.usecaseform.submit();             // Submit the page
	return true;
}

function OnEditButton()
{
	if ( document.usecaseform.uc_id.value == 0 ) {
		alert("Please select a usecase");
		return false;
	}
	document.usecaseform.action = "usecase.php?mode=edit&uc_id=" +  document.usecaseform.uc_id.value;
	document.usecaseform.submit();             // Submit the page

	return true;
}

function OnDeleteButton()
{
	if ( document.usecaseform.uc_id.value == 0 ) {
		alert("Please select a usecase");
		return false;
	}
	if (confirm("Are you sure you want to delete the usecase?" )) { 
		document.usecaseform.action = "usecase_delete.php?uc_id=" +  document.usecaseform.uc_id.value;
		document.usecaseform.submit();             // Submit the page
	} else {
		return false
	}
	return true;
}

function OnTCsButton()
{
	if ( document.usecaseform.uc_id.value == 0 ) {
		alert("Please select a usecase");
		return false;
	}
	document.usecaseform.action = "testcases.php?fa_id=" + document.usecasesform.fa_id.value + "&uc_id=" +  document.usecaseform.uc_id.value;
	document.usecaseform.submit();             // Submit the page
	return true;
}
-->
</script>
<noscript>You need Javascript enabled for this to work</noscript>
<?php


if ( isset( $_POST['fa_id'] ) ) {
	$fa_id=$_POST['fa_id'];
} else if ( isset ($_GET['fa_id'])  ) {
	$fa_id=$_GET['fa_id'];
} else {
	$fa_id=0;
}

if ( isset( $_POST['uc_id'] ) ) {
	$uc_id=$_POST['uc_id'];
} else if ( isset ($_GET['uc_id'])  ) {
	$uc_id=$_GET['uc_id'];
} else {
	$uc_id=0;
}

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=\"harness.php\">Harness&gt;</a>Usecases\n";


$form = new HtmlForm("usecasesform", '', $_SERVER['PHP_SELF'], "maxw");
$fld = new SelectList(&$db, 'Functional Area :', 'fa_id', array($fa_id), '', 0, 0,"", "document.usecasesform.submit()" );
$fld->SetDBOptions('funcarea', 'id', 'name', '', 'name', array(0, 'All'));
$form->AddField($fld);
$form->Draw();

$sql = "SELECT name, id FROM usecase";
if ( $fa_id != 0 )
	$sql .= " WHERE fa_id=" . $fa_id;
$sql .= " ORDER BY name";
$result = &$db->doQuery($sql);

$usecase_names=array();
$usecase_ids=array();

if (mysql_num_rows($result) > 0 ) {
	while ( ($row =  mysql_fetch_object($result)) ) {
		$id = $row->id;
		$name=$row->name;
		array_push($usecase_ids, $id);
		array_push($usecase_names, $name);
	}
} 


$form = new HtmlForm("usecaseform", '', "", "maxwh");
$fld_name = "Usecases (". count($usecase_ids) . ")";
$fld = new SelectList(&$db, $fld_name, 'uc_id', array($uc_id), '', "", 10, 0, "", "", "style=\"width:100%; height:100%\"", "OnEditButton()");
$fld->AddOptions($usecase_names, $usecase_ids);
$form->AddField($fld);

$fld = new Button('new', 'New', 'submit', '', 'return OnNewButton();');
$form->AddButton($fld);
$fld = new Button('view', 'View','submit', '', 'return OnViewButton();');
$form->AddButton($fld);
$fld = new Button('edit', 'Edit','submit', '', 'return OnEditButton();');
$form->AddButton($fld);
$fld = new Button('delete', 'Delete','submit', '', 'return OnDeleteButton();');
$form->AddButton($fld);
$fld = new Button('tcs', 'TCs','submit', '', 'return OnTCsButton();');
$form->AddButton($fld);

$form->Draw();


include "harness_footer.php"
?>
