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
import com.ericsson.oss.orchestrator.test.steps.CheckStatusOfMcBeforeUpgradeTestSteps;
import com.ericsson.oss.orchestrator.test.steps.ConfigureFreezeMediaDownloadSteps;
import com.ericsson.oss.orchestrator.test.steps.LiveUpgTarFileDownloadTestSteps;
import com.ericsson.oss.orchestrator.test.steps.OrcEngineInstallTestSteps;
import com.ericsson.oss.orchestrator.test.steps.OrcPkgInstallTestSteps;
import com.google.inject.Inject;

public class  CheckStatusOfMcBeforeUpgradeScenario extends TorTestCaseHelper implements TestCase {

    @Inject
   CheckStatusOfMcBeforeUpgradeTestSteps steps;
        
    
    @TestId(id = "OSS-66767", title = "CheckStatusOfMcBeforeUpgrade")
    @Context(context = {Context.CLI})
    @Test
    public void CheckStatusOfMcBeforeUpgradeScenario(){

        TestStepFlow checkStatusOfMcFLow;
        
        checkStatusOfMcFLow = flow("Check status of Mc before upgrade")
        		
        		    .addTestStep(annotatedMethod(steps,"Get Admin server with OSS Grp running"))
        		    .addTestStep(annotatedMethod(steps,"Verify MC status "))
        		    .addTestStep(annotatedMethod(steps,"Restart MC or Online  MC"))
                    .addTestStep(annotatedMethod(steps,"Prepare critical MC list from Media"))
                    .addTestStep(annotatedMethod(steps,"Verify Critical MC and other MC status"))
                    .addTestStep(annotatedMethod(steps,"Cleanup Mount and Direcorty paths"))
                    .addTestStep(annotatedMethod(steps,"Fail /success the testcase based on Critical Mc and other MC status")).build();
        
                    TestScenario scenario = scenario("CheckStatusOfMcBeforeUpgradeScenario").addFlow(checkStatusOfMcFLow).build();
                
         ScenarioListener listener = new LoggingScenarioListener();
         TestScenarioRunner runner = runner().withListener(listener).build();
         runner.start(scenario);
     }

 }