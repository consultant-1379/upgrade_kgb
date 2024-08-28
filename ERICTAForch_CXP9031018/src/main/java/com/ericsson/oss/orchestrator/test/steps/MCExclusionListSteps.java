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
import com.ericsson.oss.orchestrator.test.operators.MCExclusionListOperator;
import com.google.inject.Inject;

public class MCExclusionListSteps extends TorTestCaseHelper {

    final Logger logger = LoggerFactory.getLogger(MCExclusionListSteps.class);
            
    @Inject
    private MCExclusionListOperator mcexclusion;
    
    /**
     * Verify all MCs are in started/offline state and then generate exclusion list 
     */
        
    @TestStep(id="VerifyandGenerateMCExclusionList")
    public void VerifyandGenerateMCExclusionList(){
    	
    	setTestStep("VerifyandGenerateMCExclusionList");
    	
    	Boolean retVal = mcexclusion.checkMCInProperState();
    	assertTrue(" Few MCs are not in online/offline state , check smtool -l ", retVal);
    	
    	int retVal1 = mcexclusion.checkIfAllMCsOnline();
    	assertEquals(" Failed to run smtool command on admin host ", retVal1, 0);
    	    	
    	retVal = mcexclusion.generateMCExclusionList();
    	assertTrue(" Failed to run orcha module for generating mc exclusion list ", retVal);
    	
    }

    @TestStep(id="AddConfiguredMCs")
    public void addConfiguredMCs() {
        setTestStep("addConfiguredMCs");
        boolean added = mcexclusion.addConfiguredMCsToExclusionList();
        assertTrue("Failed to add configured MCs", added);
    }
	

}
