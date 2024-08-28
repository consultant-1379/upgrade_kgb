#!/bin/ksh

#set -xv

CAT=/usr/bin/cat
CP=/usr/bin/cp
#CWD=`/usr/bin/pwd`
ECHO=/usr/bin/echo
PASTE=/usr/bin/paste
RM=/usr/bin/rm
CP_FILE=/var/opt/ericsson/sck/data/cp.status
CP_HISTORY=/var/opt/ericsson/sck/data/cp.history
BNAME=`basename $0`
TEMP=/tmp
SMTOOLL="/opt/ericsson/nms_cif_sm/bin/smtool -list"
SMTOOL=/opt/ericsson/nms_cif_sm/bin/smtool
METASTAT=/usr/sbin/metastat
DF=/usr/sbin/df
GREP=/usr/bin/grep
EGREP=/usr/bin/egrep
DMTOOL=/ericsson/dmr/bin/dmtool
VXDISK=/usr/sbin/vxdisk

PKGRM=/usr/sbin/pkgrm
PKGADD=/usr/sbin/pkgadd
PKGINFO=/usr/bin/pkginfo
PGREP=/usr/bin/pgrep
ADMFILE=$CWD/CONF/package.adm
SMALL_UPGRADE=/opt/ericsson/sck/bin/system_upgrade.*sh
SYSTEM_CHECK=/opt/ericsson/sck/bin/system_checks.bsh

OUT=FALSE
RES=$1
WFTP=/var/tmp

CWD="`cd \`dirname $0\`; pwd`"
VER=$CWD/VERSION
ADMFILE=$CWD/CONF/package.adm
RST=$( cat $CWD/ossrc_base_sw/eric_app/cp.status | awk '{print $2}' | sed 's/.*_//' )

platforms=$1
if [[ "${platforms}" == "" ]]; then
	echo "Error: Please specify a platform to build i.e. sparc or X86"
	exit 1
fi
shipment=$2
if [[ "${shipment}" == "" ]]; then
        echo "Error: Please specify a shipment to build i.e. 12.2.4"
        exit 1
fi
mws_ip=$3
if [[ "${mws_ip}" == "" ]]; then
        echo "Error: Please specify mws ip"
        exit 1
fi
release=O$(echo "$shipment" | sed 's/\./_/;s/\..*//')
LOGFILE=$WFTP/OSSRC_$release_$shipment_Upgrade_`date +%Y%m%d-%H:%M:%S`.log

################# To check if the user is Root ###################
#### Checks to see if another ist_run is running on the system ###
################ and abort the script if it is. ##################
list_minus()
{
   eval echo \$$1 | tr -s ' ' '\n' | sort >/tmp/a1.$$
   eval echo \$$2 | tr -s ' ' '\n' | sort >/tmp/b1.$$
   comm -23 /tmp/a1.$$ /tmp/b1.$$ | sort | egrep -v '^$'
   rm /tmp/a1.$$ /tmp/b1.$$
}

root_check()
{
  if [ "`/usr/ucb/whoami`" != "root" ]; then
        echo "\nYou must be root to run this option!\n"
        OUT=TRUE
  fi

  # Check for other instances of ist_run
  pList="$(/usr/proc/bin/ptree $$|nawk '{print $1}')"
  pidList=$(pgrep ist_run)
  pidList=$(list_minus pidList pList)

  if [ -n "$pidList" ]; then
        echo "\n  Another parallel ist_run session is running on the server.\n" 
        echo "  Parallel system-changing ist_run sessions can cause severe system"
        echo "  inconsistency.\n"
        echo "  Terminate all other ist_run sessions before running this script" 
        echo `date +%Y%m%d-%H:%M:%S`
        CIST_RESULT=120
	OUT=TRUE

fi
}

intro()
{
	$ECHO "\n#########################################"
	$ECHO "#########################################\n"
        $ECHO "INSTALLATION OF $release $shipment COMMENCING\n"
        $ECHO "INSTALLATION STARTED AT `date +%Y%m%d-%H:%M:%S`"
	$ECHO "\n#########################################"
	$ECHO "#########################################\n"
}



SELMGNT()
{
	echo "\n--> Taking a print out of MC's Status in Selfmanagement output saved to $LOGFILE"
	$SMTOOLL >> $LOGFILE
	sleep 30
}

