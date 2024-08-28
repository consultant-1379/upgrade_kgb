package com.ericsson.oss.orchestrator.test.cases;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.oss.orchestrator.test.steps.SshMwsNodeTestSteps;
import com.ericsson.oss.orchestrator.test.steps.SshMwsPeerTestSteps;
import com.google.inject.Inject;

public class SshMwsPeerScenario extends TorTestCaseHelper implements TestCase {

   
	@Inject
    SshMwsPeerTestSteps steps;
	
    @TestId(id = "OSS-58870", title = "PCJ Est SSH connection between MWS orcha and Peer")
    @Context(context = {Context.CLI})
    @Test
public void createTestFlow() {

      TestStepFlow testFlow;

      
      testFlow = flow("SetupSshMwsPeer")
            
             .addTestStep(annotatedMethod(steps, "initialize"))
             .addTestStep(annotatedMethod(steps, "SetupSshMwsNode"))
             .addTestStep(annotatedMethod(steps, "VerifyPasswordLess"))
             .build();
      
      TestScenario scenario = scenario("SetupSshMwsPeer").addFlow(testFlow).build();

      ScenarioListener listener = new LoggingScenarioListener();
      TestScenarioRunner runner = runner().withListener(listener).build();
      runner.start(scenario);
  }
        
        
}
    

