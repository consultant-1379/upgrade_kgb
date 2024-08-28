package com.ericsson.oss.orchestrator.test.cases;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.sequence;
import org.testng.annotations.Test;
import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.oss.orchestrator.test.steps.*;
import com.google.inject.Inject;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;


import com.ericsson.cifwk.taf.annotations.Context;

public class AdminPreChecksScenario extends TorTestCaseHelper implements TestCase {

   
	@Inject
	AdminPreChecksTestSteps steps;
	
    @TestId(id = "OSS-70968", title = "Need to do a cleanup post running master.sh")
    @Context(context = {Context.CLI})
    @Test
public void createTestFlow() {

      TestStepFlow testFlow;

      
      testFlow = flow("AdminPreChecks")
            
             .addTestStep(annotatedMethod(steps, "verifyAdminPrep"))
             .addTestStep(annotatedMethod(steps, "corbasSecurityWorkaround"))
             .build();
      
      TestScenario scenario = scenario("AdminPreChecks").addFlow(testFlow).build();

      ScenarioListener listener = new LoggingScenarioListener();
      TestScenarioRunner runner = runner().withListener(listener).build();
      runner.start(scenario);
  }
        
        
}
    

