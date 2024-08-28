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
import com.ericsson.oss.orchestrator.test.steps.CleanupUpgradeSteps;
import com.ericsson.oss.orchestrator.test.steps.CreateDepoSteps;
import com.ericsson.oss.orchestrator.test.steps.SetupOrchaUserSteps;
import com.google.inject.Inject;

public class CleanupUpgradeScenario extends TorTestCaseHelper implements TestCase {
    
    @Inject
    CleanupUpgradeSteps steps;
    // Some of the steps we want are already defined in other step files, so re-use those
    // rather than duplicate code
    @Inject
    SetupOrchaUserSteps orchaSteps;
    @Inject
    CreateDepoSteps depoSteps;
    
    @TestId(id = "OSS-63169_1", title = "Cleanup upgrade")
    @Context(context = {Context.CLI})
    @Test
    public void cleanupScenario() {
        
        TestStepFlow cleanupFlow; 
        TestStepFlow cleanupOrchaFlow;
        
        cleanupFlow = flow("Cleanup upgrade")
                 .addTestStep(annotatedMethod(depoSteps, "getAdminHostname"))  
                 .addTestStep(annotatedMethod(steps, "setOssrcCache"))  
                 .addTestStep(annotatedMethod(depoSteps, "getShipmentandRstate"))
                 .addTestStep(annotatedMethod(depoSteps, "removeOldDepo"))      
                 .addTestStep(annotatedMethod(depoSteps, "cleanupOldDepo")) 
                 .addTestStep(annotatedMethod(steps, "verifyDepoRemoved"))
                 .addTestStep(annotatedMethod(steps, "removeLdapProxy"))
                 .addTestStep(annotatedMethod(steps, "verifyNoLdapProxy")).build();
        
        cleanupOrchaFlow = flow("Cleanup orcha")
                .addTestStep(annotatedMethod(steps, "removeUASPasswordless"))
                .addTestStep(annotatedMethod(steps, "removeEBASPasswordless"))
                .addTestStep(annotatedMethod(steps, "removePEERPasswordless"))
                .addTestStep(annotatedMethod(orchaSteps, "createOrchaHostsFile"))
                .addTestStep(annotatedMethod(orchaSteps, "removeOrchaUser"))
                .addTestStep(annotatedMethod(orchaSteps, "removeOrchaHostsFile")) 
                .addTestStep(annotatedMethod(steps, "verifyNoMWSPasswordless"))
                .addTestStep(annotatedMethod(steps, "verifyNoUASPasswordless"))
                .addTestStep(annotatedMethod(steps, "verifyNoEBASPasswordless"))
                .addTestStep(annotatedMethod(steps, "verifyNoPEERPasswordless"))                
                .addTestStep(annotatedMethod(steps, "verifyNoOrchaUser"))
                .build();
                    
        TestScenario scenario = scenario("Cleanup upgrade").addFlow(cleanupFlow).addFlow(cleanupOrchaFlow).build();
       
        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }
}