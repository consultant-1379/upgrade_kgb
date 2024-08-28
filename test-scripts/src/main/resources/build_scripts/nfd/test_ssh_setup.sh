#|/bin/bash
set -xv
echo "debuging dollarzero: $0"
echo "debugging dollarAT: $@"
exec /app/expect/5.43/bin/expect "$0" "$@"
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
                eof      {set finished 1}
                }
}
catch "wait -i $spawn_id" reason

# return status code of spawned process
exit [ lindex $reason 3 ]

