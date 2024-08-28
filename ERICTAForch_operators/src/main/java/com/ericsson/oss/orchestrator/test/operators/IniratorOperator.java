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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.*;
import com.google.inject.Singleton;

/** Operator for running system_upgrade_utils.bsh script **/
@Singleton
public class IniratorOperator extends PCJCommonOperator {
			
	private Logger logger = LoggerFactory.getLogger(IniratorOperator.class);
	
	private String lastInirator ="";
	private Boolean IniratorEnd = false;
	private int numInirators = 0;
	private String systemIniScript = "/ericsson/orchestrator/bin/admin_prepare_for_system_ini_updates.bsh";
	private String systemIniLibFile = "/ericsson/orchestrator/lib/inirator_common_functions.lib";

	/**
     * Get the last inirator question so that we will be iterating
     * till the last inirator to update system.ini file
     * @return 0 success and 1 failure
     */
	
	public int getLastInirator(){				
		String str = "grep iniset"+" "+systemIniLibFile+" | grep NUMBER_";
		logger.info("printing str value :"+str) ;
		int exitVal = runSingleCommandOnHost(getMsHost() ,str, false, false);
		if (0!=exitVal)
		{
			logger.info("Unable to get the last inirator from "+systemIniLibFile);
			return exitVal;
		}
		logger.info(" printing list of inirator questions : "+getLastOutput());
		
		String[] eachLine = getLastOutput().split("\\n" );
		numInirators = eachLine.length;
		logger.info("priniting inirator count"+numInirators);
	    String lastline = eachLine[(eachLine.length)-1];
	    logger.info("printing last line output"+lastline);
	    String pattern = "NUMBER(.*?)NODES";
	    
	    String expectedValue = extractStringFromPattern(pattern, lastline);
	    logger.info("priniting expected value"+expectedValue);
	    if(expectedValue == null || expectedValue.isEmpty())
	    {
	    	logger.info("Failed to extract string from pattern, last line output is :"+lastline);
	    	return 1;
	    }
	    lastInirator = expectedValue.split("_")[1];
	    logger.info("printing final value "+lastInirator);
	    
	    return 0;
	    
	}
	
	/**
     * execute the orcha module for generating inirator questions file
     * @return true success and false failure
     */
	
	public Boolean runSystemUtils( ){
		
	    Boolean retVal = false;
		User orcha = getMsHost().getUsers(UserType.OPER).get(0);
	    initializeHelper(getMsHost() , orcha);
	    String depo = getDepoPath();
	    String adminHostIP= (String) getOssHost().getIp();
	    String cmd = systemIniScript+" "+depo+" "+adminHostIP;
	    runInteractiveScript(cmd);
		 
		 for (int i=0 ; numInirators >= i  ; i++)
		 {
			 if(IniratorEnd)
			 {
				 logger.info("exiting for loop and printing i:"+i );
				 if(0 == getCommandExitValue())
				    {
				    	logger.info(" Orcha module for updating system.ini has run successfully");
				    	closeShell();
				    	return true;
				    }
			    	else{
			    		
			    		logger.info(" Orcha module for updating system.ini has failed : "+getLastOutput());
			    		closeShell();
			    		return false;
			    	}
			 }
			 putDelay(2000);
			 retVal = interactvShell("]:", "\r");
			 if(!retVal)
			 {
				 logger.info("exception happened in interactive script");
				 closeShell();
				 return retVal;
			 }
		 }
		 
		 closeShell();
		 return retVal;	                 	                 
	}		
		
	private Boolean interactvShell(String qes, String answer) {
        try {
        	
        	String output2 = expect(qes, 10);
        	logger.info("printing output value"+output2);
        	if (output2.contains("GPEH FILE SIZE") )
        	{
        		logger.info("passing 3 for inirator");
        		runInteractiveScript("3");
        		return true;
        	}else
        		if(output2.contains(lastInirator))
        		{
        			      
        			IniratorEnd = true;
        			logger.info("executing last inirator");
            		runInteractiveScript(answer);
            		return true;
            		 
        		}
        		else {
        			runInteractiveScript(answer);
        			
        			}
             } catch(Exception e)
             {
            	 logger.error( e.getMessage()); 
            	 return false;
             }
        return true;
     }
		
}
