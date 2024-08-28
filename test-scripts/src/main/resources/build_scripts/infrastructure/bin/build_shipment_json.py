#!/usr/bin/python
import os,csv,json,sys

def read_pkglistfile(pkglist_file,test_pkgs):
    print pkglist_file
    pkglist=list()
    f=open(pkglist_file,'rb')
    csvfile=csv.DictReader(f,delimiter=' ')
    for pkg in csvfile: 
        pkglist.append(pkg)
        if pkg['one_shot_pkg']=="yes" or pkg['test_pkg']=="yes":
            test_pkgs.append(pkg)
    f.close()
    return sorted(pkglist,key=lambda k: k['package'].lower())

def write_json_file(dict,filename):
    f=open(filename,'w')
    try:
        f.write(json.dumps(dict,sort_keys=True,separators=(',',':')))
        f.flush()
    except IOError as io:
        print "Error writing json to file"
        sys.exit(1)
    finally:
        f.close()
def build_shipment(team,shipment_name,prefix):
    print "building shipment ",shipment_name," with prefix ",prefix
    pkglist_types=['om_pkglist','ossrcsw_pkglist']
    shipment={}
    shipment['shipment_name']=shipment_name
    test_pkgs=list()
    for pkglist in pkglist_types:
        print "building ",pkglist
        pkglist_file="_".join([shipment_name,pkglist])
        shipment[pkglist]=read_pkglistfile(os.path.join(prefix,pkglist_file),test_pkgs)
    shipment['test_pkgs']=test_pkgs
    return shipment   
        
if __name__ == "__main__":
    if len(sys.argv)==4:
        scriptname=sys.argv[0]
        prefix=sys.argv[1]
        if not os.path.exists(prefix):
            print(prefix," does not exist")
            sys.exit(1)
        
        shipment_name=sys.argv[2]
        team=sys.argv[3]
        shipment=build_shipment(team,shipment_name,prefix)
        filename=shipment_name + ".json"
        outputdir=os.path.join(prefix,"json")
        if not os.path.exists(outputdir):
            os.mkdir(outputdir)
            
        filepath=os.path.join(outputdir,filename)
        
        print "writing shipment object to ", filepath
        write_json_file(shipment,filepath)
    else:
        print("Error running ",build_shipment_json.py," - incorrect number of arguments")
        print("Run as: # python ", build_shipment_json.py, "<directory prefix> <shipment name> <team>")
        sys.exit(1)
