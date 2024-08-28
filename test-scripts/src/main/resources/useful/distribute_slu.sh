

if [ $# -ne 1 ]
then
    echo "usage is ${0} [ Liveupgrade.tar.gz file ]"
    exit 1
fi

LUK=$1
LUK_TAR="`echo ${LUK} | awk -F "." '{print $1 "." $2}'`"
echo "LUK_TAR is $LUK_TAR"
if [ ! -f ${LUK} ]
then
    echo "cannot find file ${LUK}"
    echo "usage is ${0} [ Liveupgrade.tar.gz file ]"
    exit 1
fi


for i in peer1 ossmaster ebas uas1 mws omsrvs omsrvm omsas nedss
do
    sftp $i << EOF
    cd /var/tmp
    mput ${LUK}
EOF

CMD="cd /var/tmp; rm -rf /var/tmp/Liveupgrade;rm -f ${LUK_TAR};gunzip ${LUK}; tar xvf ${LUK_TAR}"
    ssh $i $CMD
done
