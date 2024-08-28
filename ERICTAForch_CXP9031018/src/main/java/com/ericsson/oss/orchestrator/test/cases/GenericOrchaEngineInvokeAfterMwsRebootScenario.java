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
package com.ericsson.oss.orchestrator.test.cases;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.annotations.TestId;

public class GenericOrchaEngineInvokeAfterMwsRebootScenario extends AbstractOrchestratorEngineInvokeScenario  {
	
	
	@TestId(id = "OSS-55692_1", title = "Run orcha engine again After Mws Reboot")
    @Test
    public void runFullOssrcEngine() {
    	
	    runEngine("runOrchaEngData");
    }
	


}
