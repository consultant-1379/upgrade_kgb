#!/bin/bash
#
#
# Script to create xml for importing nodes into ARNE
# edavmax
#

MKDIR=/usr/bin/mkdir
CHOWN=/usr/bin/chown
CHMOD=/usr/bin/chmod
CHGRP=/usr/bin/chgrp
GREP=/usr/xpg4/bin/grep
EGREP=/usr/xpg4/bin/egrep
BASENAME=/usr/bin/basename
SCRIPTDIR="`cd \`dirname $0\`; pwd`"
ERICTESTXMLTEMPLATEDIR=$SCRIPTDIR/templates

function usage() {
  echo "usage: $($BASENAME $0) -t STN|MLPPP-Router|LANSwitch|EdgeRouter|CPG|SmartMetro|RBS|ERBS -n <network element name> -s <subnetwork|rncname> -e <site> -c <count> -h <slave> -i <start ip> -a <auto integration ftpservice -o <create output file> -d <delete output file>"
  exit 1
}

function incr_ip() {
        first_three_octets=$(echo $ip | nawk -F. '{printf("%s.%s.%s", $1,$2,$3)}')
	last_octet=$(echo $ip | nawk -F. '{ print $4 }')
        let last_octet+=1
        echo "$first_three_octets.$last_octet"
}

function printCommonHeader() {
  ofile=$1 
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" >> $ofile
  echo '<!DOCTYPE Model SYSTEM "/opt/ericsson/arne/etc/arne6_3.dtd">' >> $ofile
  echo "<Model version=\"1\" importVersion=\"6.3\">" >> $ofile

}

function printCommonFooter() {
  ofile=$1 
  echo "</Model>" >> $ofile

}


function printCreateHeader() {
  ofile=$1 
  type=$2
  printCommonHeader $ofile
  echo "	<Create>" >> $ofile
  echo "		<Site userLabel=\"$SITE\">" >> $ofile
  echo "			<altitude string=\"0\"/>" >> $ofile
  echo "			<location string=\"$SITE\"/>" >> $ofile
  echo "			<longitude string=\"0\"/>" >> $ofile
  echo "			<latitude string=\"0\"/>" >> $ofile
  echo "			<worldTimeZoneId string=\"GB-Eire\"/>" >> $ofile
  echo "			<freeText string=\"\"/>" >> $ofile
  echo "			<datum string=\"wgs84\"/>" >> $ofile
  echo "		</Site>" >> $ofile
  [ $type = "stn" ] && echo "              <SubNetwork userLabel=\"$SUBNETWORK\" networkType=\"IPRAN\">" >> $ofile
  [ $type = "rbs" ] && {
	echo "              <SubNetwork userLabel=\"$SUBNETWORK\" networkType=\"UTRAN\">" >> $ofile
	echo "              	<Group userLabel=\"RbsGrp1\" groupType=\"RBSGroup\" networkType=\"WCDMA\"></Group>" >> $ofile
	
  }

}

function printDeleteHeader() {
  ofile=$1 
  printCommonHeader $ofile
  echo "	<Delete>" >> $ofile

}

function printCreateFooter() {
  ofile=$1 
  type=$2
  [ $type = "stn" ] || [ $type = "rbs" ] && echo "		</SubNetwork>" >> $ofile
  echo "	</Create>" >> $ofile
  printCommonFooter $ofile
}

function printDeleteFooter() {
  ofile=$1 
  echo "	</Delete>" >> $ofile
  printCommonFooter $ofile
}

function toLower() {
  echo $1 | tr "[:upper:]" "[:lower:]"
}

function toUpper() {
  echo $1 | tr "[:lower:]" "[:upper:]"
}


while getopts t:s:e:n:c:h:i:o:d:a: opt
do
        case "$opt" in
        t)      NETYPE="$OPTARG";;
        s)      SUBNETWORK="$OPTARG";;
        e)      SITE="$OPTARG";;
        n)      NENAME="$OPTARG";;
        c)      COUNT="$OPTARG";;
        h)      SMRS_SLAVENAME="$OPTARG";;
        i)      START_IP="$OPTARG";;
        o)	OUTFILE="$OPTARG";;
        d)	DELETEFILE="$OPTARG";;
        a)	AUTOINTEGRATIONFTPSERVICE="$OPTARG";;
        esac
done

[ -z "$NETYPE" ] || [ -z "$COUNT" ] || [ -z "$SITE" ] || [ -z "$SMRS_SLAVENAME" ] || [ -z "$SUBNETWORK" ] || [ -z "$OUTFILE" ] && usage

