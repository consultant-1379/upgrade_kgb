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
import com.ericsson.oss.orchestrator.test.steps.IniratorSteps;
import com.google.inject.Inject;

public class IniratorScenario extends TorTestCaseHelper implements TestCase {
	
	@Inject
	IniratorSteps steps;
	
	@TestId(id = "OSS-55521_1", title = "Update system.ini with inirator values")
    @Context(context = {Context.CLI})
    @Test
    public void generateSystemIni() {
    	
        TestStepFlow installFlow;        
        
        installFlow = flow("Updating system.ini")
        		    .addTestStep(annotatedMethod(steps, "getLastIniratorQuestion"))        		
        		    .addTestStep(annotatedMethod(steps, "updateSystemIni")).build();
                    
        
        TestScenario scenario = scenario("Update system.ini").addFlow(installFlow).build();
       
        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }

}
