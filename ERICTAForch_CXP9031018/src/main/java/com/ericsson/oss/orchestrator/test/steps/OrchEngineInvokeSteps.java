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
package com.ericsson.oss.orchestrator.test.steps;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.reporters.Files;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Attachment;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.orchestrator.test.operators.EngineTaskStatusDataSource;
import com.ericsson.oss.orchestrator.test.operators.WfEngOperator;
import com.google.inject.Inject;

public class OrchEngineInvokeSteps extends TorTestCaseHelper {

    private final Logger logger = LoggerFactory.getLogger(OrchEngineInvokeSteps.class);
    
   
    @Inject
    private WfEngOperator op;
    private static final String ENGINE_EXIT_CODE = "ENGINE_EXIT_CODE";
          
    
    @TestStep(id="runEngine", description = "Run engine, workfile: {0}")
    public void runEngine(@Input("wfile") String workfile,
                          @Input("args") String args) {
    	
    	setTestStep("runEngine");
    	
    	EngineTaskStatusDataSource.empty();
    	
    	TestContext context = TafTestContext.getContext();
    	
    	String argstr = "";
        if (args != null) {
            argstr = args;
        }
         
        // NB. The iniFile is placed on the MWS by the OrcEngineInstallScenario - which has
        // created it from a template. So it will already be on system
        String iniFile = op.getWfEngCfg() + "/wfeng_" + op.getAdminHost1() + ".ini";
        String hostfile = op.getDefaultHostsFile();
        Host msHost = op.getMsHost();
                
        String cmdLine = "-w " + workfile + " -H " + hostfile + " -i " + iniFile + " --allow-multiple " + argstr;

        int exitCode = op.runSingleCommandOnHost(msHost, op.getUserByName(msHost, "orcha"), op.getWfEngCmd(cmdLine), false, true);
        
        op.displayStatusFile(workfile, hostfile);

        // Just save exit code, as we want to extract artifacts and generate data source before we check exit code
        context.setAttribute(ENGINE_EXIT_CODE, exitCode);
        
        assertNotNull(context.getAttribute(ENGINE_EXIT_CODE));
    	            
    }
    
    @TestStep(id="getLogFile")
    public void getLogFile(@Input("wfile") String workfile) {
        
        setTestStep("getLogFile");
        
        Host msHost = op.getMsHost();
        
        // Work out last log file
        String logPrefix = op.getDefaultLogPrefix(workfile);
        int exitCode = op.runSingleCommandOnHost(msHost, "ls -t " + op.getWfEngLog() + "/" + logPrefix + "*.log | head -1", false, false);
        assertEquals(exitCode, 0);
        
        // Verify filename is not null, as if ls failed, then we would still have 0 exit code from head
        assertNotNull("Log filename is null", op.getLastOutput());
        assertTrue("Log filename is empty string", op.getLastOutput().length() > 0);
        
        // Retrieve log file
        String fileName = op.getLastOutput().trim();
        
        boolean received = op.getRemoteFile(msHost, op.getUserByName(msHost, "root"), fileName, logPrefix + ".log");        
        assertTrue("Failed to get log file: " + fileName, received);
    }
    
    @TestStep(id="getMasterStatusFile")
    public void getMasterStatusFile(@Input("wfile") String workfile) {
        
        setTestStep("getMasterStatusFile");
        Host msHost = op.getMsHost();
        String hostfile = op.getDefaultHostsFile();
        String statusFilename = op.getStatusFilename(workfile, hostfile);
        boolean received = op.getRemoteFile(msHost, op.getUserByName(msHost, "root"), statusFilename, op.getFilename(statusFilename));        
        assertTrue("Failed to get status file: " + statusFilename, received);                    
    }

    /**
     * Parse the master status file and save as ENGINE_STATUS dataSource
     * @param workfile
     */
    @TestStep(id="parseMasterStatusFile")
    public void parseMasterStatusFile(@Input("wfile") String workfile) {
      
        setTestStep("parseMasterStatusFile");
        Host msHost = op.getMsHost();
        String hostfile = op.getDefaultHostsFile();
       
        String statusFilename = op.getStatusFilename(workfile, hostfile);
        boolean parsed = op.parseStatusFile(op.getFilename(statusFilename));
        assertTrue("Failed to parse status file", parsed);
                    
    }
    
    /**
     * Parse the logfile and saves any error messages found to the ENGINE_STATUS dataSource
     * @param workfile
     */
    @TestStep(id="parseLogFile")
    public void parseLogFile(@Input("wfile") String workfile) {
      
        setTestStep("parseMasterStatusFile");
        Host msHost = op.getMsHost();
        String hostfile = op.getDefaultHostsFile();
       
        String logFile = op.getDefaultLogPrefix(workfile) + ".log";
        boolean parsed = op.parseLogFile(op.getFilename(logFile));
        assertTrue("Failed to parse log file", parsed);
                    
    }
    
    /**
     * Publish to allure master status file and log file
     */
    @TestStep(id="publishToAllure")
    public void publishToAllure(@Input("wfile") String workfile) {
        setTestStep("publishToAllure");
        makeLogAttachment(workfile);
        makeStatusAttachment(workfile);
    }
    
    @Attachment(type = "text/plain")
    public String makeLogAttachment(String workfile) {
        try {
            return Files.readFile(new File(op.getDefaultLogPrefix(workfile) + ".log"));
        } catch (IOException e) {
            logger.error("Failed to read file: " + e.getMessage());
            return null;
        }
    }

    @Attachment(type = "application/xml")
    public String makeStatusAttachment(String workfile) {
        try {
            String hostfile = op.getDefaultHostsFile();
            String statusFilename = op.getStatusFilename(workfile, hostfile);
            return Files.readFile(new File(op.getFilename(statusFilename)));
        } catch (IOException e) {
            logger.error("Failed to read file: " + e.getMessage());
            return null;
        }
    }
    
    @TestStep(id="verifyEngineRanSuccessfully")
    public void verifyEngineRanSuccessfully(@Input("exitCode") Integer exitCode) {
        
        setTestStep("verifyEngineRanSuccessfully");
        
        TestContext context = TafTestContext.getContext();

        int actualExitCode = context.getAttribute(ENGINE_EXIT_CODE);
        assertEquals("Engine did not run successfully", actualExitCode, exitCode.intValue());
                    
    }
    
       
}