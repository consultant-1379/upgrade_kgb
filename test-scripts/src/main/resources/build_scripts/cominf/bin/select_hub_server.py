#!/usr/bin/env python

# this script attempts to find a valid server on the hub without going through the load balancer
#
# it attempts a passwordless ssh connection to each ip in the range starting with base_ip
# and checks for the existence of the "ct" command, if it exists this ip is outout as the hub server to use 

import threading
import Queue
import time
import subprocess
import sys
import random
import getpass

user = 'edavmax' # daves key is currently set up on the jenkins box
base_ip = '150.132.151.'
default_ip = '150.132.181.115'
#cmd = ' "which ct && which clearmake && [ -e /vobs/cominf_bismrsmc/src/com/ericsson/nms/cominf/dm ]"'
cmd = ' "which ct && which clearmake"' # && [ -e /vobs/cominf_bismrsmc/src/com/ericsson/nms/cominf/dm ]"'

if user == '':user = getpass.getuser()

queue = Queue.Queue()
lock = threading.Lock()

# strings + boolean values dont play nicely with threads, using lists...
ok = [False]
out = ['']

def check():
	while True:
		x = queue.get()
		if not ok[0]:
			ip = base_ip + str(x)
			p = subprocess.Popen('ssh -o "BatchMode=yes" -o "StrictHostKeyChecking=no" ' + user + '@' + ip + cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
			p.communicate()
			if p.returncode == 0:
				ok[0] = True
				with lock:out[0] = ip
		queue.task_done()

# start threads
for i in range(20):
	t = threading.Thread(target=check)
	t.setDaemon(True)
	t.start()

# generate a list of final octets to use with base_ip and shuffle them so we dont keep using the same server
ips = range(2, 253)
random.shuffle(ips)
for i in ips:
	queue.put(i)

queue.join()

if out[0]:
	print out[0]
else:
	print default_ip
