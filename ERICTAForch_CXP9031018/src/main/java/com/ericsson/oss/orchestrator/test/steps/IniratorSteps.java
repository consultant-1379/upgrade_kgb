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
package com.ericsson.oss.orchestrator.test.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.oss.orchestrator.test.operators.IniratorOperator;
import com.google.inject.Inject;

public class IniratorSteps extends TorTestCaseHelper {

    final Logger logger = LoggerFactory.getLogger(IniratorSteps.class);
    
    @Inject
    private IniratorOperator inirator;
    
    /**
     * Get the hostname of admin server running oss group 
     */
        
    @TestStep(id="getLastIniratorQuestion")
    public void getLastIniratorQuestion(){
    	
    	setTestStep("getLastIniratorQuestion");
    	
    	int retVal = inirator.getLastInirator();
    	assertEquals("Unable to get last inirator question, check the script in mws",retVal,0);
    	
    	    	    	            
    }
    
    /**
     * Update system.ini with new inirator values 
     */
    
    @TestStep(id="updateSystemIni")
    public void updateSystemIni() {
    	
    	setTestStep("updateSystemIni");
    	
    	Boolean retVal = inirator.runSystemUtils();
    	assertTrue("Orcha module for updating system.ini failed, check the script in mws", retVal);
        	            
    }   
       
}
