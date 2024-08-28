package com.ericsson.oss.orchestrator.test.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.oss.orchestrator.test.operators.OrchILOUserSetupOperator;
import com.google.inject.Inject;

public class OrchaILOSetupTestSteps  extends TorTestCaseHelper {
	
	final static Logger logger = LoggerFactory.getLogger(OrchaILOSetupTestSteps.class);
	TestContext context = TafTestContext.getContext();
	boolean twoNode = false;
    @Inject
    private OrchILOUserSetupOperator iloSetupOperator;
    //check deployment is a 2 node or not
    @TestStep(id="checkFor2Node")
    public void checkFor2Node(){
    	setTestStep("checkFor2Node");
    	twoNode = iloSetupOperator.is2NodeHost();
    	assertNotNull("Unable to check deployment is 2 node", twoNode);
    	if( ! twoNode) { logger.info("Skipping ILO setup as deployment has no ILO servers"); }

    }
    //create orch_ilo user on both ilo servers
    @TestStep(id="createOrchIloUser")
    public void createOrchIloUser(){
    	setTestStep("createOrchIloUser");
    	if(twoNode){
    		assertEquals("Unable to create Orcha_ilo user on ILO ips",iloSetupOperator.createOrchILOUser(),0);
    	}
    }
    //Start webserver on vApp
    @TestStep(id="startWebServerOnVapp")
    public void startWebServerOnVapp (){
    	setTestStep("startWebServerOnVapp");
    	if(twoNode){
    		assertEquals("Unable to Start webserver on vApp Gateway",iloSetupOperator.startWebServer(),0);
    	}
    }
  
    //Copy public key of orcha user of mws to vApp gateway
    @TestStep(id="uploadOrchaKeyToILOFromMWS")
    public void uploadOrchaKeyToILOFromMWS (){
    	setTestStep("uploadOrchaKeyToILO");
    	if(twoNode){
    		assertEquals("Unable to upload Orcha key from mws to ILO",iloSetupOperator.uploadOrchKeyToILOFromMWS(),0);
    	}
    }
    //check password less login between admins ILOs
    @TestStep(id="checkPasswordlesslogin")
    public void checkPasswdLessLogin(){
    	if(twoNode){
    		assertEquals("Password less login failed for ILO servers",iloSetupOperator.checkPasswdLessLoginILO(),0);
    	}
    	
    }
    
    	
}
