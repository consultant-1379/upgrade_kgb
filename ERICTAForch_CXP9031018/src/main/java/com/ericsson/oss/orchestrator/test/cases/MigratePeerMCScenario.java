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
import com.ericsson.oss.orchestrator.test.steps.MigratePeerMCSteps;
import com.google.inject.Inject;

public class MigratePeerMCScenario extends TorTestCaseHelper implements TestCase {
	
	@Inject
	MigratePeerMCSteps steps;
	
	@TestId(id = "OSS-55396_1", title = "Configure Peer FTH")
    @Context(context = {Context.CLI})
    @Test
    public void ConfigurePeerFTH() {
    	
        TestStepFlow migrateMC;        
        
        migrateMC = flow("Configure Peer FTH")
        		         .addTestStep(annotatedMethod(steps, "verifyFthInstanceTowardsPeer")).build();                    
        
        TestScenario scenario = scenario("Verify FTH").addFlow(migrateMC).build();
       
        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }


}
