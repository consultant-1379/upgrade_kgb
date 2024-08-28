#!/bin/bash

# GLOBAL VARIABLES
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SCRIPT_NAME=$(basename $0)
SCRIPT=${SCRIPT_DIR}/${SCRIPT_NAME}
SSH_SCRIPT=/opt/ericsson/nms_bismrs_mc/bin/ssh_setup.sh
PASSW0RD=shroot
CFG_FILE=/root/files.conf
SNAP_FILE=/root/snap.tar

usage() {
cat << EOF 
Usage: $SCRIPT_NAME <action>
	-c create a tarball containing backed up files on hosts specifed in conf file
	-C force creation of the tarball
	-r to restore the tarball created with -c

The conf file is named as conf.HOST_NAME_OF_SERVER and must contain a declaration of an array named hosts, eg.
	hosts=(1.1.1.1 1.1.1.2)

A list of files to backup must also be specifed in a file called files.HOST_NAME_OF_SERVER
NOTE: the files specified in this file will be deleted before the restore is performed including directories

The script operates by copying itself to each host specified in the hosts array.
The script then calls itself on each host with the -s option followed by:
	s to create a tarball of the backed up files (if the tar exists it wont continue)
	S to force the creation of the tarball
	r to restore the files
The -s option is not meant to be called directly by the user
EOF
}


# --- COPY FILES TO REMOTE HOSTS ---
# copy script and file list to each host
copy() {
	echo copying scripts to: ${hosts[@]}
	for host in ${hosts[@]}
	do
		$SSH_SCRIPT "scp $SCRIPT" root@$host: $PASSW0RD &> /dev/null
		$SSH_SCRIPT "scp files.$(hostname)" root@$host:$CFG_FILE $PASSW0RD &> /dev/null
	done
}


# --- CAPTURE PRE STATE ---
# login to each host and run script with -s s options
capture() {
	copy
	arg=s
	[ "$1" == "S" ] && arg=S
	echo executing capture on: ${hosts[@]}
	for host in ${hosts[@]}
	do
		#$SSH_SCRIPT "ssh root@$host" "./${SCRIPT_NAME} -s $arg" shroot &> /dev/null || snap failed on $host
		$SSH_SCRIPT "ssh root@$host" "./${SCRIPT_NAME} -s $arg" shroot || snap failed on $host
	done
}


# --- RESTORE PRE STATE ---
# login to each host and run script with -s r option
restore() {
	copy
	
	echo removing entries from ONRM	
	for entry in $(/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS -ns masterservice lt FtpService)
	do 
		/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS -ns masterservice dm $entry
	done
	
	# delete the smrs_config file
	rm /etc/opt/ericsson/nms_bismrs_mc/smrs_config

	echo executing restore on: ${hosts[@]}
	for host in ${hosts[@]}
    do
		$SSH_SCRIPT "ssh root@$host" "./${SCRIPT_NAME} -s r" shroot &> /dev/null || restore failed on $host
		
		# force the unmounting of any nfs mounts - needed for tests that check mounts
		#$SSH_SCRIPT "ssh root@$host" 'for m in \$(mount -t nfs | cut -f3 -d\" \")\;do umount -fl \$m\;done' shroot &> /dev/null
		$SSH_SCRIPT "ssh root@$host" 'for m in \$(mount -t nfs | cut -f3 -d\" \")\;do umount -fl \$m\;done' shroot

		# this will unmount any LVs created by non blade tests
		#$SSH_SCRIPT "ssh root@host" 'for lv in \$(lvs | grep smrs | cut -f1 -d\" \")\;do echo umount \$(mount | grep \$lv | cut -f3 -d\" \")\;done' shroot
		$SSH_SCRIPT "ssh root@$host" 'for lv in \$(lvs | grep smrs | cut -f3 -d\" \")\;do umount \$(mount | grep \$lv | cut -f3 -d\" \")\;done' shroot
	done
}

# --- MAKE A TARBALL OF THE SPECIFIED FILES OR RESTORE FILES FROM TARBALL ---
snap() {
	take_snap() {
		echo creating tarball...
		while read f
		do
			[ -e "$f" ] && echo $f >> /tmp/$$
		done < $CFG_FILE
		cat /tmp/$$ | xargs tar pcf  $SNAP_FILE 
		rm /tmp/$$
		exit
	}

	tty > /dev/null && {
		echo this is not supposed to be called directly, exiting
		return 1
	}

	[ ! $# -eq 1 ] && {
		echo invalid number of args supplied to snap function
		echo only one is accepted, s = take snap, S = force a snap to be taken, r = restore snap
		exit 1
	}

	if [ "$1" == "s" ]
	then
		[ ! -e $SNAP_FILE ] && take_snap
	elif [ "$1" == "S" ]
	then
		take_snap
	elif [ "$1" == "r" ]
	then
		echo restoring...
		while read f
		do
			rm -rf "$f"
		done < $CFG_FILE
		cd /
		tar pxvf $SNAP_FILE
		cd -
	else
		echo unknown option passed to snap function: $1
		exit 1
	fi
}


RESTORE=false
CAPTURE=false
snap_type=s
while getopts ":rcChs:" opt; do
	case $opt in
		r)
			RESTORE=true
			;;
		c)
			CAPTURE=true
			;;
		C)
			CAPTURE=true
			snap_type=S
			;;
		s)
			snap $OPTARG
			# without this echo the script runs recursively
			# not sure why...
			echo some text > /dev/null
			exit
			;;
		h)
			usage
			exit 0
			;;

		\?)
			echo "Invalid option: -$OPTARG"
			exit 1
			;;
	esac
done

$RESTORE && $CAPTURE && {
	echo cannont perform a restore and a capture at once
	exit 1
}

$RESTORE || $CAPTURE && {
	# load config file for this host
	conf_file=conf.$(hostname)
	files=files.$(hostname)
	[ -e $conf_file ] || {
		echo $conf_file does not exist
		exit 1
	}
	[ -e $files ] || {
		echo $files does not exist
		exit 1
	}
	source $conf_file
}

$RESTORE && restore
$CAPTURE && capture $snap_type
