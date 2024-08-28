/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
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
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.ericsson.oss.orchestrator.test.steps.ConfigureEBAMCsTestSteps;
import com.google.inject.Inject;

public class ConfigureEBAMCsScenario extends TorTestCaseHelper  implements TestCase  {
    
    final static Logger logger = LoggerFactory.getLogger(ConfigureEBAMCsScenario.class);
    @Inject
    ConfigureEBAMCsTestSteps steps;
        
    @TestId(id = "OSS-55397", title = "OSS-RC ORCH CDB: PCJ Configure EBAS EBA MCs")
    @Test
    public void configureEBAMCsScenario() {
                
        TestStepFlow EBAMCs;
                        
        EBAMCs = flow ( "ConfigureEBAMCs")
                    .addTestStep(annotatedMethod(steps, "checkETCHosts"))
                    .addTestStep(annotatedMethod(steps, "checkEBAprocesses")).build();
                                                        
        TestScenario scenario = scenario ("ConfigureEBAMCs").addFlow(EBAMCs).build();

        ScenarioListener listener = new LoggingScenarioListener();
        TestScenarioRunner runner = runner().withListener(listener).build();
        runner.start(scenario);
    }

}
