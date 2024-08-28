package com.ericsson.oss.orchestrator.test.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** Operator for doing commands to install Workflow Engine **/
@Operator(context = Context.CLI)
@Singleton
public class DHCPOperator extends PCJCommonOperator {    

    public final static String DHCP_TEMPLATE = "dhcp_client_template";
    private final static String DHCP_HOME = "/ericsson/jumpstart/bin/manage_dhcp_clients.bsh";
    // Attributes used by test
    private final static String ADMIN_IPV6 = "testware.admin_ipv6";
    private final static String ADMIN_IPV6_ROUTER = "testware.admin_ipv6_router";
    private final static String ADMIN1_IPV6 = "testware.admin1_ipv6";
    private final static String ADMIN2_IPV6 = "testware.admin2_ipv6";
    private final static String ADMIN1_MAC = "testware.admin1_macaddr";
    private final static String ADMIN2_MAC = "testware.admin2_macaddr";
    
    public String ossGrpAdminHost;
    private String ossAdminHost;
    private String ossAdminMac;
    private String adminIpv6 = null;
    private String ipv6router = null;
    private static String isIpv6host;
    private Logger logger = LoggerFactory.getLogger(DHCPOperator.class);
    
    @Inject
    private WfEngOperator wfengoperator;
    
    /**
     * Copies a local file to the workflow engine config directory
     * @param fileToCopy
     * @return true if successfully copied
     */    
    public boolean copyWfEngCfgFileToMWS(String fileToCopy) {
        // Copy file to workflow engine's config directory
        User orchaUser = getMsHost().getUsers(UserType.OPER).get(0);
        String remoteFileLocation = wfengoperator.getWfEngCfg();    
        return sendFileRemotely(getMsHost(), orchaUser, fileToCopy, remoteFileLocation);
    }
 
    /**
     * Adds strings to end of specified file (if they are not already present)
     * @param host   remote host
     * @param user   remote user
     * @param filename  file to append to
     * @param toAdd  array of strings to add
     * @return 0 if successful, else errorcode
     */
    public int addToFile(final Host host, final User user, 
                         final String filename, final String toAdd[]) {
        initializeHelper(host, user);
        int retVal = 0;
        for (String line : toAdd) {
            execute("grep \"" + line + "\" " + filename);
            int grepVal = getCommandExitValue();
            if (grepVal == 0) {
                logger.info("No need to append line as already present in " + filename);
            } else {
                logger.info("Appending \"" + line + "\" to " + filename);
                execute("echo " + line + " >> " + filename);
                retVal = this.getCommandExitValue();
                if (retVal !=0 ) {
                    return retVal;
                }
            }
        }
        return retVal;
    }
    
    /**
	 * Verifies active server hostname
	 * 
	 * @return String
	 */
	public String getOSSRunningAdminServer() {
		
			String cmd = "/bin/hostname";		
			runSingleCommandOnHost(getOssHost(), cmd, true, true);
			String hostname = getLastOutput();

			cmd = "/opt/VRTS/bin/hagrp -state Oss -sys " + hostname;
			runSingleCommandOnHost(getOssHost(), cmd, true, true);
			
			if (getLastExitCode() == 0) {
				logger.info(cmd + " command  executed successfully");
				if (getLastOutput().matches("ONLINE")) {		
					ossGrpAdminHost = getAdminHost1();						
					logger.info(ossGrpAdminHost
							+ " is the admin server host  on which Oss Group is running");
				} else {
					ossGrpAdminHost = getAdminHost2();
					logger.info(ossGrpAdminHost
							+ " is  the  admin server host  on which Oss Group is running");
				}

			} else {
				logger.error(cmd + " command not executed successfully");}
			
			return ossGrpAdminHost;
		
	}
	
	/**
	 * Verifies active server IP address
	 * @return String
	 */
	public String getOSSRunningServerIP() {
		
		if (getOSSRunningAdminServer() == getAdminHost1()){
					ossAdminHost = getOssHostIP();						
				} else {
					ossAdminHost = getOssHost2IP();
				}			
		    logger.info(ossAdminHost + " is the ip of the host on which OSS Group is running");
			return ossAdminHost;	
	}
	
	/**
	 * Verifies active server mac address
	 * @return String
	 */
	public String getActiveNodeMacAddr(final TafConfiguration configuration) {
		
		if (getOSSRunningAdminServer() == getAdminHost1()){
			ossAdminMac = (String) configuration.getProperty(ADMIN1_MAC);
			if (ossAdminMac  == null || ossAdminMac.length() == 0) {
				throw new NullPointerException(
							"Method returned null macaddress of Admin1. Please check jenkins configuration (admin1macaddr)");
				}				
			} else {
				ossAdminMac = (String) configuration.getProperty(ADMIN2_MAC);
				if (ossAdminMac  == null || ossAdminMac.length() == 0) {
					throw new NullPointerException(
							"Method returned null macaddress of Admin2. Please check jenkins configuration (admin1macaddr)");
					}
				}
		logger.info("Active OSS mac address is " +ossAdminMac);
		return ossAdminMac;	
		}
	
