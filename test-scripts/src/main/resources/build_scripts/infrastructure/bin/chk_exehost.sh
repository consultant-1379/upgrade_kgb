#!/bin/bash
echo "** performing checks on slave $(hostname)"
THRESHOLD=$1
[ -z "$THRESHOLD" ] && THRESHOLD=90
if [ $(uname) = "Linux" ]; then
	cmd1="df -Pkh"
	cmd2="tail -n +2"
else
	cmd1="df -kh"
	cmd2="tail +2"
fi
$cmd1 | $cmd2 | grep -v '^Filesystem|tmpfs|cdrom' | awk '{ print $5 " " $1 }' | while read output;
do
  echo $output
  usep=$(echo $output | awk '{ print $1}' | cut -d'%' -f1  )
  partition=$(echo $output | awk '{ print $2 }' )
  if [ $usep -ge $THRESHOLD ]; then
    echo "Running out of space \"$partition ($usep%)\" on $(hostname) as on $(date)" 
    exit 1 
  fi
done
