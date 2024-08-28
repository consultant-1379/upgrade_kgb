#!/usr//bin/python
##############################
# Function: assertCpStatus
# Purpose: Validate that the cp.status file has the correct syntax and details are correct
# Usage: assertCpStatus.py <shipment> <OSS product_number> <version>
#
# calling python script is shown below: 
#import sys
#sys.path.append('<path to atnfdlib.py>')
#import atnfdlib   # imports atnfdlib.py
#shipment=sys.argv[1]
#product_number=sys.argv[2]
#version=sys.argv[3]
#atnfdlib.assertCpStatus(shipment,product_number,version)
#
# Return Values: 
#      0	Success
#      1-3	Failure
#
# Input globals accessed by this function
# none
#
# Output globals set by this function
# none
##############################
def assertCpStatus(shipment,product_number,version):
        import re, sys
	try:
        	cpStatusFile=open('/var/opt/ericsson/sck/data/cp.status')
	except IOError,value:
		print value, "ERROR: File does not exist or is not readable"
		sys.exit(3)
        firstLineString=cpStatusFile.readline()
        #firstLineString='CP_STATUS OSSRC_O13_0_Shipment_13.0.9 AOM_901092 R1J03'
	#firstLineString='CP_STATUS OSSRC_O12_2_Shipment_12.2.10 AOM_901075 R3K_EU05'
        pattern=re.compile( r'^(CP_STATUS )(OSSRC_)(.{5})(_Shipment_)([0-9][0-9]\.[0-9]\.[0-9]{1,2})(.)(.{3}.[0-9]{6})(.)(.*)$')

        matchObj = pattern.match( firstLineString )
        if matchObj:
                print "cp.status has valid format."
		pass
        else:
                print "ERROR:cp.status has incorrect format"
		sys.exit(2)
	# verify shipment,product,number and version in cp.status
        if (matchObj.groups()[4] == shipment) and (matchObj.groups()[6] == product_number) and (matchObj.groups()[8] == version):
                print "cp.status:Validated"
                return True
        else:
                print "ERROR: cp.status:Does not match"
		print "Expected : ", shipment + " " + product_number + " " + version
		print "Received : ", matchObj.groups()[4] + " " + matchObj.groups()[6] + " " + matchObj.groups()[8]
		sys.exit(1)

