#!/usr/bin/python
from __future__ import with_statement
from fabric.api import local, settings, abort, run
from fabric.contrib.console import confirm
import paramiko
import time
import sys
import re
def disable_paging(remote_conn):
	'''Disable paging on a Cisco router'''

	remote_conn.send("terminal length 0\n")
	time.sleep(1)

	# Clear the buffer on the screen
	output = remote_conn.recv(1000)

	return output
	pass


def ssh_remote_server( ip, username, password ):
	print "called ssh_remote_server!"
	#password = re.escape(password)
	print "ssh_remote_server: ATTEMPTING: SSH connection to %s with account %s and password %s" % (ip, username,password) 
	# Create instance of SSHClient object
	remote_conn_pre = paramiko.SSHClient()
	remote_conn_pre.load_system_host_keys()
	# Automatically add untrusted hosts (make sure okay for security policy in your environment)
	remote_conn_pre.set_missing_host_key_policy(paramiko.AutoAddPolicy())

	try:
		# initiate SSH connection
		remote_conn_pre.connect(ip, username=username, password=password, allow_agent=False,look_for_keys=False)
		
		print "========================================="
		print "ssh_remote_server: SUCCSS: SSH connection established to %s with account %s and password %s" % (ip, username,password) 

		# Use invoke_shell to establish an 'interactive session'
		remote_conn = remote_conn_pre.invoke_shell()
		print "ssh_remote_server: Interactive SSH session established"
		# Strip the initial router prompt
		output = remote_conn.recv(1000)
		# See what we have
		print "========================================="
		print "\t================================="
		for line in output.splitlines():
			print "\t ssh_remote_server: %s" % line
		
		
		# Turn off paging
		disable_paging(remote_conn)

		# Now let's try to send the router a command
		remote_conn.send("\n")
		remote_conn.send("who\n")

		# Wait for the command to complete
		time.sleep(2)
		
		output = remote_conn.recv(5000)
		remote_conn_pre.close()
		for line in output.splitlines():
			print "\t ssh_remote_server: %s" % line
		print "\t=================================\n"
		print "ssh_remote_server: Interactive SSH session closed"
		print "========================================="
		sys.exit(0)
	except Exception, exp:  
		print "ssh_remote_server: FAIL: Could not connect to %s with account %s and password %s" % (ip, username,password)
		sys.exit(100) 

def ssh_remote_server_file(file,username):
	print "called ssh_remote_server with file: %s" % file
	with open(file, "r") as ins:
		for line in ins:
			server_details = line.split(',')
			ip = server_details[1]
			password = server_details[2]
			print "========================================="
			print "ssh_remote_server: %s" % line
			
			print "ssh_remote_server: ATTEMPTING: SSH connection to %s with account %s and password %s" % (ip, username,password) 
			# Create instance of SSHClient object
			remote_conn_pre = paramiko.SSHClient()
			remote_conn_pre.load_system_host_keys()
			# Automatically add untrusted hosts (make sure okay for security policy in your environment)
			remote_conn_pre.set_missing_host_key_policy(paramiko.AutoAddPolicy())

			try:
				# initiate SSH connection
				remote_conn_pre.connect(ip, username=username, password=password, allow_agent=False,look_for_keys=False)
				
				print "========================================="
				print "ssh_remote_server: SUCCSS: SSH connection established to %s with account %s and password %s" % (ip, username,password) 

				# Use invoke_shell to establish an 'interactive session'
				remote_conn = remote_conn_pre.invoke_shell()
				print "ssh_remote_server: Interactive SSH session established"
				# Strip the initial router prompt
				output = remote_conn.recv(1000)
				# See what we have
				print "========================================="
				print "\t================================="
				for line in output.splitlines():
					print "\t ssh_remote_server: %s" % line
				
				
				# Turn off paging
				disable_paging(remote_conn)

				# Now let's try to send the router a command
				remote_conn.send("\n")
				remote_conn.send("who\n")

				# Wait for the command to complete
				time.sleep(2)
				
				output = remote_conn.recv(5000)
				remote_conn_pre.close()
				for line in output.splitlines():
					print "\t ssh_remote_server: %s" % line
				print "\t=================================\n"
				print "ssh_remote_server: Interactive SSH session closed"
				print "========================================="
				
			except Exception, exp:  
				print sys.exc_info()
				print "ssh_remote_server: FAIL: Could not connect to %s with account %s and password %s" % (ip, username,password)
				sys.exit(100) 
		sys.exit(0)
