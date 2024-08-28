/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.orchestrator.test.operators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.google.inject.Singleton;

/** Operator for generating MC exclusion list **/
@Singleton

public class MCExclusionListOperator extends PCJCommonOperator {
	
	private Logger logger = LoggerFactory.getLogger(MCExclusionListOperator.class) ;
	private Boolean allMCsUp = false;
	private static final String MC_LIST = "/config/orc_mc_exclusion_list";
	
	// Attribute used by test
	private final static String EXCL_MCS = "exclude_mcs";
	

	
	/**
	 ** checks if MCs are in the proper state(online/offline)
	 ** if MCs are in any other state, waits for maximum of 1 hr 45 mins by checking 
	 ** MC status every 3 minutes.
	 ** @return - true if MCs are in proper state other wise false
	 **/
	public Boolean checkMCInProperState( ){
		
	    //MCWaitTimer 		
		String maxMCWaitTime = (String) DataHandler.getAttribute("MCWaitTimeMinutes");
		if (null == maxMCWaitTime )
		{
			maxMCWaitTime = "50";
		}
		
		int iterations = Integer.parseInt(maxMCWaitTime)/3;
		logger.info("printing iterations"+iterations);
	 	for (int i=0; iterations >= i ; i++) 
	 	{
	 		int exitVal2 = runSingleCommandOnHost(getOssHost() ,"/opt/ericsson/nms_cif_sm/bin/smtool -l", true, true);
		    logger.info("exit value printing "+exitVal2);
	 		logger.info("printing command output"+getLastOutput());
	 		if(0==exitVal2)
	 		{	 			
	 			if(getLastOutput().matches(".*(initializing|retrying|terminating|restart scheduled|no ssr).*"))
		 		{
	 				// if atleast one MC is in one of the above states, wait for 3 minutes and repeat the loop
	 				logger.info("MCs are in improper state , will wait for 3 mins and retry smtool again....in the iteration:"+i);
	 				putDelay(180000); // 3 mins (3*60*1000)
	 				continue;
		 		}
	 			else {
	 				
	 				logger.info("All MC's are in online or offline state, Now MC exclusion list will be generated");
	 				return true;
	 			}
	 		}
	 		else{
	 			
	 			logger.info("failed to run smtool command on admin server");
	 			return false;
	 		}		    
	 	}
	   
	   logger.info("Even after Max timeout also, few MCs are not either online or offline");	
	   return false;
	    
	}
	
	/**
	 ** Update allMCsUp to true if all MCs are online 
	 ** @return - 0 if smtool is successful and 1 for failure
	 **/
	public int checkIfAllMCsOnline(){
		
		// If all MCs are online, it will update allMCsUp flag to true 
        int exitVal2 = runSingleCommandOnHost(getOssHost() ,"/opt/ericsson/nms_cif_sm/bin/smtool -l", true, true);
	    
 		if(0==exitVal2)
 		{
 			if(!(getLastOutput().matches(".*(offline|failed|stopped|unlicensed).*")))
	 		{
 				logger.info("All MCs are online");
 				allMCsUp = true; 								
	 		}
 			
 		}
 		 
 		else if(1==exitVal2){
 			logger.info("failed to run commanad smtool on admin server");
 	 		return 1;
 		}
 		
 		return 0;	
	}
	
	/**
	 ** Generates MC exclusion list
	 ** @return true for success and false for failure
	 **/
		
	public Boolean generateMCExclusionList(){
		
		User orcha = getMsHost().getUsers(UserType.OPER).get(0);
	    
	    String adminHostIP= (String) getOssHost().getIp();
	    logger.info("admin server ip is :"+adminHostIP);
	    String MCExclusionScript = "/ericsson/orchestrator/bin/admin_prepare_for_mc_exclusion_list_updates.bsh"; 
	    String depo = getDepoPath();
	    String mcExclusionCommand= MCExclusionScript+" "+depo+" "+adminHostIP;
	    
	    // if all MCs are up , interactive session will not be there
	    if (allMCsUp)
	    {
	    	int exitVal = runSingleCommandOnMwAsOrcha(mcExclusionCommand);
	    	if(0 == exitVal)
		    {
		    	logger.info(" Orcha module for generating MC exclusion list is run successfully"+getLastOutput());
		    	return true;
		    }
	    	else{
	    		
	    		logger.info(" Orcha module for generating MC exclusion list has failed");
		    	return false;
	    	}
	    	
	    }
	    
	    // All MCs are not up, few might be offline so interactive communication will be there in script
	    initializeHelper(getMsHost() , orcha);
	    runInteractiveScript(mcExclusionCommand);
	    putDelay(10000); //1 min(1*60*1000)
	    Boolean retVal = interactWithShell("correct:", "y");
	    
	    if(!retVal)
		 {
			 logger.info("exception happened in interactive script for MC exclusion list");
			 closeShell();
			 return retVal;
		 }
	    
	    if(0 == getCommandExitValue())
	    {
	    	logger.info(" Orcha module for generating MC exclusion list is run successfully");
	    	closeShell();
	    	return true;
	    }
    	else{
    		
    		logger.info(" Orcha module for generating MC exclusion list has failed and output is : "+getLastOutput());
    		closeShell();
    		return false;
    	}
	    	    
	}
	
