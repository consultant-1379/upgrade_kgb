<?php
#
# script to display OSS Environment files
#
#	Dec 2012 eeidle
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
	document.envfileform.action = "ossCreateEnvironmentFile.php?mode=new"
	document.envfileform.submit();             // Submit the page
	return true;
}

function OnViewButton()
{
	if ( document.envfileform.file_name.value == "" ) {
		alert("Please select an envfile");
		return false;
	}
	document.envfileform.action = "ossenvfile.php?mode=view&file_name=/var/lib/jenkins/cominf_build/build_scripts/nfd/etc/" +  document.envfileform.file_name.value;
	//document.write(document.envfileform.file_name);
	document.envfileform.submit();             // Submit the page
	return true;
}

function OnEditButton()
{

	if ( document.envfileform.file_name.value == "" ) {
		alert("Please select an envfile");
		return false;
	}

	document.envfileform.action = "phpFileManager-0.9.7/index.php?mode=edit";
	document.envfileform.submit();             // Submit the page

	return true;
}

function OnDeleteButton()
{
	if ( document.envfileform.file_id.value == 0 ) {
		alert("Please select an envfile");
		return false;
	}
	if (confirm("Are you sure you want to delete the envfile?" )) { 
		document.envfileform.action = "envfile_delete.php?file_id=" +  document.envfileform.file_id.value;
		document.envfileform.submit();             // Submit the page
	} else {
		return false
	}
	return true;
}

-->
</script>
<noscript>You need Javascript enabled for this to work</noscript>
<?php
  function getDirectoryList ($directory) 
  {

    // create an array to hold directory list
    $results = array();

    // create a handler for the directory
    $handler = opendir($directory);

    // open directory and walk through the filenames
    while ($file = readdir($handler)) {

      // if file isn't this directory or its parent, add it to the results
      if ($file != "." && $file != "..") {
        $results[] = $file;
      }

    }

    // tidy up: close the handler
    closedir($handler);

    // done!
    return $results;

  }
/* FUNCTION: showDir
 * DESCRIPTION: Creates a list options from all files, folders, and recursivly
 *     found files and subfolders. Echos all the options as they are retrieved
 * EXAMPLE: showDir(".") */
function showDir( $dir , $subdir = 0 ) {
    if ( !is_dir( $dir ) ) { return false; }

    $scan = scandir( $dir );

    foreach( $scan as $key => $val ) {
        if ( $val[0] == "." ) { continue; }

        if ( is_dir( $dir . "/" . $val ) ) {
            echo "<option>" . str_repeat( "--", $subdir ) . $val . "</option>\n";

            if ( $val[0] !="." ) {
                showDir( $dir . "/" . $val , $subdir + 1 );
            }
        }
    }

    return true;
}


echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;<a href=\"harness.php\">Harness&gt;</a>OSS Environment Files\n";
if ( ! isset($_GET['dir_name']) ) {
	$file_path="/var/lib/jenkins/cominf_build/build_scripts/nfd/etc/";
//$dir_name = $_SESSION['varname'];
}
else {
	$file_path=$_GET['dir_name'];
	print "<br>Dir $file_path selected. <br>";
	//if ( ! isset ($_SESSION['dir_name']) ) {
	//	$file_path=$_SESSION['dir_name'] ;
	//}
}
$file_id=0;


$file_names=array();
$file_ids=array();
$dir_ids=array();
$dir_names=array();
$file_names = getDirectoryList($file_path);
$file_names2 = array();
sort($file_names);
foreach ($file_names as $id) {
    			array_push($file_ids, $id);
}

/*
	if ( is_dir($id) ) {
		array_push($dir_ids, $id);
		array_push($file_names2,$id);
	}
	else {
		if (is_file($id) ) {
			array_push($file_ids, $id);
		}
	}
				
}
*/

$form = new HtmlForm("envfileform", '', "", "maxw");
$fld_name = "ossenvfiles (". count($file_names) . ")  ";
$fld = new SelectList(&$db, $fld_name, 'file_name', array($file_id), '', "", 30, 0, "", "", "style=\"width:50%; height:100%\"", "OnEditButton()");
$fld->AddOptions($file_names, $file_ids);
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