	/**
	 * Verifies active server IP address
	 * @return String
	 */
	public String getActiveServerIPv6(final TafConfiguration configuration ) {
		isIpv6host = (String) configuration.getProperty(ADMIN_IPV6); 
		if (getOSSRunningAdminServer() == getAdminHost1() && isIpv6host.equalsIgnoreCase("YES")){
			adminIpv6 = (String) configuration.getProperty(ADMIN1_IPV6);
			if (adminIpv6  == null || adminIpv6.length() == 0) {
				throw new NullPointerException(
						"Returned null admin1 IPv6 address . Please check jenkins configuration");
				}
			} else if (getOSSRunningServerIP() == getOssHost2IP() && isIpv6host.equalsIgnoreCase("YES") ) {
				adminIpv6 = (String) configuration.getProperty(ADMIN2_IPV6);
				if (adminIpv6  == null || adminIpv6.length() == 0) {
					throw new NullPointerException(
						"Returned null admin2 IPv6 address. Please check jenkins configuration");
					}
				}
		logger.info("Active OSS IPv6 address is " +adminIpv6);
		return adminIpv6;		
		}
	
	/**
	 * Verifies active server IP address
	 * @return String
	 */
	public String getActiveNodeRouterIpv6(final TafConfiguration configuration ) {
		isIpv6host = (String) configuration.getProperty(ADMIN_IPV6); 
		if (isIpv6host.equalsIgnoreCase("YES")){
			ipv6router = (String) configuration.getProperty(ADMIN_IPV6_ROUTER);
			if (ipv6router  == null || ipv6router.length() == 0) {
				throw new NullPointerException(
						"Method returned null IPv6 router. Please check jenkins configuration");
				}
		}
		logger.info("IPv6 router value is " +ipv6router);
		return ipv6router;		
		}
	
    /**
     * Customises ini file template given TafConfiguration, assumes analyseAdminHosts already run
     * @param configuration
     * @return
     */
    public int customiseIniFile(final TafConfiguration configuration) {
        String upgradeDir = wfengoperator.getDepotDir(configuration);
        isIpv6host = (String) configuration.getProperty(ADMIN_IPV6);         
        String ossrcMedia = wfengoperator.getMediaLocation(configuration, "ossrc");
        String omMedia = wfengoperator.getMediaLocation(configuration, "om");
        if (null != omMedia && omMedia.length() > 0 )
        {
            int endIndex = omMedia.lastIndexOf("/");
            if (endIndex != -1)  
            {
                omMedia = omMedia.substring(0, endIndex); 
            }
        }
        String solMedia = wfengoperator.getMediaLocation(configuration, "solaris");
        	if (upgradeDir == null || ossrcMedia == null || omMedia == null || solMedia == null) {
            // Error - should have media locations
            return -1;
        }
        String iniFile = wfengoperator.getWfEngCfg()  + "/mws_" + getOSSRunningAdminServer();        
        
        StringBuilder sedCommand = new StringBuilder();
        sedCommand.append("sed -e \"s?BOOT_MEDIA_VAL?");
        sedCommand.append(solMedia);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?OM_MEDIA_VAL?");
        sedCommand.append(omMedia);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?OSSRC_MEDIA_VAL?");
        sedCommand.append(ossrcMedia);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_HOST_VAL?");  
        String admHost = getOSSRunningAdminServer();
        sedCommand.append(admHost);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_HOST_IP?");  
        sedCommand.append(getOSSRunningServerIP());
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_MAC_ADDR_VAL?");  
        String admMac = getActiveNodeMacAddr(configuration);
        sedCommand.append(admMac);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_ISV6_VAL?");
        if (isIpv6host  != null || isIpv6host.length() != 0) {
            sedCommand.append(isIpv6host);
        }
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_HOSTV6_VAL?");
        if (admHost == null || isIpv6host.equalsIgnoreCase("NO")) {
        	admHost = "";
        }
        else 
        admHost = admHost+"-v6";
        sedCommand.append(admHost);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_IPV6_VAL?");
        String hostIpv6 = getActiveServerIPv6(configuration);
        if (isIpv6host.equalsIgnoreCase("NO")) {
        	hostIpv6 = "";
        }
        sedCommand.append(hostIpv6);
        
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_IPV6_ROUTER_VAL?");
        ipv6router = getActiveNodeRouterIpv6(configuration);
        if (isIpv6host.equalsIgnoreCase("NO")) {
        	ipv6router = "";
        }
        sedCommand.append(ipv6router);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_NETMASK_VAL?");  
        Host hostNetmask = DataHandler.getHostByName("netmask");
        if (hostNetmask != null) {
            sedCommand.append(hostNetmask.getIp());
        }
        sedCommand.append("?g\" " + wfengoperator.getWfEngCfg() + "/" + DHCP_TEMPLATE + " > ");
        sedCommand.append(iniFile);

        User orchaUser = getMsHost().getUsers(UserType.OPER).get(0);
        initialize(getMsHost(), orchaUser);
        Shell shell = executeCommand(sedCommand.toString());
        readUntilClosed(shell, true);
        return getShellExitValue(shell);
    }
    
    /**
     * Returns zero if removed host as a client
     * 
     * @return
     */
    public int removeAdminDHCPClient() {
    	logger.info("Attempting to remove server as client on DHCP");
		String command = DHCP_HOME+ " -a remove -c " +getOSSRunningAdminServer()+ " -N";
		return (runSingleCommandOnMwsAsRoot(command));
        }
    
    /**
     * Returns 0 if added host as DHCP client
     *
     * @return
     */

    public int addAdminAsDHCPClient() {
    	logger.info("Attempting to add the server as a dhcp client on mws");
    	String iniFile = wfengoperator.getWfEngCfg()  + "/mws_" + getOSSRunningAdminServer();
		String command = DHCP_HOME+ " -a add -f " +iniFile+ " -N";
		return (runSingleCommandOnMwsAsRoot(command));
        }
}

