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
import com.ericsson.oss.orchestrator.test.steps.OrcEngineInstallTestSteps;
import com.ericsson.oss.orchestrator.test.steps.OrcPkgInstallTestSteps;
import com.google.inject.Inject;


public class OrcPkgInstallScenario extends TorTestCaseHelper implements TestCase {

    @Inject
    OrcPkgInstallTestSteps steps;
    
    @TestId(id = "OSS-55459_FUNC_1", title = "Install Orch Pkg")
    @Context(context = {Context.CLI})
    @Test
    public void createOrcPkgInstallScenario() {

        TestStepFlow installFlow;
        
        installFlow = flow("Install Orch Pkg")
                    .addTestStep(annotatedMethod(steps,"CheckMediaPathExistence"))
                    .addTestStep(annotatedMethod(steps,"PkgorchInstall"))
                    .addTestStep(annotatedMethod(steps,"OrchPkginfo"))
                    .addTestStep(annotatedMethod(steps,"CheckOrchaUserExistence"))
                    .addTestStep(annotatedMethod(steps,"CheckOrchpathExistence")).build();
                 TestScenario scenario = scenario("createOrcPkgInstallScenario").addFlow(installFlow).build();
                
         ScenarioListener listener = new LoggingScenarioListener();
         TestScenarioRunner runner = runner().withListener(listener).build();
         runner.start(scenario);
     }

 }