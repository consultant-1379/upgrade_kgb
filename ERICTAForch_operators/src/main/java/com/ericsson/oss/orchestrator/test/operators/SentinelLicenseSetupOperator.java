package com.ericsson.oss.orchestrator.test.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.google.inject.Singleton;

@Operator(context = Context.CLI)
@Singleton
public class SentinelLicenseSetupOperator extends PCJCommonOperator{
	private Logger logger = LoggerFactory.getLogger(SentinelLicenseSetupOperator.class);
	
	/**
	 * Returns Sentinal Ip
	 * @return
	 */
	public String getSentinelServerIp()
	{
		logger.info("Getting Sentinel Server Ip");
		return (getSentinelHost().getIp());
	}
	/**
	 * Pings the server
	 * @param server
	 * @return exit varlu of ping command
	 */
	public int pingServer(String server)
	{
		logger.info("Pinging server " + server);
		return(runSingleCommandOnMwsAsRoot("ping " + server));
	}
	/**
	 * Returns Sentinel licence path
	 * @param server
	 * @return String path
	 */
	public String getSentinelLicencePath(String server)
	{
		logger.info("Getting Sentinel License path");
		String shipment=getShipment();
		String command = "echo " + shipment + " | awk -F. '{print $1}'";
		runSingleCommandOnMwsAsRoot(command);
		String track = getLastOutput();
		String sentinelPath=(String) DataHandler.getAttribute("sentinelPath");
		return("/net/" + server + sentinelPath + "/O" + track + "/sentinel_license_full");
		
	}
	/**
	 * Copies Licence file to orcha depot path
	 * @param licenseSource
	 * @param destination
	 * @return int exit value
	 */
	public int copySentinelLicenseToOrchaDepo (String licenseSource , String destination )
	{
		logger.info("Copying Sentinel License to orcha depo " + destination);
		return(copyFilesAsRoot(getMsHost(),licenseSource,destination));
	}

}
