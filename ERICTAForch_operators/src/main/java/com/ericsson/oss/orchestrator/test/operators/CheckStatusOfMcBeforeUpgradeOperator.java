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
public class CheckStatusOfMcBeforeUpgradeOperator extends PCJCommonOperator {


	private Logger logger = LoggerFactory
			.getLogger(CheckStatusOfMcBeforeUpgradeOperator.class);

	/**
	 * verifies critical Mc status and MC intermediate state status
	 * 
	 * @param host
	 * @return int value
	 */
	public int verifyCriticalMcStatus() {
		String cmd;
		cmd = "/opt/ericsson/nms_cif_sm/bin/smtool -list ";
		int exitValue = runSingleCommandOnHost(PCJCommonOperator.ossGrpAdminServerHost, cmd, false, true);

		if (exitValue == 0) {
			logger.info(cmd + " got executed successfully");
			cmd = "/opt/ericsson/nms_cif_sm/bin/smtool -list | grep -v started | awk  '{print $1}'";
			 exitValue = runSingleCommandOnHost(PCJCommonOperator.ossGrpAdminServerHost, cmd, false, true);
			 			
			if (exitValue == 0) {
				logger.info(cmd + " got executed successfully");
				String nonstartedMC[] = getLastOutput().split("\n");			
								
					
				if ( getLastOutput().contains(".*(initializing|retrying|terminating|restart scheduled|no ssr).*")) {
					logger.info("some of the other MC's are Still in intermediate state");
					criticalMcOffline = false;
					logger.info("Setting the attribute to criticalMcoffline to "
							+ criticalMcOffline);
				}

				cmd = "cat /tmp/usck_mc_loc/ERICusck/reloc/sck/lib/mc_start_list ";
				exitValue = runSingleCommandOnHost(PCJCommonOperator.ossGrpAdminServerHost, cmd, false, true);
				if (exitValue == 0) {
					logger.info(cmd + " got executed successfully");
					String criticalMcName[] = getLastOutput().split("\n");
					logger.info("nonstarted mc list is  " + nonstartedMC);
					for (int j = 0;j<nonstartedMC.length;j++){
															
						nonstartedMC[j]=nonstartedMC[j].trim();
						
					  for (int i = 0; i < criticalMcName.length; i++) {
						criticalMcName[i]=criticalMcName[i].trim();
						if ( nonstartedMC[j].equals(criticalMcName[i])) {
							logger.info("Critical MC name " + criticalMcName[i] + "  matches with nonstarted mC ...");
							criticalMcOffline = false;
							logger.info("Setting the attribute to criticalMcoffline to "
									+ criticalMcOffline);
							

						} else {
							logger.info("Critical MC " + criticalMcName[i]  + " name not matched with nonstarted mC");
						}

					}
				}
				}else {
					logger.error(cmd + " not executed successfully");
					return exitValue;
				}

				return exitValue;
			} else {
				logger.error(cmd + " not  executed successfully");
				return exitValue;
			}
		} else {
			logger.error(cmd + " not  executed successfully");
			return exitValue;
		}

	}

	/**
	 * coldrestarts/online the MC if it is non started state and MC is not present in
	 * the list of remain offlined MC list.
	 * @param remainOfflinedMclist
	 ** @return int value
	 */
	public int restartOrOnlineMc(String remainOfflinedMclist) {
		int exitValue;
		String cmd;
		boolean presentinOfflineMclist=false;
		String lowerNonStartedMcName;
		String uppercaseNonStartedMcName;
		cmd = "/opt/ericsson/nms_cif_sm/bin/smtool -list";
		exitValue = runSingleCommandOnHost(PCJCommonOperator.ossGrpAdminServerHost, cmd, true, true);
		if (exitValue == 0) {
			logger.info(cmd + "executed successfully");
			cmd = "/opt/ericsson/nms_cif_sm/bin/smtool -list | grep -v started | wc -l |  awk  '{print $1}'";
			exitValue = runSingleCommandOnHost(PCJCommonOperator.ossGrpAdminServerHost, cmd, true, true);
			if (exitValue == 0) {
				logger.info(cmd + " got executed  successfully");
				int nonStartedMcCount = Integer.parseInt(getLastOutput());
				if (nonStartedMcCount == 0) {
					logger.info("nonstartedMC Count is " + nonStartedMcCount
							+ "  so.. no need to run further steps");
					allMCsStartedState = true;
					return exitValue;

				}
				logger.info("nonstartedMC Count is " + nonStartedMcCount
						+ "so allMCstartedState attribute value is "
						+ allMCsStartedState);
				cmd = "/opt/ericsson/nms_cif_sm/bin/smtool -list | grep -v started |  awk  '{print $1 , $2 }'";
				exitValue = runSingleCommandOnHost(PCJCommonOperator.ossGrpAdminServerHost, cmd, false, true);
				String nonStartedMcOutput[] = getLastOutput().split("\n");
				for (int i = 0; i < nonStartedMcOutput.length; i++)

				{
					 presentinOfflineMclist=false;
					String output[] = nonStartedMcOutput[i].split(" ");
					String nonStartedMcName = output[0];
					String nonStartedMcState = output[1];
					logger.info("mc name is " + nonStartedMcName + "  mc state is " + nonStartedMcState);
					nonStartedMcState=nonStartedMcState.trim();
					 uppercaseNonStartedMcName = nonStartedMcName.toUpperCase(); 
					 lowerNonStartedMcName = nonStartedMcName.toLowerCase();
				    String offlinedMclist[]=remainOfflinedMclist.split("\\s+");
				    for (int j=0;j<offlinedMclist.length;j++)
				    
				    {
				    	
					if (offlinedMclist[j].equals(uppercaseNonStartedMcName)|| offlinedMclist[j].equals(lowerNonStartedMcName) ) {
						presentinOfflineMclist=true;
                        
					}				    }
				    
				        if (presentinOfflineMclist)
				        {	    								
								
				        	logger.info(" nonstarted MCname " + nonStartedMcName +" is present in the list of MC's which will remain in offline state...so not performing any operation on this MC");
	                        }
				        
				        else
				        {


					if (nonStartedMcState.matches(".*(offline).*")){
												 
						cmd = "/opt/ericsson/nms_cif_sm/bin/smtool -online "
								+ nonStartedMcName;

					} else

					{
						cmd = "/opt/ericsson/nms_cif_sm/bin/smtool -coldrestart "
								+ nonStartedMcName
								+ " -reason=planned -reasontext=cdb";

					}

					exitValue = runSingleCommandOnHost(PCJCommonOperator.ossGrpAdminServerHost, cmd, false, true);
					if (exitValue == 0) {
						logger.info(cmd + "executed successfully");
						

					} else {
						logger.error(cmd + "command execution got failed ");
						return exitValue;
					}
				}
				}//for 
				return exitValue;
				
			}else {
				logger.error(cmd + " not executed  successfully");
				return exitValue;
			}
			
		}

			

	else {
			logger.error(cmd + " not executed  successfully");
			return exitValue;
		}

	}

}
