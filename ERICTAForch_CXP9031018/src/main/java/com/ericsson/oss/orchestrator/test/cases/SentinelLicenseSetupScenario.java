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
import com.ericsson.oss.orchestrator.test.steps.SentinelLicenseSetupTestSteps;
import com.google.inject.Inject;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.*;

public class SentinelLicenseSetupScenario extends TorTestCaseHelper implements TestCase {
	@Inject
	SentinelLicenseSetupTestSteps steps;
    
    @TestId(id = "OSS_55389", title = "OSS-RC ORCH CDB: PCJ Source latest Sentinel Licenses onto upgrade_depot")
    @Test
    public void SentinelLicenseSetup() {
    	
    	TestStepFlow sentinelLicenseSetup = flow("Setting up sentinel License on mws in Orcha depo path")
    			.addTestStep(annotatedMethod(steps, "checkSentinelIp"))
    			.addTestStep(annotatedMethod(steps, "existanceOfsentinelLicense"))
    			.addTestStep(annotatedMethod(steps, "copyLicenseToOrchDepo")).build();
    	
    	TestScenario scenario = scenario("sentinelLicenseSetup").addFlow(sequence(sentinelLicenseSetup)).build();
        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }
    
}
