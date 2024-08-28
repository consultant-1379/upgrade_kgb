<?php

include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";
include_once "inc/cdatabase.inc";
$db = &new cDatabase;



function getUCInfoFromTC(&$db, $tc_id) {
        # get usecase name
        $sql = "SELECT usecase.name AS uc_name, usecase.id AS uc_id FROM usecase, testcase WHERE usecase.id=testcase.uc_id AND testcase.id=" . $tc_id;
        if (!($result = &$db->doQuery($sql)) || mysql_num_rows($result) == 0 ) {
                FatalError(101, "Failed to get usecase name for tc id=$tc_id", __file__, __line__);
        }
        $row = mysql_fetch_object($result);
        return array("uc_id" => $row->uc_id, "uc_name" => $row->uc_name);

}

$str="LDAP_USER_ADD";
$str2="";
$tokens=explode("_", $str);
echo $tokens[0]; 

?>
