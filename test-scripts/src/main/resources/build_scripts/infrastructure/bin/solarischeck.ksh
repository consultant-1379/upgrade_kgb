#!/bin/ksh

cmddir=$(cd $(dirname $0); pwd)

mwsserver=/net/159.107.173.93
serversolloc=/etc/release
imageloc=Solaris_10/Product/SUNWsolnm/reloc/etc/release
MWSLOC=/export/SW_LOCATION/OSSRC_MEDIA
MWSOM=/export/SW_LOCATION/OM_MEDIA/


if [ $( hostname ) = "eeiatuc432" ]; then
        # eeiatuc432 uses libexpect5.38 and libtcl8.4
        LD_LIBRARY_PATH=/app/expect/5.38/lib:/app/tcl/8.4.5/lib:$LD_LIBRARY_PATH export LD_LIBRARY_PATH
        EXPECT="/usr/local/bin/expect"
elif [ $( hostname ) = "eieatx009" ]; then
        # eieatx009 uses libexpect5.38 and libtcl8.4
        LD_LIBRARY_PATH=/app/expect/5.38/lib:/app/tcl/8.4.5/lib:$LD_LIBRARY_PATH export LD_LIBRARY_PATH
        EXPECT="/usr/local/bin/expect"
elif [ $( hostname ) = "eeiatuc681" ]; then
        LD_LIBRARY_PATH=/app/expect/5.38/lib:/app/tcl/8.4.5/lib:$LD_LIBRARY_PATH export LD_LIBRARY_PATH
 	EXPECT="/app/expect/5.38/bin/expect"
else
        # eieatx008 and eeiatuc681 use libexpect5.22 and libtcl7.6
        LD_LIBRARY_PATH=/usr/local/expect/lib:$LD_LIBRARY_PATH export LD_LIBRARY_PATH
        EXPECT="/usr/local/expect/bin/expect"
fi

usage ()
{
        echo "Usage: $cmd [-hv] -r release -s shipment -m machine" 1>&2
        printf " -s shipment    shipment is e.g. F or C1\n"
        printf " -r release     release is e.g. R3\n"
	printf " -m machine     Machine to perform liveupgrade on. e.g atrcx1234\n"
        printf " -h             print this help, then exit\n"
        exit 1
}

solaris_check(){
	servertype=$( uname -a | grep i86 > /dev/null )
	ihostname=$( hostname )

	if (${servertype}); then
		servertype=sol_x86.loc
	else
		servertype=sol_sun4u.loc
	fi
	
	serverver=$( cat ${serversolloc} | head -1 | nawk '{print $2, $3, $4}' )
	mwssolaris=$( cat ${mwsserver}/${MWSLOC}/OSSRC_${release}/${shipment}/${servertype} )
	mwsom="${MWSOM}/OSSRC_${release}/${shipment}/om"
	mwssolver=$( cat ${mwsserver}${mwssolaris}/${imageloc} | head -1 | nawk '{print $2, $3, $4}' )
	echo "##############################################################"
	echo "${ihostname} Server Type: ${servertype}"
	if [ -f "${mwsserver}/${MWSLOC}/OSSRC_${release}/${shipment}/runliveupgrade" ]; then
		check="NO"
	else
		check=${mwssolver}
	fi
	echo "##############################################################"
	echo "#################Live Upgrade Section Start################"
	echo "##############################################################"
	echo "Checking to see is Live Upgrade required" 
	$EXPECT $cmddir/solaris_liveupgrade.exp ${admin1} ${admin2} ${mwsom} ${mwssolaris} "${check}"
	echo "Going to sleep for 10 minutes to allow the Solaris Upgrade server to reboot" 
	sleep 600
	$EXPECT $cmddir/solaris_liveupgrade_OM.exp ${admin1} ${admin2} ${mwsom} ${mwssolaris} "${check}"
	echo "##############################################################"
	echo "#################Live Upgrade Section Complete################"
	echo "##############################################################"	
}


while getopts r:s:m:q:h opt; do
        case $opt in
                r)      release=$OPTARG
                        ;;
                s)      shipment=$OPTARG
                        ;;
		m)      admin1=$OPTARG
                        ;;
		q)	admin2=$OPTARG
                        ;;
		h|\?)   usage
                        ;;
	esac
done
shift `expr $OPTIND - 1`

[ "$release" != "" ] || usage
[ "$shipment" != "" ] || usage
[ "$admin1" != "" ] || usage
[ "$admin2" != "" ] || usage


solaris_check
