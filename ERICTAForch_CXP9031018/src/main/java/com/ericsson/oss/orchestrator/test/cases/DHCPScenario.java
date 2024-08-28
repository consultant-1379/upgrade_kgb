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
import com.ericsson.oss.orchestrator.test.steps.DHCPTestSteps;
import com.google.inject.Inject;

public class DHCPScenario extends TorTestCaseHelper implements TestCase {

    @Inject
    DHCPTestSteps steps;
    
    @TestId(id = "OSS-75028", title = "Setup admin as DHCP client on mws")
    @Context(context = {Context.CLI})
    @Test
    public void createOrcEngineInstallScenario() {
        
        TestStepFlow configureFlow;
        
        configureFlow = flow("Add node as a DHCP client")
        		    .addTestStep(annotatedMethod(steps, "checkFor2Node"))
                    .addTestStep(annotatedMethod(steps, "copyIniTemplate"))
                    .addTestStep(annotatedMethod(steps, "customiseIni"))
                    .addTestStep(annotatedMethod(steps, "removeDHCPClient"))
                    .addTestStep(annotatedMethod(steps, "addDHCPClient")).build();        
        
        TestScenario scenario = scenario("AddNodeDHCPclient").addFlow(configureFlow).build();

        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }

}
