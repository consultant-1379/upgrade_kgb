<?php

if ( ! isset ($java_onloadfunc) )
	$java_onloadfunc='';
echo "<html>\n";

echo "<head>\n";
echo "<title>" . $WEBSITE['Name'] . "</title>\n";
echo "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/harness.css\">\n";
echo "</head>\n";
echo "<body style=\"height: 100%;\" $java_onloadfunc>\n";
echo "<p style=\"width: 100%;\" ><img align=right src=images/eric_logo_mini.gif></p>\n";
echo "<h1>" . $WEBSITE['Name'] . "</h1>\n";
