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

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.oss.orchestrator.test.operators.CreateDepoOperator;
import com.google.inject.Inject;

public class CreateDepoSteps extends TorTestCaseHelper {

    final Logger logger = LoggerFactory.getLogger(CreateDepoSteps.class);
    
    private static String adminHostname = "";
    private static String shipment = "";
    private static String rstate = "";
    private String orchaDepo ; 
    
    @Inject
    private CreateDepoOperator createdepo;
    
    /**
     * Get the hostname of admin server running oss group 
     */
        
    @TestStep(id="getAdminHostname")
    public void getAdminHostname() {
    	
    	setTestStep("getAdminHostname");
    	adminHostname = createdepo.determineAdminHostname();
    	assertNotNull("Failed to get admin hostname ", adminHostname);
    }
 		  	 
    /**
     * Get the shipment from OSSRC media path and Rstate from manifest file 
     */
    
    @TestStep(id="getShipmentandRstate")
    public void getShipmentandRstate() {
    	
    	setTestStep("getShipmentandRstate");
    	
        String ossrcMediaPath = (String)DataHandler.getAttribute(createdepo.OSSRC_CACHE);
        
        shipment = (String) DataHandler.getAttribute("shipment");
        
    	String build_manifest_file = ossrcMediaPath+"/ossrc_base_sw/build_manifest_i386";
        String cmd = "cat "+build_manifest_file;
        
        //  get the contents of build manifest file
        int retval = createdepo.runSingleCommandOnMwsAsRoot(cmd);
        assertEquals("Failed to run command on MWS, check manifest file existence in OSSRC media", retval, 0);
        
        // extracts the shipment value from the contents of manifest file
        String[] val1 = createdepo.getLastOutput().split("\\n");
    	String val2 = val1[0];
    	String[] val3 = val2.split(" ");
    	rstate = val3[val3.length - 1];
    	logger.info("Rstate of shipment is :"+rstate);
    	
    	String str = "/JUMP/UPGRADE_SW/"+adminHostname+"/"+shipment+"/"+rstate;
    	DataHandler.setAttribute(createdepo.ORCHA_DEPOT, str );
    	            
    }
    
    /**
     * Remove R-State part of the depo using orcha module 
     */
    
    @TestStep(id="removeOldDepo")
    public void removeOldDepo() {
    	
    	setTestStep("removeOldDepo");
    	String depoArgs = adminHostname+"/"+shipment+" "+rstate;
    	createdepo.removeOldDepo(depoArgs);
  	   
    }
    
    /**
     * Remove old depo if any existing with admin host name 
     */
    
    @TestStep(id="cleanupOldDepo")
    public void cleanupOldDepo() {
    	
    	setTestStep("cleanupOldDepo");
    	String depoArgs = adminHostname;
    	createdepo.cleanupOldDepo(depoArgs);
  	   
    }
    
    /**
     * Creates new depo in MWS with admin hostname 
     */    
    
    @TestStep(id="createdepo")
    public void createdepo() {
    	
    	setTestStep("createdepo");
    	String depoArgs = adminHostname+"/"+shipment+" "+rstate;
        int exitvalue = createdepo.createDepo(depoArgs);
  	    
  	    assertEquals("Failed to create depo directory", exitvalue, 0);
  		    
    }
       
}
