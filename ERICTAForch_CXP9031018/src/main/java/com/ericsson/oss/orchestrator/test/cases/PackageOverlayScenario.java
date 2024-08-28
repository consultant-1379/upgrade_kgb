package com.ericsson.oss.orchestrator.test.cases;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.*;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.oss.orchestrator.test.steps.PackageOverlaySteps;
import com.google.inject.Inject;

public class PackageOverlayScenario extends TorTestCaseHelper implements TestCase {
	
	@Inject
	PackageOverlaySteps steps;
	
	@Context(context = {Context.CLI})
    @Test
    public void PackageOverlay() {
    	
        TestStepFlow PackageOverlay;        
        
        PackageOverlay = flow("Copy Uploaded Packages to MWS")
        		         .addTestStep(annotatedMethod(steps, "getShipmentandMediaPath"))
                         .addTestStep(annotatedMethod(steps, "CopyUploadedPackagestoMedia")).build();                    
        
        TestScenario scenario = scenario("Copy Uploaded Packages").addFlow(PackageOverlay).build();
       
        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }

}
