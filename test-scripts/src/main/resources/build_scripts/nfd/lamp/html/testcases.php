<?php
#
# script to display testcases
#
#	Apr 2012 edavmax
#
# The "refresh" variable informs the page which of the dropdowns was selected:
# refresh=uc_tc means the functional area dropdown was selected
# refresh=tc means the usecase dropdown was selected
#
#
include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;
include "header.php";

function sprintf_nbsp() {
	$args = func_get_args();
	return str_replace(' ', '&nbsp;', vsprintf(array_shift($args), array_values($args)));
}


?>
<script language="Javascript">
<!--
function OnNewButton(fa_id, uc_id) 
{
	document.testcaseform.action = "testcase.php?mode=new&fa_id=" + fa_id + "&uc_id=" + uc_id;
	document.testcaseform.submit();             // Submit the page
	return true;
}

function OnViewButton()
{
	if ( document.testcaseform.tc_id.value == 0 ) {
		alert("Please select a testcase");
		return false;
	}
	document.testcaseform.action = "testcase.php?mode=view&tc_id=" +  document.testcaseform.tc_id.value;
	document.testcaseform.submit();             // Submit the page
	return true;
}

function OnEditButton()
{
	if ( document.testcaseform.tc_id.value == 0 ) {
		alert("Please select a testcase");
		return false;
	}
	document.testcaseform.action = "testcase.php?mode=edit&tc_id=" +  document.testcaseform.tc_id.value;
	document.testcaseform.submit();             // Submit the page
	return true;
}

function OnDeleteButton()
{
	if ( document.testcaseform.tc_id.value == 0 ) {
		alert("Please select a testcase");
		return false;
	}
	if (confirm("Are you sure you want to delete the testcase?" )) { 
		document.testcaseform.action = "testcase_delete.php?tc_id=" +  document.testcaseform.tc_id.value;
		document.testcaseform.submit();             // Submit the page
	} else {
		return false
	}
	return true;
}

function OnFASelect()
{
        document.testcasesform.action = "testcases.php?refresh=uc_tc";
        document.testcasesform.submit();             // Submit the page
        return true;
}

function OnUCSelect()
{
        document.testcasesform.action = "testcases.php?refresh=tc";
        document.testcasesform.submit();             // Submit the page
        return true;
}


-->
</script>
<noscript>You need Javascript enabled for this to work</noscript>
<?php

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=harness.php>Harness&gt;</a><a href=\"usecases.php\">Usecases&gt;</a>Testcases\n";

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

if ( isset( $_GET['refresh'] ) ) {
	$refresh=$_GET['refresh'];
} else {
	$refresh="tc";

}

if ( $uc_id != 0 ) {
	# if uc_id is set and func area id not set get fa_id for this uc_id
	if ( $fa_id == 0 ) {
		$sql = "SELECT fa_id FROM usecase WHERE id=" . $uc_id;
		if ( ($result = &$db->doQuery($sql)) ) {
			$row =  mysql_fetch_object($result);
			$fa_id=$row->fa_id;
		}
	}
}
$form = new HtmlForm("testcasesform", '', $_SERVER['PHP_SELF'], "maxw");
$fld = new SelectList(&$db, 'Functional Area :', 'fa_id', array($fa_id), '', 0, 0, "", "OnFASelect()");
$fld->SetDBOptions('funcarea', 'id', 'name', '', 'name', array(0, 'All'));
$form->AddField($fld);

# get usecases for selected FA
$usecase_names=array("All");
$usecase_ids=array(0);

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
	# only set the usecase  if user changes functional area 
	if ( $refresh == "uc_tc" )
		$uc_id=$usecase_ids[0];
} 


$fld = new SelectList(&$db, 'Usecase :', 'uc_id', array($uc_id), '', 0, 0, "", "OnUCSelect()");
$fld->AddOptions($usecase_names, $usecase_ids);
$form->AddField($fld);

$form->Draw();

# get testcases for selected FA and UC
$sql = "SELECT slogan, id FROM testcase";

if ( $fa_id != 0 ) {
	if ( $uc_id != 0 ) {
		# get all testcases for selected usecase
		$sql .= " WHERE uc_id=" . $uc_id;
	} else {
		# get all testcases for functional area
		$instr="(";
		foreach ( $usecase_ids as $uid ) {
			$instr .= "$uid,";
			
		}
		$instr = substr($instr, 0, -1);
		$instr .= ")";
		$sql .= " WHERE uc_id in " . $instr;
	}
}
$sql .= " ORDER BY id";
$result = &$db->doQuery($sql);
#echo $fa_id . "--" . $uc_id . "--" . $refresh;
#echo $sql;

$testcase_names=array();
$testcase_ids=array();

if (mysql_num_rows($result) > 0 ) {
	while ( ($row =  mysql_fetch_object($result)) ) {
		$id = $row->id;
		$slogan=$row->slogan;
		array_push($testcase_ids, $id);
		array_push($testcase_names, sprintf_nbsp("%3s%06d %s", "ID=", $id, $slogan));
	}
} 

$form = new HtmlForm("testcaseform", '', "", "maxwh");
$fld_name="Testcases (" . count($testcase_ids) . ")";

if ( isset( $testcase_ids[0] ) ) {
	$sel_id= $testcase_ids[0];
} else {
	$sel_id= 0;
}
$fld = new SelectList(&$db, $fld_name, 'tc_id', array($sel_id), '', "", 10, 0, "", "", "style=\"width: 100%; height: 100%\"", "OnEditButton()");
$fld->AddOptions($testcase_names, $testcase_ids);
$form->AddField($fld);

$fld = new Button('new', 'New', 'submit', '', "return OnNewButton($fa_id, $uc_id);");
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
