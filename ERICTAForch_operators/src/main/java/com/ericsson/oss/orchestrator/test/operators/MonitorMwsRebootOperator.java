package com.ericsson.oss.orchestrator.test.operators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.oss.orchestrator.test.operators.*;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.google.inject.Singleton;

@Operator(context = Context.CLI)
@Singleton
public class MonitorMwsRebootOperator extends PCJCommonOperator {

	private Logger logger = LoggerFactory
			.getLogger(MonitorMwsRebootOperator.class);
	 public  boolean wait_for_shutdown_startup=false;

	/**
	 * Checks for the existence of previous status file
	 * if exists checks it's status
	 * @param rebootStatusFilePath
	 * @param host
	 * @return boolean 
	 */
	public boolean previousStatusFileCheck(String rebootStatusFilePath,
			Host host) {
		String cmd = "ls  " + rebootStatusFilePath;
		int exitValue = 1;
		exitValue = runSingleCommandOnHost(host, cmd, true, true);
		if (exitValue == 0) {
			logger.info("Reboot tasks status  file is present so checking for the status");
			cmd = " awk  '{print $1 }' " + rebootStatusFilePath;
			exitValue = runSingleCommandOnHost(host, cmd, true, true);
			if (exitValue == 0) {
				logger.info(cmd + "got executed successfully");
				if (getLastOutput().equals("OK"))

				{
					logger.info("Previous status file status is OK ");
					return true;

				} else if (getLastOutput().equals("NOK")) {
					logger.error("Previous status file status is NOK so not proceeding further ");
					return false;
				} else {
					logger.error("Previous status file status is neither OK nor NOK");
					return false;
				}

			} else {
				logger.error(cmd + "execution got failed");
				return false;

			}

		} else if (exitValue == 2)// incase File is not present exit code will
									// be 2
		{

			logger.info(" Previous  status file does not exist and setting the attribute to wait_for_shutdown_startup "
					+ wait_for_shutdown_startup);
			wait_for_shutdown_startup = true;
			return true;
		} else {
			logger.error(cmd + "execution got failed");
			return false;

		}

	}

	
    /**checks for hidden file StartupFile
     * 
     * @param host
     * @param shutdownfilePath
     * @param startupFilePath
     * @param maxMCWaitTime
     * @return int 
     */
	public int checkForStartupFile(Host host, String shutdownfilePath,
			String startupFilePath, String maxMCWaitTime) {
		String cmd = "ls  " + shutdownfilePath;
		int exitValue = 1;
		int iterations = Integer.parseInt(maxMCWaitTime);
		for (int i = 1; i <= iterations; i++) {
			exitValue = runSingleCommandOnHost(host, cmd, true, true);
			if (exitValue == 2) {
				logger.info(shutdownfilePath
						+ "is not present on server and this is  " + i
						+ "  attempt");
				cmd = "ls  " + startupFilePath;
				exitValue = runSingleCommandOnHost(host, cmd, true, true);
				if (exitValue == 2) {
					logger.info(startupFilePath
							+ "is not  present on server and this is  " + i
							+ "  attempt");
					logger.info("intermediate state is reached  and waiting for startup file now");
					putDelay(60000);

				} else if (exitValue == 0) {
					logger.info(startupFilePath
							+ "is present on server and this is  " + i
							+ "  attempt");
					exitValue = 0;
					return exitValue;

				} else

				{
					logger.error(cmd + "execution got failed");
					return exitValue;
				}

			} else if (exitValue == 0) {
				logger.info(shutdownfilePath + "is present on server even in "
						+ i + "attempt");
				logger.info("waiting for 1 minute and repeating checks");
				putDelay(60000);
			} else {

				logger.error(cmd + "execution got failed");
				return exitValue;
			}
		}

		logger.error("even after time out startup file is not in place / shutdown file stil present on MWS .Please check the logs");
		return exitValue;
	}

	

}
