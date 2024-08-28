package com.ericsson.oss.orchestrator.test.cases;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import org.testng.annotations.Test;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.google.inject.Inject;
import com.ericsson.oss.orchestrator.test.steps.CacheLatestMediaOnMwsTestSteps;
import com.ericsson.oss.orchestrator.test.steps.SybaseBackupTestSteps;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.ExecutionMode;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.cifwk.taf.TestContext;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.*;


public class CacheMediaandSybaseBackupScenario extends TorTestCaseHelper implements TestCase {	
	
    @Inject
    CacheLatestMediaOnMwsTestSteps steps;
    
    @Inject
    SybaseBackupTestSteps steps1;
   
    @TestId(id = "OSS_66811_1", title = "OSS-RC ORCH CDB: Cache media and Sybase backup")
    @Test
    public void cacheMediaandSybaseBackup() {
    	TestStepFlow cacheLatestMedia;
    	//Creating DataSource 
    	TestContext context = TafTestContext.getContext();
    	String media=(String)DataHandler.getAttribute("media");
    	if ( media != null && ! media.isEmpty() ){
    		String[] mediaList = media.toString().split("\\ ");
        	for (int i=0; i<mediaList.length; i++) {
        		context.dataSource("Media").addRecord()
        		.setField("media", mediaList[i].toUpperCase());
        	}	
    	}
    	// Creating flow with above created DataSource as input.
    	cacheLatestMedia = flow("Checking cache media on mws")
    			.addTestStep(annotatedMethod(steps, "checkCachedMediaOnMws"))
    			.addTestStep(annotatedMethod(steps, "downloadLatestMedia"))
    			.addTestStep(annotatedMethod(steps, "mountMedia"))
    			.addTestStep(annotatedMethod(steps, "upgradeERICjump"))
    			.addTestStep(annotatedMethod(steps, "cacheMediaOnMws"))
    			.addTestStep(annotatedMethod(steps, "umountMediaOnMws"))
    			.withDataSources(dataSource("Media")).build();
    	
    	TestStepFlow sybasebackup;

        sybasebackup = flow ( "SybaseBackup")
        		    .addTestStep(annotatedMethod(steps1, "checkMCStatus"))
                    .addTestStep(annotatedMethod(steps1, "checkAndCreateSybaseBackup")).build();
    	
    	TestScenario scenario = scenario("cacheLatestMediaandSybaseBackup").addFlow(cacheLatestMedia)
    			                                                           .addFlow(sybasebackup)
                                                                           .withExecutionMode(ExecutionMode.PARALLEL)
    			                                                           .build();
        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    	
    }    
}
