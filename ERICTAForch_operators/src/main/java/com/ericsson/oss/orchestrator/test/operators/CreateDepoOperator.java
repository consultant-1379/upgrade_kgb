/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.orchestrator.test.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/** Operator for running orc_create_rep_dir.bsh script **/
@Singleton
public class CreateDepoOperator extends PCJCommonOperator {
			
	private Logger logger = LoggerFactory.getLogger(CreateDepoOperator.class);
	private static String adminHostname = "";
	 
	/**
     * Removes if any R-State orcha depo exists 
     * @param str - old depo name
     */
		
	public void removeOldDepo(String str) {
		
		String str1 = "/ericsson/orchestrator/bin/orc_create_rep_dir.bsh -r -b "+str;
		
		logger.info("Removing old DEPO existing with same name as we would like to create new one");
		//deletes orcha depo with same name on MWS as orcha user
		runSingleCommandOnHost(getMsHost() ,str1, false, false);
	}
	
	/**
     * Removes if any orcha depo exists with host name of admin server
     * @param str - old depo name
     */
		
	public void cleanupOldDepo(String str) {
		
		String str1 = "rm -rf /JUMP/UPGRADE_SW/"+str;
		
		logger.info("Removing old DEPO existing with same name as we would like to create new one");
		//deletes orcha depo with same name on MWS as orcha user
		runSingleCommandOnHost(getMsHost() ,str1, false, false);
	}
	
	/**
     * creates new depo with host name of admin server
     * @param  str - upgrade reference
     * @return creation status
     */
	
    public int createDepo(String str) {
		
		String str1 = "/ericsson/orchestrator/bin/orc_create_rep_dir.bsh -b "+str;
		
		//creates orcha depo on MWS as orcha user
		int Exitval = runSingleCommandOnHost(getMsHost() ,str1, false, false);
		logger.info(" create depo output"+getLastOutput());
		         
        return Exitval;
	}
    
    public String determineAdminHostname() {
    	logger.info("Determine if the deployment is physical by getting OSS hostname using <-/bin/hostname-> on " +getOssHost()+ "");
    	if (runSingleCommandOnHost(getOssHost() ,"/bin/hostname", true, true) == 0);
    	{
    	adminHostname = getLastOutput();
    	}
    	
 		if ( adminHostname.equals("ossmaster")){
 			logger.info("This is a vApp so getting gateway name using <-/bin/hostname-> on " +getGatewayHost().getHostname()+ "");
 			if (runSingleCommandOnGatewayAsRoot("/bin/hostname") == 0);
 				}
 	
 			adminHostname = getLastOutput();
 			{
 				return adminHostname;
 		}
    }
}
