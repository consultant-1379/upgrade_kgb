package com.ericsson.oss.orchestrator.test.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.oss.orchestrator.test.operators.DHCPOperator;
import com.google.inject.Inject;

public class DHCPTestSteps extends TorTestCaseHelper {

    final static Logger logger = LoggerFactory.getLogger(DHCPTestSteps.class);
    
    boolean isTwoNode = false;
    
    @Inject
    private DHCPOperator engOperator;
 
    //check deployment is a 2 node or not
    @TestStep(id="checkFor2Node")
    public void checkFor2Node(){
    	setTestStep("checkFor2Node");
    	isTwoNode = engOperator.is2NodeHost();
    	assertNotNull("Unable to check deployment is 2 node", isTwoNode);
    	if( ! isTwoNode) { logger.info("Skipping DHCP setup as deployment isnt a 2 node cluster"); }
    }

    /**
     * Copies the wfeng.oss_ini_template to the MWS
     */
    @TestStep(id = "copyIniTemplate")
    public void copyIniTemplate() {
        setTestStep("Copy ini template");
        if(isTwoNode){
        String localFile = DHCPOperator.DHCP_TEMPLATE;
        boolean copied = engOperator.copyWfEngCfgFileToMWS(localFile);
        assertTrue("Failed to copy dhcp_client_template", copied);
        }
    }

    /**
     * Customises the workflow ini file with details from TafConfiguration
     */
    @TestStep(id = "customiseIni")
    public void customiseIni() {
        setTestStep("Customise ini");
        // Customise dhcp ini file
        if(isTwoNode){
        int sedExit = engOperator.customiseIniFile(TafConfigurationProvider
                .provide());
        assertEquals("Failed to customise ini file", 0, sedExit);
        }
    }

    /**
     * Add node as a DHCP client on mws
     */
    @TestStep(id = "removeDHCPClient")
    public void removeDHCPClient() {
        setTestStep("Add node as a DHCP client");
        if(isTwoNode){
        assertEquals("Failed to remove host as DHCP client", 0, engOperator.removeAdminDHCPClient());
        }
    }
   
    /**
     * Add node as a DHCP client on mws
     */
    @TestStep(id = "addDHCPClient")
    public void addDHCPClient() {
        setTestStep("Add node as a DHCP client");
        if(isTwoNode){
        assertEquals("Failed to add host as DHCP client", 0, engOperator.addAdminAsDHCPClient());
        }
    }
   
}

