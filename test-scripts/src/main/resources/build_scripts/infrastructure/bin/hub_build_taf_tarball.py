#!/usr/bin/python
import os
import shutil
import sys
import tarfile
import inspect

def create_compressed_tarball(tgz_filename, working_directory, *args):
	print "%s" %  working_directory
	if os.path.exists(working_directory):
		test_harness_package_tgz_filename = open(tgz_filename,"wb")
		os.chdir(working_directory)
		tar	= tarfile.open(tgz_filename, "w:gz")
		for arg in args:
			tar.add(arg)
		tar.close()
	else:
		sys.exit(1)
cwd = os.getcwd()
print "Current working dir : %s" % os.getcwd()		
print "Workspace : %s" % os.getenv('WORKSPACE') 
print "G_PACKAGE_TGZFILE : %s" % os.getenv('G_PACKAGE_TGZFILE')
#print os.environ
# cd into SOURCES directory so that we can tar up its child directories
create_compressed_tarball(os.getenv('G_PACKAGE_TGZFILE'), cwd + "/SOURCES", "FA/DDC/", "FA/SCK/", "FA/TEMP", "FA/USCK", "harness/", "suites/")
# move tgz_filename to workspace dir.
src = cwd + "/SOURCES/platform_test_harness.tar.gz"
dst = cwd + "/platform_test_harness.tar.gz"
#/shutil.move(src, dst)
#print ("destination (dst)= " + dst + ".")
#print inspect.getfile(inspect.currentframe()) # script filename (usually with path)
#pyprint os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe()))) # script directory
#sys.path.append(os.environ['WORKSPACE'])
