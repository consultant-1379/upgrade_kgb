package com.ericsson.oss.orchestrator.test.steps;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.orchestrator.test.operators.WfEngOperator;
import com.google.inject.Inject;

public class OrcEngineInstallTestSteps extends TorTestCaseHelper {

    final static Logger logger = LoggerFactory.getLogger(OrcEngineInstallTestSteps.class);
    
    private static final String ALREADY_INSTALLED = "ENGINE_INSTALLED";
    private static final String ENGINE_RUNNING = "ENGINE_RUNNING";
    private static final String INSTALL_PATH = "/var/tmp"; // Location of where going
                                                      // to download latest orch
                                                      // engine to

    @Inject
    private WfEngOperator engOperator;
    
    /**
     * Tests if workflow engine is installed (e.g. help is successful) and sets
     * ALREADY_INSTALLED attribute accordingly
     */
    @TestStep(id = "checkIfInstalled")
    public void checkIfInstalled() {
        setTestStep("checkIfInstalled");
        User orchaUser = engOperator.getMsHost().getUsers(UserType.OPER).get(0);
        engOperator.initialize(engOperator.getMsHost(), orchaUser);
        Shell shell = engOperator.executeWfEng("--help");
        engOperator.readUntilClosed(shell, false);
        int helpExit = engOperator.getShellExitValue(shell);
        TestContext context = TafTestContext.getContext();
        if (helpExit == 0) {
            context.setAttribute(ALREADY_INSTALLED, true);
        } else {
            context.setAttribute(ALREADY_INSTALLED, false);
        }
        assertNotNull(context.getAttribute(ALREADY_INSTALLED));
    }

    /**
     * Tests if workflow engine is running and sets ENGINE_RUNNING attribute
     * accordingly
     */
    @TestStep(id = "checkIfEngRunning")
    public void checkIfEngRunning() {
        setTestStep("checkIfEngRunning");
        TestContext context = TafTestContext.getContext();
        boolean running = engOperator.isOrchEngineRunning();
        context.setAttribute(ENGINE_RUNNING, running);
        logger.info("Engine already running? " + running);
        assertNotNull(context.getAttribute(ENGINE_RUNNING));
        String runStr = (String)TafConfigurationProvider.provide().getProperty("testware.stop_if_engine_running");
        if ("true".equalsIgnoreCase(runStr)) {
            logger.info("Configured to stop if engine is running");
            assertFalse("Engine running so stop", running);
        }
    }


    
    /**
     * Verifies workflow engine is installed
     */
    @TestStep(id = "downloadLatestEngine")
    public void downloadLatestEngine() {
        setTestStep("downloadLatestEngine");
        // TODO: Download latest ERICorch.pkg from Nexus

    }

    /**
     * Copies the default.adm to the MWS
     */
    @TestStep(id = "createAdmFile")
    public void createAdmFile() {
        setTestStep("createAdmFile");
        boolean created = engOperator.createAdmFile();
        assertTrue("Failed to copy default.adm", created);
    }

    /**
     * Removes the default.adm from the MWS
     */
    @TestStep(id = "removeAdmFile")
    public void removeAdmFile() {
        setTestStep("removeAdmFile");
        boolean removed = engOperator.removeAdmFile();
        assertTrue("Failed to remove default.adm", removed);
    }

    /**
     * Verifies workflow engine is installed
     */
    @TestStep(id = "verifyInstalled")
    public void verifyInstalled() {
        setTestStep("Verify workflow installed");
        TestContext context = TafTestContext.getContext();
        boolean installed = context.getAttribute(ALREADY_INSTALLED);
        assertTrue(installed);
    }

    /**
     * Installs the workflow engine from the latest
     */
    @TestStep(id = "upgradeEngine")
    public void upgradeEngine() {
        setTestStep("Upgrade Orchestrator Engine");
        TestContext context = TafTestContext.getContext();
        boolean running = context.getAttribute(ENGINE_RUNNING);
        if (running) {
            logger.warn("Cannot upgrade engine as it is already running");
        } else {
            logger.info("Able to upgrade engine as its not running");
            assertTrue("Upgraded engine",
                    engOperator.upgradeEngine(INSTALL_PATH));
        }
    }

