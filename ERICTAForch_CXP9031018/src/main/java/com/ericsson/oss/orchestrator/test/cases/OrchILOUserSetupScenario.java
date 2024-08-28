package com.ericsson.oss.orchestrator.test.cases;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.sequence;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.oss.orchestrator.test.steps.OrchaILOSetupTestSteps;
import com.google.inject.Inject;

public class OrchILOUserSetupScenario extends TorTestCaseHelper implements TestCase {


	@Inject
	OrchaILOSetupTestSteps steps;
	@TestId(id = "OSS-43104_1", title = "PCJ to setup ssh keys for orch_ilo user on admin ILO servers ")
    @Test
    public void orchILOUserSetup() {
		TestStepFlow orchILOUser;
		orchILOUser= flow("Creating orch_ilo user")
				.addTestStep(annotatedMethod(steps, "checkFor2Node"))
				.addTestStep(annotatedMethod(steps, "createOrchIloUser"))
				.addTestStep(annotatedMethod(steps, "startWebServerOnVapp"))
				.addTestStep(annotatedMethod(steps, "uploadOrchaKeyToILOFromMWS"))
				.addTestStep(annotatedMethod(steps, "checkPasswordlesslogin")).build();
		TestScenario scenario = scenario("orchILOUserSetup").addFlow(sequence(orchILOUser)).build();
        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
	}

}
