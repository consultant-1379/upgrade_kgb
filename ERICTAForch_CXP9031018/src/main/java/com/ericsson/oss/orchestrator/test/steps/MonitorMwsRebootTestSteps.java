package com.ericsson.oss.orchestrator.test.steps;

import org.testng.AssertJUnit;
import org.testng.AssertJUnit;
import org.testng.AssertJUnit;
import org.testng.AssertJUnit;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.oss.orchestrator.test.operators.LiveUpgTarFileDownloadOperator;
import com.ericsson.oss.orchestrator.test.operators.MonitorMwsRebootOperator;
import com.ericsson.oss.orchestrator.test.operators.OrchPkgOperator;
import com.ericsson.oss.orchestrator.test.operators.WfEngOperator;
import com.google.inject.Inject;

public class MonitorMwsRebootTestSteps extends TorTestCaseHelper {

	final static Logger logger = LoggerFactory
			.getLogger(MonitorMwsRebootTestSteps.class);

	@Inject
	private MonitorMwsRebootOperator monitorMwsRebootOperator;
	public static String shutDownFileLocation = "/tmp/.eric_server_going_down";
	public static String startupFileLocation = "/tmp/.eric_server_starting_up";
	public static String rebootStatusFilePath = "/var/log/ericsson/.slu_reboot_tasks_status_file";
	public static String maxRebootWaitTime;

	@TestStep(id = "Previous status file check")
	public void previousStatusFileCheck() {
		setTestStep("Previous status file check");

		assertTrue(
				"Previous status file is having NOK or command not executed properly please check logs",
				monitorMwsRebootOperator.previousStatusFileCheck(
						rebootStatusFilePath,
						monitorMwsRebootOperator.getMsHost()));

	}

	@TestStep(id = "check for shutdown file")
	public void checkforShutdownFile() {
		setTestStep("check for shutdown file");

		if (monitorMwsRebootOperator.wait_for_shutdown_startup) {
			logger.info(" skipping check for shutdown file teststep as previous run status file is not in place");

		} else {

			TafConfiguration configuration = TafConfigurationProvider.provide();
			maxRebootWaitTime = (String) configuration
					.getProperty("testware.maxMwsRebootWaitTimeMinutes");
			assertNotNull("maxMWSReboot wait time is  NULL  ",
					maxRebootWaitTime);
			logger.info("maxWC wait time that can be waited to check  shutdown file on mws in /tmp path is "
					+ maxRebootWaitTime);
			assertEquals("shutdown file is not present in the path",
					monitorMwsRebootOperator.checkforFileAvailability(
							monitorMwsRebootOperator.getMsHost(),
							shutDownFileLocation, maxRebootWaitTime), 0);

		}
	}

	@TestStep(id = "check for ssh connection")
	public void checkforSshConnection() {
		setTestStep("check for ssh connection");
		if (monitorMwsRebootOperator.wait_for_shutdown_startup) {
			logger.info(" skipping check for ssh connection teststep as previous run status file is not in place");

		} else {

			assertEquals("shutdown file is not present in the path",
					monitorMwsRebootOperator.checkforSshConnection(
							monitorMwsRebootOperator.getMsHost().getHostname(),
							maxRebootWaitTime), 0);

		}
	}

	@TestStep(id = "check for startup file")
	public void checkForStartupFile() {
		setTestStep("check for startup file");
		if (monitorMwsRebootOperator.wait_for_shutdown_startup) {
			logger.info(" skipping check for startup file teststep as previous run status file is not in place");

		} else {
			logger.info("Max wait time that can be waited to check startup file in /tmp path is  "
					+ maxRebootWaitTime);
			assertEquals(
					"startup  file is not present in the path even after timeout",
					monitorMwsRebootOperator.checkForStartupFile(
							monitorMwsRebootOperator.getMsHost(),
							shutDownFileLocation, startupFileLocation,
							maxRebootWaitTime), 0);

		}
	}

}
