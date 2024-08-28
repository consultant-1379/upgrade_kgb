<?php
#
# script to display test suites
#
#	May 2012 edavmax
include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;
include_once "inc/chtmlform.inc";

include "header.php";
function sprintf_nbsp() {
	$args = func_get_args();
	return str_replace(' ', '&nbsp;', vsprintf(array_shift($args), array_values($args)));
}

?>
<script language="Javascript">
<!--
function OnNewButton()
{
	document.testsuiteform.action = "testsuite.php?mode=new"
	document.testsuiteform.submit();             // Submit the page
	return true;
}

function OnViewButton()
{
	if ( document.testsuiteform.ts_id.value == 0 ) {
		alert("Please select a test suite");
		return false;
	}
	document.testsuiteform.action = "testsuite.php?mode=view&ts_id=" +  document.testsuiteform.ts_id.value;
	document.testsuiteform.submit();             // Submit the page
	return true;
}

function OnEditButton()
{
	if ( document.testsuiteform.ts_id.value == 0 ) {
		alert("Please select a test suite");
		return false;
	}
	document.testsuiteform.action = "testsuite.php?mode=edit&ts_id=" +  document.testsuiteform.ts_id.value;
	document.testsuiteform.submit();             // Submit the page

	return true;
}

function OnDeleteButton()
{
	if ( document.testsuiteform.ts_id.value == 0 ) {
		alert("Please select a test suite");
		return false;
	}
	if (confirm("Are you sure you want to delete the test suite?" )) { 
		document.testsuiteform.action = "testsuite_delete.php?ts_id=" +  document.testsuiteform.ts_id.value;
		document.testsuiteform.submit();             // Submit the page
	} else {
		return false
	}
	return true;
}

function OnTCsButton()
{
	if ( document.testsuiteform.ts_id.value == 0 ) {
		alert("Please select a test suite");
		return false;
	}
	document.testsuiteform.action = "testcases.php?fa_id=" + document.testsuitesform.fa_id.value + "&ts_id=" +  document.testsuiteform.ts_id.value;
	document.testsuiteform.submit();             // Submit the page
	return true;
}
-->
</script>
<noscript>You need Javascript enabled for this to work</noscript>
<?php


if ( isset( $_POST['ts_id'] ) ) {
	$ts_id=$_POST['ts_id'];
} else if ( isset ($_GET['ts_id'])  ) {
	$ts_id=$_GET['ts_id'];
} else {
	$ts_id=0;
}

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=\"harness.php\">Harness&gt;</a>Test suites\n";

$sql = "SELECT name, id FROM testsuite ORDER BY name";
$result = &$db->doQuery($sql);

$testsuite_names=array();
$testsuite_ids=array();

if (mysql_num_rows($result) > 0 ) {
	while ( ($row =  mysql_fetch_object($result)) ) {
		$id = $row->id;
		$name=$row->name;
		array_push($testsuite_ids, $id);
		array_push($testsuite_names, $name);
	}
}


$form = new HtmlForm("testsuiteform", '', "", "maxwh");
$fld_name = "Test suites (". count($testsuite_ids) . ")";
$fld = new SelectList(&$db, $fld_name, 'ts_id', array($ts_id), '', "", 10, 0, "", "", "style=\"width:100%; height:100%\"", "OnEditButton()");
$fld->AddOptions($testsuite_names, $testsuite_ids);
$form->AddField($fld);

$fld = new Button('new', 'New', 'submit', '', 'return OnNewButton();');
$form->AddButton($fld);
$fld = new Button('view', 'View','submit', '', 'return OnViewButton();');
$form->AddButton($fld);
$fld = new Button('edit', 'Edit','submit', '', 'return OnEditButton();');
$form->AddButton($fld);
$fld = new Button('delete', 'Delete','submit', '', 'return OnDeleteButton();');
$form->AddButton($fld);

$form->Draw();


include "harness_footer.php"
?>
