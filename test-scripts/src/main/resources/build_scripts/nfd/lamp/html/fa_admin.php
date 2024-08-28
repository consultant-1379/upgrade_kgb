<?php
#
# script to admin FAs
#
#	Apr 2012 edavmax
include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;
include_once "inc/chtmlform.inc";
include "header.php";


echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=\"harness.php\">Harness&gt;</a>Functional Areas\n";

$auth_id=0;
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

$form->Draw();


include "harness_footer.php"
?>
