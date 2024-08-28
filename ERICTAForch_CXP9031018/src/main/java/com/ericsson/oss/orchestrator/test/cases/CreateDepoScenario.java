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
import static com.ericsson.cifwk.taf.scenario.TestScenarios.*;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.*;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.oss.orchestrator.test.steps.CreateDepoSteps;
import com.google.inject.Inject;

public class CreateDepoScenario extends TorTestCaseHelper implements TestCase {
	
	@Inject
	CreateDepoSteps steps;
	
	@TestId(id = "OSS-55519_1", title = "Orch Depo creation")
    @Context(context = {Context.CLI})
    @Test
    public void depoCreateScenario() {
    	
        TestStepFlow installFlow;        
        
        installFlow = flow("Create depo")
        		    .addTestStep(annotatedMethod(steps, "getAdminHostname"))        		
        		    .addTestStep(annotatedMethod(steps, "getShipmentandRstate"))
                    .addTestStep(annotatedMethod(steps, "cleanupOldDepo"))
                    .addTestStep(annotatedMethod(steps, "createdepo")).build();
                    
        TestScenario scenario = scenario("Orch depo creation").addFlow(installFlow).build();
       
        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }

}
