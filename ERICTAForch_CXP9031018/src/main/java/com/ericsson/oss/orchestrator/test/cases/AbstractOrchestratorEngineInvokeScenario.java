/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.orchestrator.test.cases;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import org.testng.annotations.Test;
import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.google.inject.Inject;
import com.ericsson.oss.orchestrator.test.steps.OrchEngineInvokeSteps;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;

/**
 * Abstract test case for running orcha engine against a workflow. Parameters are controlled by a data-source.
 * An abstract class is used so that if it is required to run multiple workflow files in the same suite, then you can as long as you create
 * two sub-classes.
 * For instance if want to run engine, run another test, resume engine, analyse engine results then there will need to be two invokes of the engine
 * in one suite. So this would need to be done with either separate classes to run engine if run in same suite, or run in different suites
 * @author xmcgama
 *
 */
public abstract class AbstractOrchestratorEngineInvokeScenario extends TorTestCaseHelper implements TestCase {
	
	@Inject
	OrchEngineInvokeSteps steps;
	
    public void runEngine(String dataSourceName) {
    	
        TestStepFlow engFlow;        
        
        engFlow = flow("Run " + dataSourceName)   		
                    .addTestStep(annotatedMethod(steps, "runEngine"))
        		    .addTestStep(annotatedMethod(steps, "getLogFile"))
        		    .addTestStep(annotatedMethod(steps, "getMasterStatusFile"))
        		    .addTestStep(annotatedMethod(steps, "publishToAllure"))
                    .addTestStep(annotatedMethod(steps, "parseMasterStatusFile"))
                    .addTestStep(annotatedMethod(steps, "parseLogFile"))
                    .addTestStep(annotatedMethod(steps, "verifyEngineRanSuccessfully"))
                    .withDataSources(dataSource(dataSourceName)).build();
                           
        TestScenario scenario = scenario("Run"+dataSourceName).addFlow(engFlow).build();
       
        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }
    

}