    /**
     * Copies the default full_ossrc_workflow.xml to the MWS
     */
    @TestStep(id = "copyWorkfile")
    public void copyWorkfile() {
        setTestStep("Copy workflow file");
        
        // Now put latest workflow files into place from workflow directory
        List<String> workFiles = engOperator.getWorkflowFilenames();
        for (String workFile : workFiles) {
            boolean copied = engOperator.copyWfEngEtcFileToMWS(workFile);
            assertTrue("Failed to copy workflow XML file", copied);
        }
        
    }

    /**
     * Creates a hosts xml on the local node from the details of nodes passed to
     * test through -Dhost... variables
     */
    @TestStep(id = "createHosts")
    public void createHosts() {
        setTestStep("Create hosts file locally");
        // create Hosts file
        assertTrue(engOperator.createHostsFile());

    }

    /**
     * Copies the hosts file from local system to MWS
     */
    @TestStep(id = "copyHosts")
    public void copyHosts() {
        setTestStep("Copy hosts file");
        String hostsFile = engOperator.getHostsFile();
        boolean copied = engOperator.copyWfEngEtcFileToMWS(hostsFile);
        assertTrue("Failed to copy hosts XML file", copied);
    }

    /**
     * Copies the default wfeng.cfg to the MWS
     */
    @TestStep(id = "copyConfig")
    public void copyConfig() {
        setTestStep("Copy config");
        String localFile = "wfeng.cfg";
        boolean copied = engOperator.copyWfEngCfgFileToMWS(localFile);
        assertTrue("Failed to copy wfeng.cfg", copied);
    }

    /**
     * Copies the wfeng.oss_ini_template to the MWS
     */
    @TestStep(id = "copyIniTemplate")
    public void copyIniTemplate() {
        setTestStep("Copy ini template");
        String localFile = WfEngOperator.INI_TEMPLATE;
        boolean copied = engOperator.copyWfEngCfgFileToMWS(localFile);
        assertTrue("Failed to copy wfeng.oss_ini_template", copied);
    }

    /**
     * Customises the workflow ini file with details from TafConfiguration
     */
    @TestStep(id = "customiseIni")
    public void customiseIni() {
        setTestStep("Customise ini");
        // Customise ini file
        User orchaUser = engOperator.getMsHost().getUsers(UserType.OPER).get(0);
        engOperator.initialize(engOperator.getMsHost(), orchaUser);
        int sedExit = engOperator.customiseIniFile(TafConfigurationProvider
                .provide());
        assertEquals("Failed to customise ini file", 0, sedExit);
    }

    /**
     * If requested then deletes status file
     */
    @TestStep(id = "deleteStatusFile")
    public void deleteStatusFile() {
        setTestStep("Determining whether to delete status file");
        TafConfiguration configuration = TafConfigurationProvider.provide();
        String deleteStatusFileStr = (String) configuration
                .getProperty("testware.delete_status_file");
        boolean deleteStatusFile = Boolean.parseBoolean(deleteStatusFileStr);
        String hostsFile = engOperator.getHostsFile();
        if (deleteStatusFile) {
            List<String> workFiles = engOperator.getWorkflowFilenames();
            for (String workFile : workFiles) {
                String statusFilename = engOperator.getStatusFilename(
                        engOperator.getWfEngEtc() + "/" + workFile.substring(workFile.lastIndexOf("/")+1),
                        hostsFile);
                logger.info("Attempting to delete: " + deleteStatusFile);
                Shell shell = engOperator.executeCommand("rm -f " + statusFilename);
                engOperator.readUntilClosed(shell, true);
                int rmExit = engOperator.getShellExitValue(shell);

                assertEquals("Failed to remove old status file " + deleteStatusFile, 0, rmExit);
                logger.info("Deleted old status file " + deleteStatusFile);

            }
            
        } else {
            logger.info("Requested to not delete old status file");
        }
    }

   
}

