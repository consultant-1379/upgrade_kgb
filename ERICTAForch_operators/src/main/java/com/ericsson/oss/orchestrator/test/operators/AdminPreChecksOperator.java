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

public class AdminPreChecksOperator extends PCJCommonOperator {

	
    private Logger logger = LoggerFactory.getLogger(AdminPreChecksOperator.class);
            /**
         * @return 
         * @throws 
         * @DESCRIPTION Removes potential  upgrade blockers from servers, before we Split cluster.
         * @PRE Connection to SUT
         * @VUsers 1
         * @PRIORITY HIGH
         */
    
     public boolean adminPrep() {      
     boolean cleanup = true ;
     List<Host> hosts = DataHandler.getAllHostsByType(HostType.RC);
     if (hosts.isEmpty()) {
    	 	logger.info("No Admins defined in host.properties so test has nothing to do");	
      		return cleanup;
      }
                    
     //For all Admin servers  defined in host.properties
     //remove /export/scripts which master.sh leaves behind and will cause upgrade to hang on admin2  when isolated.
     for (Host host : hosts) {              
    	    int removeMount = runSingleCommandOnHost(host, " if mount | grep /export/scripts; then fuser -k /export/scripts; umount -f /export/scripts; else echo 0; fi  ", false, true);
    	 	if (removeMount !=0 ){
    	 		logger.error(" Failed to remove /export/scripts mount to remove on : " +host);
    	 		cleanup= false; 
    	 	}
    	
    //Kill any DMR process which are still open since install and will hang upgrade.  
    //int exitValue1 = runSingleCommandOnHost(host, " pgrep -f dmr && kill -9 `pgrep -f dmr` || echo 0  ", false, true);
    int killDmr = runSingleCommandOnHost(host," pid=`ps -Af | grep  dmr | grep -v 'grep' | awk '{print $2}'` && kill -9 $pid  >/dev/null 2>&1 || echo 0 " , false, true);    	 
    		if (killDmr !=0 ){
    			logger.error(" failed to remove  dmr procs on : " +host);
    			cleanup = false; 
    			} 
    }   
    return cleanup;
    }     
     
     
     public boolean setupCorbaWorkAround()
     {
    	 	boolean corbasetup = true ;    	     	
    	 	// check if omsas is configured on Oss server itself 
    	 	int noOmsas = runSingleCommandOnHost(getOssHost() ,"/opt/ericsson/nms_cif_sm/bin/smtool selftest scs 7 | grep -w empty", true, true);
    	 	logger.info("exit value printing "+noOmsas);
    	 	logger.info("printing command output"+getLastOutput());
    	 	 	 	 	 
	 	 	 			 	
	 		// if n == 0,  then we dont have an omas configured and have nothing to do, else we do have work to do.  
	 		if(0 != noOmsas)	 			
	 		{
	 			//verify security.ksh exists before we try and use it 
		        int fileCheck = checkForSecpfbin();
		        if (fileCheck != 0) {	 			 		          	
		        		return corbasetup=false;
		        }
	 			
	 			int corbaSecOn = runSingleCommandOnHost(getOssHost() ," out2=`/opt/ericsson/secpf/scripts/bin/security.ksh -status`; echo $out2 | nawk -F':' '{print $2}' | grep ON ", true, true);
	 			logger.debug("exit value printing "+corbaSecOn);
	 			logger.debug("printing command output"+getLastOutput());	 			 			 			 		
	 			if(0==corbaSecOn) 
	 			 		{
	 			 		logger.info("CORBA security is ON , we have more work to do");
	 			 		//Verify we have an Omsas configured in hosts.prop
	 			 		Host Omsas=getOmsasHost();
	 			 		if (Omsas == null) {
	 			 				logger.error("Corba Security is on but omsas is not defined in host.properties cannot continue " );
	 			 		        return corbasetup=false;
	 			 		}
	 			 		else
	 			 		{
	 			 				logger.info("Copying certs from  " +Omsas);	
	 			 				// copy certs from omsas to admin using passwd info from hosts.prop 
	 			 				String cmd="scp " + getOmsasHost().getIp() +":/opt/ericsson/csa/certs/*RootCA.pem /var/tmp/";
	 			 				User adminUser=getOssHost().getUsers(UserType.ADMIN).get(0);
	 			 				String rootpw=getOmsasHost().getPass();
	 			 				initializeHelper(getOssHost(),adminUser);
	 			 				runInteractiveScript(cmd);
	 			 		        putDelay(10000);
	 			 		        if(interactWithShell("assword:",rootpw))
	 			 		        { 
	 			 		        	
	 			 		        	// install certs on admin using security scripts   
	 			 		        	String cmd1=" pemlist=`ls /var/tmp/*RootCA.pem | grep -v TOR | grep -v LTE` && for pem in $pemlist; do  /opt/ericsson/secpf/scripts/bin/security.ksh -u -installTrustCert $pem; done";
	 			 		        	int certInstall = runSingleCommandOnHost(getOssHost() ,cmd1, true, true);
	 			 			 		logger.debug("printing command output"+getLastOutput());
	 			 			 		logger.debug("exit value printing "+certInstall);
	 			 			 		if(0 != certInstall) 
	 			 			 		{
	 			 			 			logger.error("failed  to update Omsas: " + getOmsasHost().getHostname() +" certs on : " +getOssHost()  );
	 			 			 			corbasetup=false;
	 			 			 			
	 			 			 		}
	 			 			 			 			 		
	 			 			 		return corbasetup;
	 			 			 	
	 			 		        }	// end interactive
	 			 		        else {
	 			 		        	 logger.error("interactWithShell expected Screen output not found  " );
 			 			 			 return corbasetup=false;
	 			 		        }
	 			 		      
	 			 		    }	
	 			 		
	 			 		}
	 			 		
	 		} 
	 		logger.warn("Corba Security is not enabled on oss host: " +getOssHost() + " TC  has nothing to modify"  );
			return corbasetup; 	 
    	 
     }  //end setupCorbaWorkAround
     
     
     
     	/**
     	 * Returns omsas object
     	 * @return the omsas host
     	 */
	    public Host getOmsasHost() {
	        return DataHandler.getHostByType(HostType.OMSAS);
	    }
		

	    /**
		* Returns status of filecheck 
		* / Check if /opt/ericsson/secpf/scripts/bin/security.ksh exists on ossmaster ,  we need  it to verify security status.
		* // If its not there, we exit.
		*/

	    public int checkForSecpfbin(){			    	    
	    int fileFound = runSingleCommandOnHost(getOssHost() ,"test -f /opt/ericsson/secpf/scripts/bin/security.ksh", true, true);   	    
		if(0==fileFound)
		{
				logger.info("needed script /opt/ericsson/secpf/scripts/bin/security.ksh  found Ok   ");
				return 0;
		}
		 
		else if(1==fileFound){
			logger.error("failed to find /opt/ericsson/secpf/scripts/bin/security.ksh on: " +getOssHost() );
	 		return 1;
		}
	return fileFound;     			
}        

}  


