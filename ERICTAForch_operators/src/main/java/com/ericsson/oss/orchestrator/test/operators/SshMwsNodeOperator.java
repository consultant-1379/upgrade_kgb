package com.ericsson.oss.orchestrator.test.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.orchestrator.test.operators.CLIHelperOperator;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.google.inject.Inject;

public class SshMwsNodeOperator extends PCJCommonOperator {

	
    private Logger logger = LoggerFactory.getLogger(SshMwsNodeOperator.class);
    
    private static final String SSH_OPTS = "-o PubkeyAuthentication=yes -o KbdInteractiveAuthentication=no -o PasswordAuthentication=no " +
            "-o ChallengeResponseAuthentication=no -o LogLevel=quiet -o StrictHostKeyChecking=no " +
            "-o HostbasedAuthentication=no -o PreferredAuthentications=publickey";
    private static final String LS = "/usr/bin/ls";
    private static final String SSH = "/usr/bin/ssh";
              
        /**
         * @return 
         * @throws TimeoutException
         * @DESCRIPTION Adds passwordless ssh from MWS to UAS
         * @PRE Connection to SUT
         * @VUsers 1
         * @PRIORITY HIGH
         */
        


    public boolean setupPasswordlessSSHMwsNode(HostType type) {
          //  throws InterruptedException, TimeoutException {
    boolean keyExchanged = true ;


    // Read hosts
    TafConfiguration configuration = TafConfigurationProvider.provide();

    
    // get all hosts of specified type defined in host.properties    
    List<Host> hosts = DataHandler.getAllHostsByType(type);
    if (hosts.isEmpty()) {
    	logger.info("No " + type + " defined in host.properties so test has nothing to do");
    	keyExchanged = true;	
    	return keyExchanged;
    }
            
    if (getMsHost() == null) {
    	logger.info("No MWS defined so test has nothing to do");
    	keyExchanged = false;	
    	return keyExchanged;
    }

    //Exit if key for orcha user does not exist
    int retVal1 = checkForOrchaKey();
    if (retVal1 != 0) {
    	logger.error("No RSA Key found on MWS");
    	keyExchanged = false;	
    	return keyExchanged;
    }
           
    //For all hosts of type defined in host.properties
    for (Host host : hosts) {              
    	// Read config and locate upgrade node or single node admin server
    	User rootUser = getMsHost().getUsers(UserType.ADMIN).get(0);

    	// get Orcha's .pub key for exchange                  	            	
    	
    	int catOrchaKey = runSingleCommandOnMwsAsRoot("cat /orcha/.ssh/id_rsa.pub");
    	String stdOut=getLastOutput();
    	if ( catOrchaKey !=0){
    		keyExchanged = false;
			logger.error("failed to find  RSA  keys on" +getMsHost());
			return keyExchanged;
    	}    		
    	
    	//Only do this if stdOut is non null 
    	if (stdOut != null && stdOut.length() > 0) {
    		rootUser = host.getUsers(UserType.ADMIN).get(0);            	
        	String lines[] = stdOut.split("\\r?\\n");
        	int exitValue = runSingleCommandOnHost(host, " grep '" + lines[0] + "' /.ssh/authorized_keys" + " || echo '" + lines[0] + "' >> /.ssh/authorized_keys", false, true);
        	if(exitValue !=0){
        			keyExchanged = false;
        			logger.error("failed to update authorized keys on" +host);
        			}
    	}
    	else{
    		logger.error(".pub key for orcha@"+getMsHost() +" appears to be empty");
    		keyExchanged = false;
			return keyExchanged;
    		}
    		
    	
		} //For all hosts of type defined in host.properties
	return keyExchanged;                                        
    }     
  
        /**
         * Checks that the orcha Key has been created on MWS          
         * @return - exit code from run
         */
    
        public int checkForOrchaKey(){
        	
    		
    		// Check if RSA key exists for Orcha user on MWS before proceeding with copy of Key. 
        	// If its not there, we exit.  either Orcha user does not exist on mws or key has not been created.  
            int exitVal2 = runSingleCommandOnHost(getMsHost() ,"test -f /orcha/.ssh/id_rsa.pub", true, true);   	    
     		if(0==exitVal2)
     		{
     				logger.info("Orcha RSA key found on MWS ");
     				return 0;
     		}
     		 
     		else if(1==exitVal2){
     			logger.error("failed to find Orcha RSA Key on MWS");
     	 		return 1;
     		}
			return exitVal2;     			

        }
        

    
}
