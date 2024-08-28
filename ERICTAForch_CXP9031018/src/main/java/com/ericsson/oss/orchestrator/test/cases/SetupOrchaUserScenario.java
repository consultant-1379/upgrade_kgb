package com.ericsson.oss.orchestrator.test.cases;

import org.testng.annotations.Test;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.oss.orchestrator.test.steps.SetupOrchaUserSteps;
import com.google.inject.Inject;

public class SetupOrchaUserScenario extends TorTestCaseHelper implements TestCase {

    @Inject
    SetupOrchaUserSteps steps;
    
    @TestId(id = "OSS-55517-1", title = "SetupOrchaUser")
    @Context(context = {Context.CLI})
    @Test
    public void createOrcEngineInstallScenario() {

        TestStepFlow setupFlow;
        
        setupFlow = flow("SetupOrchaUser")
                    .addTestStep(annotatedMethod(steps, "createOrchaHostsFile"))
                    .addTestStep(annotatedMethod(steps, "removeOrchaUser"))
                    .addTestStep(annotatedMethod(steps, "removeOrchaPasswordHistory"))
                    .addTestStep(annotatedMethod(steps, "verifyOrchaIdFree"))
                    .addTestStep(annotatedMethod(steps, "createOrchaUser"))
                    .addTestStep(annotatedMethod(steps, "verifyMWSPasswordless"))
                    .addTestStep(annotatedMethod(steps, "verifyAdminPasswordless"))
                    .addTestStep(annotatedMethod(steps, "removeOrchaHostsFile"))
                    .addTestStep(annotatedMethod(steps, "verifyAdminOrchaIdMatches")).build();    
        
        TestScenario scenario = scenario("setupOrchaUser").addFlow(setupFlow).build();

        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }

}
