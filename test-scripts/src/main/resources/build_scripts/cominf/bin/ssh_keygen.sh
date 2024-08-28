#!/usr/local/bin/expect
#---------------------------------------------------------------------
# Ericsson AB 2007
#---------------------------------------------------------------------
#
# ####################################################################
# # COPYRIGHT Ericsson AB 2007
# # 
# # The copyright to the computer program(s) herein is the property
# # of ERICSSON AB, Sweden. The programs may be used
# # and/or copied only with the written permission from ERICSSON
# # AB or in accordance with the terms and conditions
# # stipulated in the agreement/contract under which the program(s)
# # have been supplied.
# #                                         
# ####################################################################
#
#------ History ------------------------------------------------------
# Rev	Date	Prepared		Description
#
# R1A	20111006	enaggop		First version
#

set timeout 600

proc timedout {} {
        send_error "The current command has timed out, exiting.\n"
        exit 1
}

spawn ssh-keygen -t rsa 
set finished 0
while { $finished != 1 } {
expect {
       timeout timedout
                "Overwrite*" {send "yes\r"}
		"Enter file in which to save the key*" {send "\r"}
		"Enter passphrase*" {send "\r"}
                "Enter same passphrase again*" {send "\r"}
		eof	 {set finished 1}
		}
}