	public boolean addConfiguredMCsToExclusionList() {
	    Map<String, String> mcStatuses = parseExclListParams();
	    if (!mcStatuses.isEmpty()) {
	        
	        try {
    	        PrintWriter writer = new PrintWriter("exclfile.tmp");
    	        Set<String> mcs = mcStatuses.keySet();	      
        	    for (String mc : mcs) {
        	        String status = mcStatuses.get(mc);
        	        String line = mc + " " + status;
        	        writer.println(line);
        	        logger.debug("Configured excl list contains: " + line);
        	    }
        	    writer.close();
        	    String depo = getDepoPath();
        	    String mcListPath = depo + MC_LIST;
        	    boolean sent = this.sendFileRemotely(getMsHost(), getMsHost().getUsers(UserType.ADMIN).get(0), "exclfile.tmp", "/tmp");
        	    new File("exclfile.tmp").delete();
        	    if (!sent) {
        	        logger.error("Failed to send exclusion list file to MWS");
        	        return false;
        	    } else {
        	        logger.info("Copied exclusion list file to MWS");
        	        if (allMCsUp)
        	        {
        	            String cmd = "touch " + mcListPath;
        	            int exitCode = this.runSingleCommandOnMwsAsRoot(cmd, false);
                        if (exitCode != 0) {
                            logger.error("Failed to create empty exclusion list: " + exitCode);
                            return false;
                        }
        	        }
        	        logger.info("Remove any MCs in current exclusion list that are also configured");
        	        for (String mc : mcs) {
        	            String cmd = "grep -vw " + mc + " " + mcListPath + " >> " + mcListPath +".tmp";
        	            int exitCode = this.runSingleCommandOnMwsAsRoot(cmd, false);
        	            if (exitCode != 0 && exitCode !=1 ) {
        	                // Exit code will be 0 if MC is in file, and 1 if its not. Other errors indicate a problem
        	                logger.error("Failed to remove " + mc + " from exclusion list");
        	                return false;
        	            }
        	            cmd = "mv " + mcListPath + ".tmp " + mcListPath;
        	            exitCode = this.runSingleCommandOnMwsAsRoot(cmd, false);
                        if (exitCode != 0) {
                            logger.error("Failed to rename after remove " + mc + " from exclusion list");
                            return false;
                        }
        	        }
        	        logger.info("Now append configured MCs to existing exclusion list");
        	        
                    String cmd = "cat /tmp/exclfile.tmp >> " + mcListPath;
                    int exitCode = this.runSingleCommandOnMwsAsRoot(cmd, false);
                    logger.info("Appended configured excl list to generated one, exitCode: ", exitCode);
                    cmd = "rm /tmp/exclfile.tmp";
                    this.runSingleCommandOnMwsAsRoot(cmd);
                    return (exitCode == 0);
        	    }
	        } catch (FileNotFoundException e) {
	            logger.error("Failed to write temporary exclusion list locally", e);
	            return false;
	        }
	    } else {
	        logger.info("No configured MCs to add to exclusion list");
	    }
	    return true;
	}
	
	/**
     * Returns the mcs to exclude with their state that have been passed through command line
     * On command line then exclude_mcs should be set to a comma separated list of MCS and their state, e.g.
     * mc1:state1,mc2:state2,mc3:state3
     * @return
     */
    private Map<String, String> parseExclListParams() {
        Map<String, String> mcsToExclude = new HashMap<String, String>();
        
        Object exclParam = TafConfigurationProvider.provide().getProperty(EXCL_MCS);
        if (exclParam == null) {
            logger.debug("Found 0 configured MCs");
            return mcsToExclude;
        }
        // We may get a List if there is commas or a String - so cope with either
        List<String> mcs = new ArrayList<String>();
        if (exclParam instanceof List) {
            for (Object x : (List)exclParam) {
                mcs.add((String)x);
            }
        } else {
            mcs.add((String)exclParam);
        }
        for (String mc : mcs) {
            String params[] = mc.split(":");
            if (params.length == 2) {
                mcsToExclude.put(params[0], params[1]);
            }
        }
        logger.debug("Configured MCS: " + mcsToExclude.size());
        return mcsToExclude;
    }

}
