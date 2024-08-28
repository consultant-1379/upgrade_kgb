#!/bin/sh
#
#          COPYRIGHT Ericsson AB 2007
#
#      The copyright to the computer program(s) herein is
#      the property of Ericsson AB, Sweden. The program(s) 
#      may be used and/or copied only with the written 
#      permission from Ericsson AB or in accordance with 
#      the terms and conditions stipulated in the 
#      agreement/contract under which the program(s) have 
#      been supplied.
#
#      2005-11-28     epkwiks       First version
#      2007-04-12     epkwiks       Update for Solaris 10.
#      2009-03-12     edavmax       Make same file work on sparc & x86
#
#
#
#	ssh_setup.sh 
#
# 	eeidle in hub expect will be found in /opt/SELIW/expect/5.45
# \
# the following line restarts using expect \
exec /usr/bin/expect "$0" "$@"

set timeout -1
set command [lindex $argv 0]
set args [lindex $argv 1]
set password [lindex $argv 2]
#puts "$argc ++ $command ++ $args ++ $password"

proc timedout {} {
        send_error "The current command has timed out, exiting.\n"
        exit 1
}

eval spawn $command $args
set finished 0
while { $finished != 1 } {
expect {
       timeout timedout
		"Are you sure you want to continue*" {send "yes\r"}
		"assword*" {send "$password\r"}
		eof	 {set finished 1}
		}
}
catch "wait -i $spawn_id" reason

# return status code of spawned process
exit [ lindex $reason 3 ]

