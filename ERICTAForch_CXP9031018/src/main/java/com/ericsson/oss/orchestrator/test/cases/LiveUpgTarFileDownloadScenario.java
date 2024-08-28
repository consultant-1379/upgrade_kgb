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
import com.ericsson.oss.orchestrator.test.steps.LiveUpgTarFileDownloadTestSteps;
import com.ericsson.oss.orchestrator.test.steps.OrcEngineInstallTestSteps;
import com.ericsson.oss.orchestrator.test.steps.OrcPkgInstallTestSteps;
import com.google.inject.Inject;

public class LiveUpgTarFileDownloadScenario extends TorTestCaseHelper implements TestCase {

    @Inject
    LiveUpgTarFileDownloadTestSteps steps;
    
    @TestId(id = "OSS-55390", title = "LiveUpgTarFileDownload")
    @Context(context = {Context.CLI})
    @Test
    public void liveUpgTarFileDownloadScenario(){

        TestStepFlow downloadTafFileFLow;
        
        downloadTafFileFLow = flow("Download Live Upgrade tar file")
        		    .addTestStep(annotatedMethod(steps,"DownloadToGateway"))
                    .addTestStep(annotatedMethod(steps,"VerifyDownloadedTarFile"))
                    .addTestStep(annotatedMethod(steps,"CopyTarFileToMWS"))
                    .addTestStep(annotatedMethod(steps,"VerifyTarFileExistenceOnMWS")).build();
                 TestScenario scenario = scenario("LiveUpgTarFileDownloadScenario").addFlow(downloadTafFileFLow).build();
                
         ScenarioListener listener = new LoggingScenarioListener();
         TestScenarioRunner runner = runner().withListener(listener).build();
         runner.start(scenario);
     }

 }