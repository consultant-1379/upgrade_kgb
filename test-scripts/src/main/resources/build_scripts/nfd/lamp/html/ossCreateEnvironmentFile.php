<?php
include_once "inc/chtmlform.inc";
include_once "inc/globals.inc";
include_once "inc/error.inc";

include "header.php";
echo "<p><a href=http://" . $WEBSITE['HOMEURL'] . ">Home</a>&gt;Harness\n";
$harnesshost='';
?>
<html> <head> <title>OSS Environment Configuration</title></head>
<body>
<form action="Select_Env.php" method="post">
<b>Specify environment file Name:</b> <input type="text" name="ENV_FILE_NAME" size=50><br>
<b>Run test harness on:</b><br>

<input type="radio" name="harnesshost" value="targetserver" checked="yes" >Target Server
<input type="radio" name="harnesshost" value="localhost">Test Control Server<br>
<b>Choose test case grouping:</b><br>
<input type="radio" name="harnessparam" value="testcases"  checked="yes">Individual Test Cases
<input type="radio" name="harnessparam" value="testsuites">Test Suites  
<input type="radio" name="harnessparam" value="usecases">Use Cases <br>

<b>Parameters for test case grouping:</b> <br>
<input type="text" name="testselection" size=200><br>
<b>Admin1 IPV4 Address&nbsp;&nbsp;:</b>
<input type="text" name="CI_ADMIN1_IPV4_ADDRESS" size=30><br>
<b>Admin2 IPV4 Address&nbsp;&nbsp;:</b>
<input type="text" name="CI_ADMIN2_IPV4_ADDRESS" size=30><br>
<b>Admin1 Hostname&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<input type="text" name="CI_ADMIN1_HOSTNAME" size=30><br>
<b>Admin2 Hostname&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<input type="text" name="CI_ADMIN2_HOSTNAME" size=30><br>
<b>Admin1 Root Password:</b>
<input type="text" name="CI_ADMIN1_ROOTPW" size=30><br>
<b>Admin2 Root Password:</b>
<input type="text" name="CI_ADMIN2_ROOTPW" size=30><br>
<b>Admin1 ILO Name&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<input type="text" name="CI_ADMIN1_ILO_NAME" size=30><br>
<b>Admin2 ILO Name&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<input type="text" name="CI_ADMIN2_ILO_NAME" size=30><br>
<b>Admin1 ILO Root Password:</b>
<input type="text" name="CI_ADMIN1_ILO_ROOTPW" size=30><br>
<b>Admin2 ILO Root Password:</b>
<input type="text" name="CI_ADMIN2_ILO_ROOTPW" size=30><br>
<b>Release, e.g. 'O13_2'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<input type="text" name="CI_RELEASE" size=30><br>
<b>Shipment, e.g. '13.2.7'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<input type="text" name="CI_SHIPMENT" size=30><br>

<b>Type of build&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<label for="II">II</label> 
<input type="radio" name="CI_TYPE" id="II" value="II"  checked="yes" />
<label for="UG">UG</label> 
<input type="radio" name="CI_TYPE" id="UG" value="UG" /><br>

<b>Architecture&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<label for="i386">i386</label> 
<input type="radio" name="CI_PLATFORM" id="i386" value="i386"  checked="yes" />
<label for="SPARC">SPARC</label> 
<input type="radio" name="CI_PLATFORM" id="SPARC" value="SPARC" /><br>

<b>Vendor&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<label for="hp">hp</label> 
<input type="radio" name="CI_ENVIRONMENT" id="hp" value="hp"  checked="yes" />
<label for="sun">sun</label> 
<input type="radio" name="CI_ENVIRONMENT" id="sun" value="sun" /><br>
<b>typeii&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<label for="II">II</label> 
<input type="radio" name="CI_BUILD_TYPE" id="II" value="II" />
<label for="CU">CU</label> 
<input type="radio" name="CI_BUILD_TYPE" id="CU" value="CU"  checked="yes"/><br>
<b>OSS Product Number&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<input type="text" name="CI_OSS_PROD_NUM" size=30><br>
<b>OSS Product Revision&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:</b>
<input type="text" name="CI_OSS_PROD_REV" size=30><br>
<b>Do all fields have a value?&nbsp;:</b>
<label for="no">No</label> 
<input type="radio" name="somedummy" id="no" value="no"  checked="yes"/>
<label for="yes">Yes</label> 
<input type="radio" name="somedummy" id="yes" value="yes" /><br>

<input type="submit" value="Submit Data">
<input type="reset" value="Clear Data">
</form>
</body>
</html>
<!--
<b>DMR configuration must be set to 'NO' for jenkins jobs:</b>
<input type="text" name="CI_DMR" size=3><br>
<input type="text" name="CI_MWS_JUMP" size=30><br>
<input type="text" name="CI_PRE_INI" size=30><br>
<input type="text" name="CI_ADMIN2_HOSTNAME" size=30><br>
<input type="text" name="CI_ADMIN1_HOSTNAME" size=30><br>
<input type="text" name="CI_ADMIN2_HOSTNAME" size=30><br>
<input type="text" name="CI_ADMIN2_STORAGE_NIC1" size=30><br>
<input type="text" name="CI_ADMIN2_STORAGE_NIC2" size=30><br>
<input type="text" name="CI_ADMIN2_STORAGE_IP" size=30><br>
<input type="text" name="CI_ADMIN2_STORAGE_NETMASK" size=30><br>
<input type="text" name="CI_ADMIN2_PUBLIC_NIC1" size=30><br>
<input type="text" name="CI_ADMIN2_PUBLIC_NIC2" size=30><br>
<input type="text" name="CI_ADMIN2_PUBLIC_NETMASK" size=30><br>
<input type="text" name="CI_ADMIN2_PUBLIC_DEFAULT_ROUTER" size=30><br>
<input type="text" name="CI_ADMIN2_PRIVATE_NIC_IP" size=30><br>
<input type="text" name="CI_ADMIN2_PRIVATE_NETMASK" size=30><br>
<input type="text" name="CI_ADMIN2_BACKUP_IP" size=30><br>
<input type="text" name="CI_ADMIN2_BACKUP_NETMASK" size=30><br>
<input type="text" name="CI_ADMIN2_IPV6_ADDRESS" size=32><br>
<input type="text" name="CI_ADMIN2_IPV6_SUBNET_PREFIX" size=4><br>
<input type="text" name="CI_ADMIN2_IPV6_DEFAULT_ROUTER" size=32><br>
-->
