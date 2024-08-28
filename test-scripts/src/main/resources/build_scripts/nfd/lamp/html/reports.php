<?php
include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;
include "header.php";
echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;Reports\n";
?>



<p><a href="cominf_test_spec.php">Cominf Test Spec</a>

<?php
include "footer.php"
?>
