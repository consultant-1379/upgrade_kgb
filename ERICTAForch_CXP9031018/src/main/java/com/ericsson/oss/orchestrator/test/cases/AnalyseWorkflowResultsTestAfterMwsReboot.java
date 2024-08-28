package com.ericsson.oss.orchestrator.test.cases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.oss.orchestrator.test.operators.WfEngOperator;
import com.google.inject.Inject;

public class AnalyseWorkflowResultsTestAfterMwsReboot extends TorTestCaseHelper implements TestCase {
    
    final static Logger logger = LoggerFactory.getLogger(AnalyseWorkflowResultsTestAfterMwsReboot.class);
     
    @Inject
    private WfEngOperator op;
    
    @TestId(id = "OSS-55692_2", title = "Engine task analysis")
    @Test(groups={"Acceptance"})
    @DataDriven(name = WfEngOperator.ENGINE_STATUS_DATA_SOURCE)
    public void verifyEngineTask(
             @Input(WfEngOperator.TASK_ID)String taskid,
             @Input(WfEngOperator.STATUS)String status,
             @Input(WfEngOperator.DURATION)String duration,
             @Input(WfEngOperator.ESTIMATED_DUR)String estimated,
             @Input(WfEngOperator.ERROR_MSGS)String errorMsgs
             ) throws InterruptedException, TimeoutException { 
        verifyTask(taskid, status, duration, estimated, errorMsgs);
        
    }     
    
    @TestStep(id="verifyTask", description = "Analyse engine results: Task {0}")
    public void verifyTask(String taskId, String status, String durationStr, String estimatedStr, String errorStrings) {
        logger.info("TASK ANALYSIS id: " + taskId + ", status: " + status + ", duration: " + durationStr + ", estimated: " + estimatedStr);
        if (estimatedStr.length() > 0 && durationStr.length() > 0) {
            // Check if duration > estimated
            Float estimated = Float.parseFloat(estimatedStr);
            Float duration = Float.parseFloat(durationStr);
            if (duration > estimated) {
                logger.warn("WARNING: Duration " + duration + " was greater than " + estimated);
            }
        }
        if (errorStrings != null && errorStrings.length() > 0) {
            logger.error("Errors found for task " + taskId + ":");
            logger.error(errorStrings);
        }
        assertTrue("Invalid status for task: " + taskId + ", status: " + status, op.isSuccessStatus(status));
        
    }
}