[ -z "$START_IP" ] && START_IP=0.0.0.0

NETYPE=$( toLower $NETYPE )

[ -z "$NENAME" ] && NENAME=$( toUpper $NETYPE )

[ ! -f $ERICTESTXMLTEMPLATEDIR/$NETYPE.xml ] && {
  echo "Error - unsupported network element type - $NETYPE."
  exit 1
}

[ "$NETYPE" = "erbs" ] || [ "$NETYPE" = "rbs" ] && [ -z "$AUTOINTEGRATIONFTPSERVICE" ] && {
   echo "Error - autoIntegration FtpService name must be specified for ERBS/RBS node types."
   exit 1
}


[ -z "$DELETEFILE" ] && DELETEFILE=${OUTFILE%.*}_delete.xml

[ -f $OUTFILE ] && rm -f $OUTFILE
[ -f $DELETEFILE ] && rm -f $DELETEFILE

# MAIN

echo "Creating files: $OUTFILE, $DELETEFILE"
printCreateHeader $OUTFILE $NETYPE
printDeleteHeader $DELETEFILE 

count=1
ip=$START_IP
while [ $count -le $COUNT ]; do
	nename=$NENAME$count
	echo "Adding network element $nename to xmls"
	sed -e "s/%nename%/$nename/g" -e "s/%slave%/$SMRS_SLAVENAME/g" -e "s/%ip%/$ip/g"  \
            -e "s/%rncname%/$SUBNETWORK/g" -e "s/%site%/$SITE/g" \
            -e "s/%autoIntegrationFtpService%/$AUTOINTEGRATIONFTPSERVICE/g" $ERICTESTXMLTEMPLATEDIR/$NETYPE.xml >> $OUTFILE
	case "$NETYPE" in
		"erbs")
			echo "		<Object fdn=\"SubNetwork=ONRM_ROOT_MO,MeContext=$nename,ManagedElement=1\"/>" >>  $DELETEFILE
			;;
		"rbs")
			echo "		<Object fdn=\"SubNetwork=ONRM_ROOT_MO,SubNetwork=$SUBNETWORK,MeContext=$nename,ManagedElement=1\"/>" >> $DELETEFILE
			;;
		stn)
			echo "		<Object fdn=\"SubNetwork=ONRM_ROOT_MO,SubNetwork=$SUBNETWORK,ManagedElement=$nename\"/>" >> $DELETEFILE
			;;
		*)
			echo "		<Object fdn=\"SubNetwork=ONRM_ROOT_MO,ManagedElement=$nename\"/>" >> $DELETEFILE
			;;
	esac
	if [ $ip != "0.0.0.0" ]; then
          ip=$(incr_ip $ip)
	fi
 	let count+=1
done

case "$NETYPE" in
	"rbs")

		[ ! -f $ERICTESTXMLTEMPLATEDIR/rnc.xml ] && {
  			echo "Error - unable to find file $ERICTESTXMLTEMPLATEDIR/rnc.xml."
  			exit 1
		}
		echo "Adding RNC $SUBNETWORK to xmls"
		sed -e "s/%rncname%/$SUBNETWORK/g" -e "s/%slave%/$SMRS_SLAVENAME/g" -e "s/%site%/$SITE/g" $ERICTESTXMLTEMPLATEDIR/rnc.xml >> $OUTFILE
		echo "		<Object fdn=\"SubNetwork=ONRM_ROOT_MO,SubNetwork=$SUBNETWORK,MeContext=$SUBNETWORK,ManagedElement=1\"/>" >> $DELETEFILE
		echo "		<Object fdn=\"SubNetwork=ONRM_ROOT_MO,SubNetwork=$SUBNETWORK,Group=RbsGrp1\"/>" >> $DELETEFILE
		echo "		<Object fdn=\"SubNetwork=ONRM_ROOT_MO,SubNetwork=$SUBNETWORK\"/>" >> $DELETEFILE  
	;;
	"stn")
		echo "		<Object fdn=\"SubNetwork=ONRM_ROOT_MO,SubNetwork=$SUBNETWORK\"/>" >> $DELETEFILE  
	;;
		
esac
echo "		<Object fdn=\"SubNetwork=ONRM_ROOT_MO,Site=$SITE\"/>" >> $DELETEFILE


printCreateFooter $OUTFILE $NETYPE
printDeleteFooter $DELETEFILE 
echo "Finished"
	

