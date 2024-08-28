
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
import com.ericsson.oss.orchestrator.test.steps.OrcEngineInstallTestSteps;
import com.ericsson.oss.orchestrator.test.steps.OrcPkgInstallTestSteps;
import com.google.inject.Inject;

public class ConfigureFreezeMediaDownloadScenario extends TorTestCaseHelper implements TestCase {

    @Inject
    ConfigureFreezeMediaDownloadSteps steps;
        
    
    @TestId(id = "OSS-55388", title = "Configure FreezeTarFileDownload")
    @Context(context = {Context.CLI})
    @Test
    public void ConfigureFreezeMediaDownloadScenario(){

        TestStepFlow downloadconfigFreezeFileFLow;
        
        downloadconfigFreezeFileFLow = flow("Configure Freeze  tar file download")
        		    .addTestStep(annotatedMethod(steps,"DownloadToGateway"))
                    .addTestStep(annotatedMethod(steps,"VerifyDownloadedTarFile"))
                    .addTestStep(annotatedMethod(steps,"CopyTarFileToMWS"))
                    .addTestStep(annotatedMethod(steps,"VerifyTarFileExistenceOnMWS")).build();
                     TestScenario scenario = scenario("configurei FreezeTarFileDownloadScenario").addFlow(downloadconfigFreezeFileFLow).build();
                
         ScenarioListener listener = new LoggingScenarioListener();
         TestScenarioRunner runner = runner().withListener(listener).build();
         runner.start(scenario);
     }

 }