###################################################################
#install new usck if version is later than installed one
USCK_INSTALL()
{
	echo "\n--> Removing & Updating USCK if Required"
	USCK_VER=$( pkginfo -l ERICusck 2>/dev/null | grep VERSION | awk '{print $2}' )
	if [[ "$USCK_VER" != "" ]];then
		$PKGRM -n -a $ADMFILE ERICusck || error "$? Exit point ${LINENO}: Problem with removing ERICusck"
	fi
	if [ -f /ericsson/config/.su* ];then
		rm -f /ericsson/config/.su* 
	fi
	if [ -f /ericsson/config/.cu* ];then 
                rm -f /ericsson/config/.cu* 
        fi 
	$CWD/ossrc_base_sw/inst_config/common/upgrade/update_usck.bsh -p $CWD/ossrc_base_sw/inst_config/common/upgrade || error "$? Exit point ${LINENO}: Problem added USCK package"
	
}
VOLUME_CHECK()
{
	echo "\n--> Starting Volume Checks"
	${SYSTEM_CHECK} -d volume_checks
}
PRE_CHECK()
{
	echo "\n--> Starting Pre Checks"
	${SYSTEM_CHECK} -a pre_sys_upgrade
}

INSTALL_UPGRADE()
{
	echo "\n--> Starting upgrade installation"
       check_mirrors
                ret=$?
                if [[ "$ret" -eq "1" ]]; then
                        echo "use no detach"
			$SMALL_UPGRADE -no_detach -jump_start $mws_ip@//export/SW_LOCATION/OSSRC_MEDIA/OSSRC_${release}/${shipment}_UG || error "$? Exit point ${LINENO}: Problem with installation."

                else
                        echo "normal way"
			$SMALL_UPGRADE -jump_start $mws_ip@//export/SW_LOCATION/OSSRC_MEDIA/OSSRC_${release}/${shipment}_UG || error "$? Exit point ${LINENO}: Problem with installation."
                fi
}



ref_logfile()
{
        $ECHO "\n\n\nINSTALLATION OF THE LATEST FEATURE TEST PACKAGE FINISHED\n"
        $ECHO "A LOGFILE HAS ALSO BEEN CREATED FOR THIS UPGRADE\n"
	$ECHO "PLEASE CHECK THE LOGFILE FOR ANY UNDOCUMENTED ERRORS!\n"
        $ECHO "YOU CAN REFERENCE IT VIA $LOGFILE\n"
	$ECHO "INSTALLATION FINISHED OF $release $shipment  at `date +%Y%m%d-%H:%M:%S`\n"
}

getout()
{
	if [ $OUT != FALSE ]; then
		echo "\n\n--> ERROR IN INSTALLATION SEE $LOGFILE FOR MORE INFO\n\n"	
		exit 1
	else
		return 0
	fi
}	

error()
{
        echo "\n\n--> ERROR IN INSTALLATION SEE $LOGFILE FOR MORE INFO\n\n"
        exit 1
}
check_mirrors()
{
#set -xv
        _thishost_=$(hostname)
       echo "Checking mirrors on ${_thishost_}"

        if [ "$($DF -k / | $EGREP -e 'dev.md.dsk')" != "" ]; then
                if [ "$($METASTAT -c | $GREP sync)" != "" ]; then
                        echo "Error"
                        echo "Root Mirrors are not good on ${_thishost_}"
                        echo "--> NOK"
                        return 1
                fi
        fi
#check if mirrors are defined on the server.
        echo "Checking if mirrors are defined on the server --->"
        $VXDISK list | grep disk[0-9][0-9]*mirr  2>/dev/null
         if [ $? -ne 0 ]; then
                echo "no mirrors defined, hence using no detach flag"
                return 1
                else

        $DMTOOL check_for_two_good_mirrors
        if [ $? -ne 0 ]; then
                echo "Error"
                echo "Mirrors are not in sync on ${_thishost_}"
                echo "Using no detach flag"
                return 1
        else
                echo "Mirrors are good on ${_thishost_}"
                echo "--> OK"
                echo ""
                return 2
        fi
fi
}


################## MAIN Body of Script ###########################
MAIN_BODY()
{
		list_minus
                root_check
                intro
                getout
                SELMGNT
		USCK_INSTALL
		VOLUME_CHECK
		PRE_CHECK
		INSTALL_UPGRADE
                ref_logfile
		sleep 600
		init 6
		exit 0
}
MAIN_BODY 2>&1 | tee -a $LOGFILE
