package com.ericsson.oss.orchestrator.test.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.oss.orchestrator.test.operators.SentinelLicenseSetupOperator;
import com.google.inject.Inject;

public class SentinelLicenseSetupTestSteps extends TorTestCaseHelper{

	final static Logger logger = LoggerFactory.getLogger(SentinelLicenseSetupTestSteps.class);
	TestContext context = TafTestContext.getContext();
    @Inject
    private SentinelLicenseSetupOperator sentinelOperator;
    
    private String sentinelIp;
    private String sentinelPath;
    
    /**
     * Get the sentinel Ip
     */
    @TestStep(id="checkSentinelIp")
    public void checkSentinelIp()
    {
    	setTestStep("checkSentinelIp");
    	
    	//Get the sentinel Ip from host property file
    	sentinelIp = sentinelOperator.getSentinelServerIp();
    	assertNotNull("Unable to find the sentinel server IP",sentinelIp);
    	
    	//Check the Ip is alive or not
    	assertEquals("Given sentinel Server " + sentinelIp + " is not alive", sentinelOperator.pingServer(sentinelIp),0);
    	
    }
    /**
     * Get the sentinel License path
     */
    @TestStep(id="existanceOfsentinelLicense")
    public void existanceOfsentinelLicense()
    {
    	setTestStep("existanceOfsentinelLicense");
    	
    	//Get the path of sentinel license file
    	sentinelPath = sentinelOperator.getSentinelLicencePath(sentinelIp);
    	assertNotNull("Unable to fine License Path", sentinelPath);
    	
    	//check existence of sentinel_license_full file sentinel server
    	assertEquals("Sentinel License Path doesnot exist on server " + sentinelIp, sentinelOperator.verifyPathAvailabilityOnMws(sentinelPath), 0);
    	
    	 
    }
    /**
     * Copy sentinel License full file path to orcha depot path
     * 
     */
    @TestStep(id="copyLicenseToOrchDepo")
    public void copyLicenseToOrchDepo()
    {
    	setTestStep("copyLicenseToOrchDepo");
    	
    	//Get Orcha Depo Path
    	String orchaDepo= sentinelOperator.getDepoPath();
    	assertNotNull("Unable to get Orcha Depo Path", orchaDepo);
    	
    	//run scp  command on mws to get the sentinel license
    	assertEquals("Unable to copy sentinel Licence to Orcha Depo", sentinelOperator.copySentinelLicenseToOrchaDepo(sentinelPath, orchaDepo+"/license"),0);
    	
    	//check the sentinelLicense Path exists on mws
    	assertEquals("Unable to find sentinel Licence file even though copy is sucessful", sentinelOperator.verifyPathAvailabilityOnMws(orchaDepo+"/license/sentinel_license_full"),0);
    	
    }
}
