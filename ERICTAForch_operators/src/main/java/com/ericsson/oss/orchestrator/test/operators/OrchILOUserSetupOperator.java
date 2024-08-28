package com.ericsson.oss.orchestrator.test.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;

public class OrchILOUserSetupOperator extends PCJCommonOperator{
	private Logger logger = LoggerFactory.getLogger(OrchILOUserSetupOperator.class);
	
	public int runCommandOnBothILOServers(String command){
		int ilo1 = 0,ilo2 = 0;
		Host host = getILOHost1();
		Host host2 = getILOHost2();
		if (runSingleCommandOnHost(host, command, false, true) != 0) { 
			logger.error("Unable to create orch_ilo user on " + host.getHostname());
			ilo1=1; 
		}
		if (runSingleCommandOnHost(host2, command, false, true) != 0) { 
			logger.error("Unable to create orch_ilo user on " + host.getHostname());
			ilo2=1;
		}
		
        if (ilo1 != 0 || ilo2 != 0){ return 1; }
        return 0;
	}
	
	public int createOrchILOUser(){
		logger.info("Creating orch_ilo user on both ILO IPs");
		String command = "create /map1/accounts1 username=orch_ilo password=shroot12 name=orch_ilo group=admin,config,oemhp_rc,oemhp_power,oemhp_vm";
		return(runCommandOnBothILOServers(command));
	}
	
	public int startWebServer(){
		logger.debug("Changing the port number of http from 80 to 81 ");
		String command="sed \"s/Listen 80/Listen 81/g\" /etc/httpd/conf/httpd.conf   > /tmp/temp.txt; scp  /tmp/temp.txt /etc/httpd/conf/httpd.conf ; rm -rf /tmp/temp.txt";
		runSingleCommandOnGatewayAsRoot(command);
		if (runSingleCommandOnGatewayAsRoot("grep \"Listen 81\" /etc/httpd/conf/httpd.conf") != 0){ 
			logger.error("Unable to change port from 80 to 81 in /etc/httpd/conf/httpd.conf file");
			return 1;
		}
		logger.info("Starting web server on vApp ");
		command="service httpd restart; service httpd status";
		if ( runSingleCommandOnGatewayAsRoot(command) != 0 ){
			if (! getLastOutput().contains("running...")){
				logger.error("Unable to restart httpd service on vApp gateway");
				return 1;
			}
		}
		logger.info("Successfully started httpd service");
		return 0; 
		
	}
	
	public int uploadOrchKeyToILOFromMWS(){
		logger.debug("Copying orcha public key from MWS to vApp gateway");
		if (verifyPathAvailabilityOnMws("/orcha/.ssh/id_rsa.pub") != 0) {
			logger.error("Unable to find public key of orcha user in mws");
			return 1;
		}
		if (sendFileRemotely(getMsHost(), getGatewayHost(), "/orcha/.ssh/id_rsa.pub", "/var/www/html/", true) != 0) {
			logger.error("Unable to copy public key from MWS to vApp gateway");
			return 1;
		}
		logger.info("Upload orcha public key from vApp gateway to ILO");
		String vAppIP= getGatewayHost().getIp();
		String command = "oemhp_loadSSHKey /map1/accounts1/orch_ilo -source http://"+ vAppIP +":81/id_rsa.pub";
		return(runCommandOnBothILOServers(command));
		
	}
	
	public int checkPasswdLessLoginILO(){
		int ilo1 = 0,ilo2 = 0;
		logger.info("Checking password less connection for ILO servers");
		String command= "ssh -i /orcha/.ssh/id_rsa orch_ilo@"+ getILOHost1().getIp() + " show";
		String command2= "ssh -i /orcha/.ssh/id_rsa orch_ilo@"+ getILOHost2().getIp() + " show";
		User user=getMsHost().getUsers(UserType.ADMIN).get(0);
		//Running on ILO1
		initializeHelper(getMsHost(),user);
		runInteractiveScript(command);
		putDelay(2000);
		if(interactWithShell("password:",getILOHost1().getPass())){
			logger.error("Password less login for orch_ilo user of "+ getILOHost1() +" is unsuccessfull");
			closeShell();
			ilo1=1;
		}
		//Running on ILO2
		initializeHelper(getMsHost(),user);
		runInteractiveScript(command2);
		putDelay(2000);
		if(interactWithShell("password:",getILOHost2().getPass())){
			logger.error("Password less login for orch_ilo user of "+ getILOHost2() +" is unsuccessfull");
			closeShell();
			ilo2=1;
		}
        if (ilo1 != 0 || ilo2 != 0){ return 1; }
        closeShell();
        logger.info("Password less login is present for both ILO servers");
        return 0;
		
	}
}
