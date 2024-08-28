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
import com.ericsson.oss.orchestrator.test.operators.WfEngOperator;
import com.ericsson.oss.orchestrator.test.steps.ConfigureFreezeMediaDownloadSteps;
import com.ericsson.oss.orchestrator.test.steps.LiveUpgTarFileDownloadTestSteps;
import com.ericsson.oss.orchestrator.test.steps.MonitorMwsRebootTestSteps;
import com.ericsson.oss.orchestrator.test.steps.OrcEngineInstallTestSteps;
import com.ericsson.oss.orchestrator.test.steps.OrcPkgInstallTestSteps;
import com.google.inject.Inject;

public class MonitorMwsRebootScenario extends TorTestCaseHelper implements
		TestCase {

	@Inject
	MonitorMwsRebootTestSteps steps;

	@TestId(id = "OSS-68472", title = "Monitor Mws status after Reboot")
	@Context(context = { Context.CLI })
	@Test
	public void MonitorMwsRebootScenario() {

		TestStepFlow moinitorMwsReboot;

		moinitorMwsReboot = flow("Monitor Mws status after Reboot")
				.addTestStep(annotatedMethod(steps, "Previous status file check"))
				.addTestStep(annotatedMethod(steps, "check for shutdown file"))
				.addTestStep(annotatedMethod(steps, "check for ssh connection"))
				.addTestStep(annotatedMethod(steps, "check for startup file"))
				.build();

		TestScenario scenario = scenario("MonitorMwsRebootScenario").addFlow(
				moinitorMwsReboot).build();

		ScenarioListener listener = new LoggingScenarioListener();
		TestScenarioRunner runner = runner().withListener(listener).build();
		runner.start(scenario);
	}

}