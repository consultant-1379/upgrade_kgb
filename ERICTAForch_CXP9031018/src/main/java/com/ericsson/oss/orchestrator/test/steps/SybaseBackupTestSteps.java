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
package com.ericsson.oss.orchestrator.test.steps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;  
import com.ericsson.cifwk.taf.annotations.TestStep; 
import com.ericsson.oss.orchestrator.test.operators.SybaseBackupOperator;
import com.google.inject.Inject;


public class SybaseBackupTestSteps  extends TorTestCaseHelper implements TestCase{

    final static Logger logger = LoggerFactory.getLogger(SybaseBackupTestSteps.class);
    @Inject 
    private SybaseBackupOperator sybop;
    
    private String sybaseDbaToolsPath="/usr/bin/test -e '/ericsson/syb/util/dba_tools' ";
		    	
    /**  
     * Check if SelfManagement and TSSAuthorityMC are online
    */
    @TestStep(id="checkMCStatus")
    public void checkMCStatus()
    {
    	setTestStep("checkMCStatus");   
 	 		if (sybop.checkHostExists()) {
 	 			assertTrue(sybop.checkMCStatus());
 	 		}	 		
    }
    
    /**
     * Check if Sybase backup exists and if
     * required Create Sybase Backup
    */
    @TestStep(id="checkAndCreateSybaseBackup")
    public void checkAndCreateSybaseBackup()
    {	
    	setTestStep("checkAndCreateSybaseBackup");             
	 		if (sybop.checkHostExists()) 
	 		{
	 			int dbatool = sybop.runSingleCommandOnHost(sybop.getOssHost() ,sybaseDbaToolsPath, false, true);
	 			assertEquals(dbatool,0);
    	    	
	 	logger.info("Checking for Sybase Backup");    	
        boolean validationOk = sybop.sybaseDbCheck(); //Sybase validation check returns true if OK
	    	if(!validationOk){
	    		assertEquals("Sybase Backup did not complete successfully",sybop.sybaseDbBackup(),0);
	    		    
	    	}   
	 	}
	  	  
    } 
		
 }
