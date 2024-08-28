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
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.oss.orchestrator.test.operators.VcsOperator;
import com.google.inject.Inject;

public class VcsSteps extends TorTestCaseHelper {
	
final Logger logger = LoggerFactory.getLogger(VcsSteps.class);
    
        
    @Inject
    private VcsOperator vcsoperator;
    
    /**
     * Verify Veritas VCS license on Admin servers and installs the new 
     * license key if the license has expired
     */
        
    @TestStep(id="verifyandInstallVcsLicenseonOss")    
    public void verifyandInstallVcsLicenseonOss()
    {    
    	setTestStep("verifyandInstallVcsLicenseonOss");
    	
    	logger.info("Extracting VCS license from admin servers");
    	int license = vcsoperator.vxLicense();
    	
    	if (1 < license) {
			
    		logger.info("License has not expired and is valid for another"+license+" days");
    		return ;
    	}
    	
    	String newlickey = vcsoperator.getnewlickey();  
    	assertTrue(" Empty license key, check the license availability in attemjump220 server", newlickey!="");
    	    	
    	if(vcsoperator.is2NodeHost())
    	{
    		logger.info("The admin server is a 2 node cluster");
    		logger.info("The old license will be removed and new license will be installed on admin2");
    		
    		vcsoperator.removeOldLicense(vcsoperator.getOssHost2());
    		
    		Boolean licAdded = vcsoperator.addNewLicense(vcsoperator.getOssHost2(),newlickey);
    		assertTrue("Not able to add new license,Caught exception with expect", licAdded);
    		
    		int val2 = vcsoperator.verifyNewLicense(vcsoperator.getOssHost2());
    		assertTrue("Veritas license is not valid , check admin2", val2>0); 
    		
    	}
    	
    	logger.info("The old license will be removed and new license will be installed on admin server running OSS group");
    	vcsoperator.removeOldLicense(vcsoperator.getOssHost());    	
    	
    	Boolean lic1Added = vcsoperator.addNewLicense(vcsoperator.getOssHost(),newlickey);
		assertTrue("Not able to add new license,Caught exception with expect", lic1Added);
		
		int validity=  vcsoperator.verifyNewLicense(vcsoperator.getOssHost());		
		assertTrue("Veritas license is not valid , check admin1", validity>0);
		
		if ( vcsoperator.checkIfHADIsRunning() != 0)
		{
			logger.info("Starting HA Daemon on : " +vcsoperator.getOssHost());
			int startHA = vcsoperator.runSingleCommandOnHost(vcsoperator.getOssHost() ,"/opt/VRTS/bin/hastart", false, true);	
			assertEquals("Failed to run <-/opt/VRTS/bin/hastart on-> " + vcsoperator.getOssHost(), startHA, 0 );
			vcsoperator.putDelay(30000);
		    assertTrue("Oss did not come ONLINE after running <-/opt/VRTS/bin/hastart-> on " + vcsoperator.getOssHost(), vcsoperator.checkIfOssOnlined(vcsoperator.getOssHost()));
		}
    }
    
}
  	    