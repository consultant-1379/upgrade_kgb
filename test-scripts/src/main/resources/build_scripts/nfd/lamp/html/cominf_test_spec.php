<?php
#
# script to display cominf test spec
#
#	Apr 2012 edavmax
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;

include "header.php";

echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=reports.php>Reports&gt;</a>Cominf Test Spec\n";

$sql="select funcarea.name as fa_name,usecase.name as uc_name,testcase.slogan, testcase.id as tc_id, testcase.slogan as tc_slogan, testcase.polarity, testcase.type, testcase.automated, testcase.preconditions, testcase.manual_steps, testcase.postconditions from funcarea,usecase,testcase where funcarea.id=usecase.fa_id and  testcase.uc_id=usecase.id and funcarea.id in (1,2,3,4,5,7)  order by funcarea.name";
$result = &$db->doQuery($sql);
$exist_fa="";
$exist_uc="";
if (mysql_num_rows($result) > 0 ) {
	while ( ($row =  mysql_fetch_object($result)) ) {
		if ( $exist_fa != $row->fa_name ) {
			echo "<h1>$row->fa_name</h1>\n";
			$exist_fa=$row->fa_name;
		}
		if ( $exist_uc != $row->uc_name ) {
			echo "<h2>$row->uc_name</h2>\n";
			$exist_uc=$row->uc_name;
		}
		echo "<p><strong>TC Slogan: </strong>$row->tc_slogan</strong></p>\n";
		echo "<p><strong>TC ID: </strong>$row->tc_id</strong></p>\n";
		echo "<p><strong>TC Polarity: </strong>$row->polarity</strong></p>\n";
		echo "<p><strong>TC Type: </strong>$row->type</strong></p>\n";
		if ( $row->automated == 0 ) {
			$auto_text="no";
		} else { 
			$auto_text="yes";
		}
		echo "<p><strong>TC Automated: </strong>$auto_text</strong></p>\n";
		echo "<p><strong>Pre conditions:</strong>\n";
		echo "<p><pre>$row->preconditions</pre>\n";
		echo "<p><strong>Manual Steps:</strong>\n";
		echo "<p><pre>$row->manual_steps</pre>\n";
		echo "<p><strong>Post conditions:</strong>\n";
		echo "<p><pre>$row->postconditions</pre>\n";
		echo "<hr>\n";
		
	}
}



include "harness_footer.php"
?>







