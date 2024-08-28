<?php
include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;
include "header.php";
echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;Harness\n";
?>



<p><a href="authors.php">Authors</a>
<p><a href="fas.php">Functional Areas</a>
<p><a href="usecases.php">Usecases</a>
<p><a href="testcases.php">Testcases</a>
<p><a href="testsuites.php">Test Suites</a>
<p><a href="reports.php">Reports</a>
<p><a href="ossenvfiles.php">OSS Environment Configuration</a>

<?php
include "footer.php"
?>
