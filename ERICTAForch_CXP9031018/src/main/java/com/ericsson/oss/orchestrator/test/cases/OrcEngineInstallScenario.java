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
import com.ericsson.oss.orchestrator.test.steps.OrcEngineInstallTestSteps;
import com.google.inject.Inject;

public class OrcEngineInstallScenario extends TorTestCaseHelper implements TestCase {

    @Inject
    OrcEngineInstallTestSteps steps;
    
    @TestId(id = "OSS-62290", title = "Install Orch Engine")
    @Context(context = {Context.CLI})
    @Test
    public void createOrcEngineInstallScenario() {

        TestStepFlow installFlow;
        
        installFlow = flow("Install Orch Engine")
                .addTestStep(annotatedMethod(steps, "checkIfEngRunning"))
                .addTestStep(annotatedMethod(steps, "downloadLatestEngine"))
                .addTestStep(annotatedMethod(steps, "createAdmFile"))
                .addTestStep(annotatedMethod(steps, "upgradeEngine"))
                .addTestStep(annotatedMethod(steps, "removeAdmFile"))
                .addTestStep(annotatedMethod(steps, "checkIfInstalled"))
                .addTestStep(annotatedMethod(steps, "verifyInstalled")).build();

        
        TestStepFlow configureFlow;
        
        configureFlow = flow("Configure Orch Engine")
                    .addTestStep(annotatedMethod(steps, "copyWorkfile"))
                    .addTestStep(annotatedMethod(steps, "createHosts"))
                    .addTestStep(annotatedMethod(steps, "copyHosts"))
                    .addTestStep(annotatedMethod(steps, "copyConfig"))
                    .addTestStep(annotatedMethod(steps, "copyIniTemplate"))
                    .addTestStep(annotatedMethod(steps, "customiseIni"))
                    .addTestStep(annotatedMethod(steps, "deleteStatusFile")).build();        
        
        TestScenario scenario = scenario("createConfigureOrchEngine").addFlow(installFlow).addFlow(configureFlow).build();

        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }

}
