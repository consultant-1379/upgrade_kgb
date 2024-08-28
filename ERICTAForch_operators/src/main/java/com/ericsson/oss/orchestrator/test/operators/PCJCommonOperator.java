package com.ericsson.oss.orchestrator.test.operators;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Operator(context = Context.CLI)
@Singleton


public class PCJCommonOperator extends CLIOperator {    
	
	@Inject
    private CLICommandHelper cli;
    
	/** Operator for running commands common to PCJs ***/	   
    private Host ossHost = null; // In case of admin pair, represents adminHost1
    private Host ossHost2 = null; // In case of single admin, will be null
    private Host iloHost = null;
    private Host iloHost2 = null;
    private Host sentinelHost = null;
    private Host peer1 = null;
    private Host peer2 = null;
    private Host peer3 = null;
    private Host peer4 = null;
    private Host peer5 = null;
	private boolean analysedHosts = false;
	private boolean analysedPeer = false;
	private boolean analysedSC1 = false;
	public final static String ORCHA = "orcha"; // Name of orcha user
	private int lastExitCode;

	private String lastOutput;
	private String shipment;

	private final static Logger logger = LoggerFactory
			.getLogger(PCJCommonOperator.class);
	public static final String CACHE_SUFFIX = "CachePath";
	public static final String SOLARIS_CACHE = "solaris" + CACHE_SUFFIX;
	public static final String OSSRC_CACHE = "ossrc" + CACHE_SUFFIX;
	public static final String OM_CACHE = "om" + CACHE_SUFFIX;
	public static final String COMINF_CACHE = "cominf" + CACHE_SUFFIX;
	public static final String OMSAS_CACHE = "omsas" + CACHE_SUFFIX;
	// attribute that holds depot directory
	public static final String ORCHA_DEPOT = "orchaDepo";
    private static final String SSH_OPTS = "-o PubkeyAuthentication=yes -o KbdInteractiveAuthentication=no -o PasswordAuthentication=no " +
            "-o ChallengeResponseAuthentication=no -o LogLevel=quiet -o StrictHostKeyChecking=no " +
            "-o HostbasedAuthentication=no -o PreferredAuthentications=publickey";
            public static Host ossGrpAdminServerHost=null;
	public boolean criticalMcOffline = true;
	public boolean allMCsStartedState = false;
          
    private static final String LS = "/usr/bin/ls";
    private static final String SSH = "/usr/bin/ssh";
	 
	//  Properties
	public static final String LDAP_CLIENT_UPDATE_SCRIPT = "orch.admin_prepare_for_ldap_client_update";

	// List of hosts to put in hosts.xml - assumes that ossmaster will be of
	// type RC
	protected final static List<HostType> hostsInterested = Arrays.asList(
			HostType.EBAS, HostType.NEDSS, HostType.OMSAS, HostType.OMSRVM,
			HostType.OMSRVS, HostType.PEER, HostType.UAS, HostType.RC, HostType.MS);

	/**
	 * Translates HostType as used by TAF to the values used by OSS
	 * 
	 * @param type
	 * @return type of Host as known by orchestrator modules
	 */
	public String getOssType(final HostType type) {
		if (type.equals(HostType.RC)) {
			return "ADMIN";
		} else if (type.equals(HostType.OMSRVM) || type.equals(HostType.OMSRVS)) {
			return "OM_SERVICES";
		} else if (type.equals(HostType.MS)) {
			return "MWS";
		} else {
			return type.toString();
		}
	}

	/**
	 * Analyses hosts through DataHandler so that the getXXXHost methods will
	 * return relevant hosts.
	 */
	private void analyseOssHosts(final TafConfiguration configuration) {
		// Used to analyse ossHosts so know which is admin1 and which is admin2
		for (Host host : DataHandler.getAllHostsByType(HostType.RC)) {
        	
        	if (host.getGroup() != null && host.getGroup().equalsIgnoreCase("admin2")) {
                ossHost2 = host;
            } else {
        		ossHost =host;
        	}
            
        }
        analysedHosts = true;
    }
	
	/**
	 * Alayses hosts of SC1 type through DataHandler
	 * @param configuration
	 */
	private void analyseSC1Hosts(final TafConfiguration configuration) {		
		for (Host host : DataHandler.getAllHostsByType(HostType.SC1)) {		
			if (host.getGroup() != null && host.getGroup().equalsIgnoreCase("ilo1")) {		
				iloHost = host;		
			} else if (host.getGroup() != null && host.getGroup().equalsIgnoreCase("ilo2")) {		
				iloHost2 = host;		
			} else if (host.getGroup() != null && host.getGroup().equalsIgnoreCase("sentinel")) {		
				sentinelHost = host;		
			}else {		
				logger.info("There is no SC1 type hosts given in host.properties");		
			}		
		}		
		analysedSC1 = true;		
	}
    
    /**
     * Analyses peer hosts through DataHandler so that the getXXXHost methods will return
     * relevant hosts.
     */
    private void analysePeerHosts(final TafConfiguration configuration) {
        // Used to analyse peer hosts to know which is peer1,peer2,peer3,peer4,peer5
        for (Host host : DataHandler.getAllHostsByType(HostType.PEER)) {
            	
            if (host.getGroup() != null && host.getGroup().equalsIgnoreCase("peer1")) {
                peer1 = host;
            } else if (host.getGroup() != null && host.getGroup().equalsIgnoreCase("peer2")) {
            	peer2 = host;
            } else if (host.getGroup() != null && host.getGroup().equalsIgnoreCase("peer3")) {
            	peer3 = host;
            } else if (host.getGroup() != null && host.getGroup().equalsIgnoreCase("peer4")) {
            	peer4 = host;
            } else if (host.getGroup() != null && host.getGroup().equalsIgnoreCase("peer5")) {
            	peer5 = host;
            } else {
            	peer1 = host;
            }            
        }
        analysedPeer = true;
    } 
    
    /**
     * Returns hostname of first admin host
     * @return adminHost1 (assumes analyseHosts already run)
     */
    public String getAdminHost1() {
        if (!analysedHosts) {
            analyseOssHosts(TafConfigurationProvider.provide());
        }
        if (ossHost != null) {
            return ossHost.getHostname();
        } else {
            return null;
        }
    }
    
    /**
     * Returns hostname of first admin host
     * @return iloHost1 (assumes analyseHosts already run)
     */
    public Host getILOHost1() {
        if (!analysedSC1) {
            analyseSC1Hosts(TafConfigurationProvider.provide());
        }
        return iloHost;
    }
    
  
    /**
     * Returns Ilo IP1 from given hosts
     * @return
     */
    public String getILOIp1() {
    	if (!analysedSC1) {
            analyseSC1Hosts(TafConfigurationProvider.provide());
        }
        if (iloHost != null) {
            return iloHost.getIp();
        } else {
            return null;
        }
    }
    /**
     * Returns hostname of second ILO host
     * @return iloHost2 (assumes analyseHosts already run)
     */
    public Host getILOHost2() {
        if (!analysedSC1) {
            analyseSC1Hosts(TafConfigurationProvider.provide());
        }
         return iloHost2;
    }
    /**
     * Returns Ilo IP2 from given hosts
     * @return
     */
    
    public String getILOIp2() {
    	if (!analysedSC1) {
            analyseSC1Hosts(TafConfigurationProvider.provide());
        }
        if (iloHost2 != null) {
            return iloHost2.getIp();
        } else {
            return null;
        }
    }
    
    /**
     * Returns hostname of second admin host
     * @return adminHost2 (assumes analyseHosts already run), null if not present
     */
    public String getAdminHost2() {
        if (!analysedHosts) {
            analyseOssHosts(TafConfigurationProvider.provide());
        }
        if (ossHost2 != null) {
            return ossHost2.getHostname();
        } else {
            return null;
        }
    }
    
    /**
     * Returns hostname of second peer server
     * @return peerhost
     */
    public String getPeerServer(String peerserver) {
        if (!analysedPeer) {
        	analysePeerHosts(TafConfigurationProvider.provide());
        }
        
        if (peerserver.equals("peer1"))
        {
        	if (peer1 != null) {
        		return peer1.getHostname();        	
        	}else {
        		return null;
        	}
        }
        
        if (peerserver.equals("peer2"))
        {
        	if (peer2 != null) {
        		return peer2.getHostname();        	
        	}else {
        		return null;
        	}
        }
        
        if (peerserver.equals("peer3"))
        {
        	if (peer3 != null) {
        		return peer3.getHostname();        	
        	}else {
        		return null;
        	}
        }
        
        if (peerserver.equals("peer4"))
        {
        	if (peer4 != null) {
        		return peer4.getHostname();        	
        	}else {
        		return null;
        	}
        }
        
        if (peerserver.equals("peer5"))
        {
        	if (peer5 != null) {
        		return peer5.getHostname();        	
        	}else {
        		return null;
        	}
        }
        
        return null;
    }
    
    /**
     * Returns peerserverip of second peer server
     * @return peerserverip
     */
    public String getPeerServerIP(String peerserver) {
        if (!analysedPeer) {
        	analysePeerHosts(TafConfigurationProvider.provide());
        }
        
        if (peerserver.equals("peer1"))
        {
        	if (peer1 != null) {
        		return peer1.getIp();        	
        	}else {
        		return null;
        	}
        }
        
        if (peerserver.equals("peer2"))
        {
        	if (peer2 != null) {
        		return peer2.getIp();        	
        	}else {
        		return null;
        	}
        }
        
        if (peerserver.equals("peer3"))
        {
        	if (peer3 != null) {
        		return peer3.getIp();        	
        	}else {
        		return null;
        	}
        }
        
        if (peerserver.equals("peer4"))
        {
        	if (peer4 != null) {
        		return peer4.getIp();        	
        	}else {
        		return null;
        	}
        }
        
        if (peerserver.equals("peer5"))
        {
        	if (peer5 != null) {
        		return peer5.getIp();        	
        	}else {
        		return null;
        	}
        }
        
        return null;
    }

    /**
	 * Returns the Host corresponding to the MS
	 * 
	 * @return the MS host
	 */
	public Host getMsHost() {
		return DataHandler.getHostByType(HostType.MS);
	}

	/**
	 * Returns ossHost object
	 * 
	 * @return the OSS host
	 */
	public Host getOssHost() {
		if (!analysedHosts) {
        	analyseOssHosts(TafConfigurationProvider.provide());
		}
		return ossHost;
	}

	/**
	 * Returns 2nd ossHost object or null if not present
	 * 
	 * @return the OSS host
	 */
	public Host getOssHost2() {
		if (!analysedHosts) {
			analyseOssHosts(TafConfigurationProvider.provide());
		}
		return ossHost2;
	}

	/**
	 * Returns whether this is a system with 2 ADMIN nodes
	 * 
	 * @return true if 2 admin nodes, false otherwise
	 */
	public boolean is2NodeHost() {
		if (!analysedHosts) {
			analyseOssHosts(TafConfigurationProvider.provide());
		}
		if (ossHost2 == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Returns ossHost IP
	 * @return the OSS server IP
	 */
	public String getOssHostIP() {
		if (!analysedHosts) {
        	analyseOssHosts(TafConfigurationProvider.provide());
		}
		if (ossHost != null) {
		return ossHost.getIp();
		} else {
            return null;
        }
	}
	
	/**
	 * Returns second ossHost IP
	 * @return the OSS host IP
	 */
	public String getOssHost2IP() {
		if (!analysedHosts) {
        	analyseOssHosts(TafConfigurationProvider.provide());
		}
		if (ossHost2 != null) {
			return ossHost2.getIp();
			} else {
	            return null;
	        }
	}
	
	
	
	
	

	/**
	 * Returns root user for host, or null if does not exist
	 * 
	 * @param host
	 *            Host want orcha user from
	 * @return User object
	 */
	public User getUserByName(final Host host, final String username) {
		logger.debug("Finding " + username + " user for host "
				+ host.getHostname());
		User foundUser = null;
		List<User> users = host.getUsers();
		for (User user : users) {
			if (user.getUsername().equals(username)) {
				foundUser = user;
				break;
			}
		}
		logger.debug("Returning orcha user as: " + foundUser);
		return foundUser;
	}

	/**
	 * Gets lists of target servers (calling analyseHosts if necessary)
	 * 
	 * @return List of target servers, ie. those in the hostsInterested list
	 */
	public List<Host> getTargetHosts() {
		List<Host> targetHosts = new ArrayList<Host>();

		List<Host> hosts = DataHandler.getHosts();
		for (Host host : hosts) {
			// Write an entry for each host interested in except the MWS
			if (hostsInterested.contains(host.getType())) {
				targetHosts.add(host);
			}
		}
		return targetHosts;
	}

	/**
	 * Uses the CLI to connect to MWS as root, execute a command, read all
	 * output and return shell exit code. Output can later be retrieved by the
	 * getLastOutput method
	 * 
	 * @param command
	 * @return exit code
	 */
	public int runSingleCommandOnMwsAsRoot(final String command) {
		Host host = getMsHost();
		return runSingleCommandOnHost(host, command, true, true);
	}

	/**
	 * Uses the CLI to connect to MWS as root, execute a command, read all
	 * output and return shell exit code. Output can later be retrieved by the
	 * getLastOutput method
	 * 
	 * @param command
	 * @return exit code
	 */
	public int runSingleCommandOnMwsAsRoot(final String command,
			boolean removeNewLines) {
		Host host = getMsHost();
		return runSingleCommandOnHost(host, command, removeNewLines, true);
	}

	/**
	 * Uses the CLI to connect, execute a command, read all output and return
	 * shell exit code. Output can later be retrieved by the getLastOutput
	 * method
	 * 
	 * @param host
	 * @param command
	 * @param removeNewLines
	 * @param runAsRoot
	 *            - if true runs as root, else runs as orcha
	 * @return exit code
	 */
	public int runSingleCommandOnHost(final Host host, final String command,
			final boolean removeNewLines, final boolean runAsRoot) {
		User user;
		if (runAsRoot) {
			user = host.getUsers(UserType.ADMIN).get(0);
		} else {
			user = getUserByName(host, ORCHA);
		}

		return runSingleCommandOnHost(host, user, command, removeNewLines);

	}

	/**
	 * Uses the CLI to connect, execute a command, read all output and return
	 * shell exit code. Outpu t can later be retrieved by the getLastOutput
	 * method
	 * 
	 * @param host
	 * @param user
	 * @param command
	 * @param removeNewLines
	 * @return exitCode
	 */
	public int runSingleCommandOnHost(final Host host, final User user,
			final String command, final boolean removeNewLines) {
		logger.debug("Running command: " + command + " on host "
				+ host.getHostname() + " " + "as " + user.getUsername());
		return runSingleCommandOnHost(host, user, command, removeNewLines,
				false);
	}

	/**
	 * Uses the CLI to connect, execute a command, read all output and return
	 * shell exit code. Outpu t can later be retrieved by the getLastOutput
	 * method
	 * 
	 * @param host
	 * @param user
	 * @param command
	 * @param removeNewLines
	 * @param displayAsRun
	 *            Whether to output stdout to screen as command is run
	 * @return exitCode
	 */
	public int runSingleCommandOnHost(final Host host, final User user,
			final String command, final boolean removeNewLines,
			final boolean displayAsRun) {
		logger.debug("Running command: " + command + " on host "
				+ host.getHostname() + " " + "as " + user.getUsername());
		lastExitCode = -1;
		initialize(host, user);
		Shell shell = executeCommand(command);
		String output = readUntilClosed(shell, displayAsRun);
		if (removeNewLines) {
			lastOutput = output.replace("\n", "").replace("\r", "");
		} else {
			lastOutput = output;
		}
		lastExitCode = getShellExitValue(shell);
		logger.debug("Output of " + command + " is: ");
		logger.debug(lastOutput);
		disconnect(shell);
		return lastExitCode;
	}

	/**
	 * Returns the last output from running one of the runSingleCommandOnHost
	 * methods
	 * 
	 * @return Stdout of last command run using runSingleCommandOn..
	 */
	public String getLastOutput() {
		return lastOutput;
	}

	/**
	 * Returns the last exit code from running one of the runSingleCommandOnHost
	 * methods
	 * 
	 * @return Stdout of last command run using runSingleCommandOn..
	 */
	public int getLastExitCode() {
		return lastExitCode;
	}

	/**
	 * Returns a space separated string with hostnames from list of hosts
	 * 
	 * @param hosts
     * @return	String
	 */
	protected String getHostStr(final List<Host> hosts) {
		StringBuilder hostStr = new StringBuilder();
		for (Host host : hosts) {
			hostStr.append(host.getHostname());
			hostStr.append(" ");
		}
		return hostStr.toString();
	}

	/**
	 * This Method runs the command with orcha user on MWS returns the exit
	 * value of the command
	 * 
	 * @param command
	 * @return exit value of command
	 */
	public int runSingleCommandOnMwAsOrcha(String command) {
		Host host = getMsHost();
		return runSingleCommandOnHost(host, command, true, false);
	}

	/**
	 * Returns whether package is already installed on MWS
	 * 
	 * @param pkgname
	 * @return whether pkg is installed on MWS
	 */
	public boolean isPkgInstalled(String pkgname) {
		String cmd = "pkginfo " + pkgname;
		int exitCode = runSingleCommandOnMwsAsRoot(cmd);
		if (exitCode == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This Method retrieves the version of the specified package from the
	 * specified directory (not installed return string
	 * 
	 * @param pkgPath
	 *            Path to package
	 * @return String version of the orc Pkg
	 */
	public String getPkgVersion(final String pkgPath) {
		String cmd = "pkginfo -ld " + pkgPath
				+ " | grep -i version | awk '{print $2}'";
		logger.info("Executing pkginfo   command to get Orc package version from media "
				+ cmd);
		runSingleCommandOnMwsAsRoot(cmd);
		return (getLastOutput());
	}

	/**
	 * This Method executes pkginfo command of given pkg returns the installed
	 * version of pkg
	 * 
	 * @param pkgname
	 * @return String
	 */
	public String getPkgInfoCmdVersion(String pkgname) {
		String cmd = "pkginfo -l " + pkgname
				+ "| grep -i version | awk '{print $2}'";
		logger.info("Executing pkginfo  command" + cmd);
		runSingleCommandOnMwsAsRoot(cmd);
		return (getLastOutput());
	}

	/**
	 * This Method executes user id (id -a) command of given pkg returns the
	 * output of id -a command
	 * 
	 * @param user
	 * @return String
	 */
	public String getUserUidInfo(String user) {
		String cmd = "id -a " + user;
		logger.info("Executing id  command to get UID of Orcha user " + cmd);
		runSingleCommandOnMwsAsRoot(cmd);
		return (getLastOutput());
	}

	/**
	 * This Method executes pwd command with the mentioned user returns the exit
	 * value of the command
	 * 
	 * @param asRoot
	 * @return String output of pwd command
	 */
	public String getPwd(boolean asRoot) {
		String cmd = "pwd";
		if (asRoot) {
			runSingleCommandOnMwsAsRoot(cmd);
		} else {
			runSingleCommandOnMwAsOrcha(cmd);
		}
		return (getLastOutput());
	}

	/**
	 * Verify the given path is available or not
	 * 
	 * @param path
	 * @return the exit value of command
	 */
	public int verifyPathAvailabilityOnMws(final String path) {
		String cmd = "ls " + path;
		return runSingleCommandOnMwsAsRoot(cmd);
	}

	/**
	 * Runs mkdir command on MWS
	 * 
	 * @param directory
	 * @return exit value command
	 */

	public int makeDirOnMws(String directory) {
		logger.info("Creating Directory " + directory + " on mws");
		String command = "mkdir -p " + directory;
		return (runSingleCommandOnMwsAsRoot(command));

	}

	/**
	 * Runs rm -rf command on MWS
	 * 
	 * @param path
	 *            to be removed
	 * @return exit code
	 */

	public int removePathOnMws(String path) {
		logger.debug("Removing Path " + path + " from mws");
		String command = "rm -rf " + path;
		return (runSingleCommandOnMwsAsRoot(command));
	}

	/**
	 * Returns the shipment from the property file
	 * 
	 * @return the shipment
	 */
	public String getShipment() {
		logger.info("Getting the shipment from the job ");
		shipment = (String) DataHandler.getAttribute("shipment");
		return (shipment);
	}

	/**
	 * Pause the thread for number of seconds * 1000
	 * 
	 * @param wait
	 *            - seconds to wait
	 */

	public void putDelay(int wait) {
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {

			e.printStackTrace();

		}

	}

	/**
	 * Verify the given path is available or not
	 * 
	 * @param patterns
	 *            - pattern
	 * @param s
	 *            - complete string where to search for pattern
	 * @return the matched pattern or null if it does not match
	 */

	public String extractStringFromPattern(String patterns, String s) {
		Pattern pattern = Pattern.compile(patterns);
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	/**
	 * Copies Files from source to destination in given host
	 * 
	 * @param host
	 * @param source
	 * @param destination
	 * @return Returns exit value of scp command
	 */
	public int copyFilesAsRoot(Host host, String source, String destination) {
		logger.debug("Copying from " + source + " to " + destination + " of "
				+ host);
		String command = "scp -r " + source + " " + destination;
		return (runSingleCommandOnHost(host, command, true, true));
	}

	/**
	 * Returns Orcha depo path
	 * 
	 * @return the orchaDepo path
	 */
	public String getDepoPath() {
		logger.info("Getting Depo Path created on Mws");
		String orchaDepo = (String) DataHandler.getAttribute("orchaDepo");
		return (orchaDepo);
	}

	/**
	 * Returns sentinel object
	 * 
	 * @return the Sentinel host
	 */
	public Host getSentinelHost() {
		if (!analysedSC1) {	
			analyseSC1Hosts(TafConfigurationProvider.provide());		
		}		
		return sentinelHost;
	}

	/**
	 * This Method executes su - orcha -c pwd command with the mentioned user
	 * returns the output of the command
	 * 
	 * 
	 * @return String output of pwd command
	 * 
	 */
	public String getOrchaHomePath() {
		String cmd = "su - orcha -c  pwd | tail -1 ";

		runSingleCommandOnMwsAsRoot(cmd);
		if (getLastExitCode() != 0) {
			logger.info("command " + cmd + " is not executed properly");
			return null;
		}

		return getLastOutput();
	}

	/**
	 * Returns the name of the filename by removing the directory from it
	 * 
	 * @param filename
     * @return String
	 */
	public String getFilename(String filename) {
		return filename.substring(filename.lastIndexOf("/") + 1);
	}

	/**
	 * Verify the given path is available or not
	 * 
	 * @param path
	 *            String parameter
	 * @return the exit value of command
	 * 
	 */
	public int verifyPathAvailability(String path, Host host) {
		String cmd = "ls " + path;
		return runSingleCommandOnHost(host, cmd, true, true);

	}

	/**
	 * Umounts the given path
	 * 
	 * @param mountPath
	 * @param host
	 * @return int value
	 */
	public int umountMediaPath(String mountPath, Host host) {
		logger.info("Unmounting the path " + mountPath + " on Mws");
		String cmd = "umount " + mountPath;
		runSingleCommandOnHost(host, cmd, true, true);
		int exitCode = getLastExitCode();
		if (exitCode != 0) {
			logger.error(" mount command of media not executed successfully");
		} else {
			logger.info(" mount command of media executed successfully");

		}
		return (exitCode);
	}

	/**
	 * Removes directory path
	 * 
	 * @param dirPath
	 * @param host
	 * @return int value
	 */
	public int rmDirPath(String dirPath, Host host) {

		String cmd = "rm -rf  " + dirPath;
		runSingleCommandOnHost(host, cmd, true, true);
		int exitCode = getLastExitCode();
		if (getLastExitCode() != 0) {
			logger.error(cmd + " command of media not executed successfully");
		} else {
			logger.info(cmd + " command got  executed successfully");

		}
		return (exitCode);
	}

	/**
	 * verifies which admin server is having OSSGroup
	 * 
	 * @return int
	 */
	public int verifyOSSGrpRunningAdminServer() {
		String hostname;
		int  hostsNumber=1;
		if (is2NodeHost())
		{
			hostsNumber=2;
						
		}
			for (int  i = 1; i <= hostsNumber; i++) {

			if (i == 1) {
				ossGrpAdminServerHost = getOssHost();

			} else

			{
				ossGrpAdminServerHost = getOssHost2();
							}
				
			String cmd = "/bin/hostname";
			runSingleCommandOnHost(ossGrpAdminServerHost, cmd, true, true);
			hostname = getLastOutput();
			if (getLastExitCode() == 0) {
			cmd = "/opt/VRTS/bin/hagrp -state Oss -sys " + hostname;
			runSingleCommandOnHost(ossGrpAdminServerHost, cmd, true, true);
			if (getLastExitCode() == 0) {
				logger.info(cmd + " command  executed successfully");
				if (getLastOutput().matches("ONLINE")) {
					logger.info(hostname
							+ "is the admin server host  on which Oss Group is running");
					break;
				} else {
					logger.info(hostname
							+ "is  not the  admin server host  on which Oss Group is running");
					ossGrpAdminServerHost =null;
				}

			} else {
				logger.error(cmd + " command not  executed successfully");

			}
			
			}
			else {
				logger.error(cmd + " command not  executed successfully");

			}

		}
		

		return getLastExitCode();
	}



	/**
	 ** checks if MCs are in the proper state(online/offline) if MCs are in any
	 * other state, waits for maximum of 1 hr 45 mins by checking MC status
	 * every 3 minutes.
	 ** 
	 * @return - true
	 **/
	public Boolean checkMCInProperState(String maxMCWaitTime) {
		int exitVal2;

		exitVal2 = runSingleCommandOnHost(ossGrpAdminServerHost,
				"/opt/ericsson/nms_cif_sm/bin/smtool -p", true, true);

		if (exitVal2 == 0) {

			logger.info("smtool -p command is executed properly");
			logger.info("smtool -p output is " + getLastOutput());

		}

		else {

			logger.error("smtool -p command is not executed properly");
			return false;
		}

		int iterations = Integer.parseInt(maxMCWaitTime) / 3;

		for (int i = 0; iterations >= i; i++) {
			exitVal2 = 100;
			exitVal2 = runSingleCommandOnHost(ossGrpAdminServerHost,
					"/opt/ericsson/nms_cif_sm/bin/smtool -l", true, true);
			logger.info("exit value printing " + exitVal2);
			logger.info("printing command output" + getLastOutput());

			if (0 == exitVal2) {

				if (getLastOutput()
						.matches(
								".*(initializing|retrying|terminating|restart scheduled|no ssr).*")) {

					logger.info(" Few MCs are in intermediate state so waiting for 3 minutes till timeout is reached");
					putDelay(180000);
					

				} else {

					logger.info("All MC's are in Final state");
					return true;
				}
			} else if (1 == exitVal2) {

				logger.error("failed to run smtool -l  command on admin server");
				return false;
			}
		}
		logger.info(" Few MCs are in intermediate state even after time out");
		return true;
	}


         /**
         Creates Directory on the host			
	 * 
	 * @param DirPath
	 * @param host
	 * @return int value
	 */
	public int createDirPathOnHost(String DirPath, Host host) {
		String cmd = "ls -ltr " + DirPath;
		int exitValue = runSingleCommandOnHost(host, cmd, true, true);
		if (exitValue == 0) {
			logger.info(cmd
					+ " executed properly and directory already exists..so removing ");

			for (int i = 1; i <= 2; i++) {
				cmd = "rm  -rf " + DirPath;
				exitValue = runSingleCommandOnHost(host, cmd, true, true);
				if (exitValue == 0) {
					logger.info(cmd
							+ " executed properly and directory is removed..creating directory now ");
					break;

				} else {
					if (i == 1) {

						logger.info(cmd
								+ " Execution failed and removal of directory path is not successful in first attempt");
						logger.info(" checking it is having  mounted content by doing unmount and reattempting removal of directory again");
						cmd = "umount " + DirPath;
						exitValue = runSingleCommandOnHost(host, cmd, true,
								true);
						logger.info("exit value of mount command " + exitValue);
						logger.info(cmd + " cmd got executed");
						logger.info("reattempting deletion of directory");
					}

					else {
						logger.error(cmd
								+ "removal of directory failed even in the second attempt");
					}
	 
				}
			}

			cmd = "mkdir " + DirPath;
			return runSingleCommandOnHost(host, cmd, true, true);
		} else {
		logger.info(cmd
					+ " executed properly and directory does not exist ..so creating ");
			cmd = " mkdir " + DirPath;
			exitValue = runSingleCommandOnHost(host, cmd, true, true);
			return exitValue;
		}
	}


	 

	/**
	 * executed mount commond to mount media from MWS to given host ,in the
	 * given mount path
	 * 
	 * @param mountDirPath
	 * @param cacheMediaPath
	 * @param host
	 * @return int value
	 */
	public int mountMediaOnHost(String mountDirPath, String cacheMediaPath,
			Host host) {

		String cmd = "mount " + getMsHost().getIp() + ":" + cacheMediaPath
				+ mountDirPath;
		return runSingleCommandOnHost(host, cmd, true, true);

		}
	/**This Method runs the command with root user Gateway Host
	 * returns the exit value of the command 
	 * @param command
	 * @return exit value of command
	 */
	public int runSingleCommandOnGatewayAsRoot(String command)
	{
		Host host = getGatewayHost();
		return runSingleCommandOnHost(host, command, true, true);
	}
 
	 /**gets the gateway Host
	  * 
	  * @return gatewy Host
	  */
	 public Host getGatewayHost() {
	     return DataHandler.getHostByType(HostType.GATEWAY);
	 }
	 
	 
	 /**
	  * Sends File from one Host to another Host
	  * @param sourcehost
	  * @param remoteHost
	  * @param fileToCopy
	  * @param remoteFileLocation
	  * @param AsRoot
	  * @return Boolean value of the command
	  */
	 public int sendFileRemotely(final Host sourcehost,final Host remoteHost, final String fileToCopy,final String remoteFileLocation,final Boolean AsRoot)
	 {   
	 	logger.info ("Executing scp command on" + sourcehost + " to transfer "+ fileToCopy + " to " +remoteHost +" to this location" + remoteFileLocation);
	 	User user;
	 	if(AsRoot){
	 	 user=sourcehost.getUsers(UserType.ADMIN).get(0);
	 	}
	 	else{
	 		user=sourcehost.getUsers(UserType.OPER).get(0);
	 	}
	 	
	 	initializeHelper(sourcehost,user);
	    String cmd="scp -o PreferredAuthentications=\"password\" -o \"StrictHostKeyChecking no\" " + fileToCopy + " " + remoteHost.getUser()+"@" + remoteHost.getIp() +":/" +remoteFileLocation;
	    cli.DEFAULT_COMMAND_TIMEOUT_VALUE = 144000;
	    runInteractiveScript(cmd);
	    putDelay(60000);
	 	int exit=1;
	 	if(interactWithShell("password:",remoteHost.getPass())){
	 		
	 		logger.info("interactWithShell:password of sendFileRemotely method is  executed properly");
			putDelay(600000);
	 		exit=getCommandExitValue();
	 		closeShell();
	 		return exit;
	 	}
	 	else
	 	{
	 		logger.info("interactWithShell:password of sendFileRemotely method is  executed properly");
			closeShell();
	 		return exit;
	 	}
	 		
	 	}
 	     	
 
	/**Verify the given path is available or not
     * 
     * @param path String parameter
     * @return the exit value of command
     */
    public int  verifyPathAvailabilityOnGateway(final String path) {         
    	String cmd="ls " + path ;
    	return runSingleCommandOnGatewayAsRoot(cmd);
    }
    
    
    /**
     * Checks passwordless ssh from origHost to remote hosts
     * @param origHost - Host to attempt to connect from
     * @param hosts - Host list to pass to script
     * @param username - Userid to ssh onto hosts as e.g root/orcha
     * @return - exit code from run
     */
    public int checkOrchaSSHPasswordless(final Host origHost, final List<Host> hosts,  final String username) {
        logger.info("Checking orcha user on hosts: [" + getHostStr(hosts) + "] from host " + origHost.getHostname());
        // Orcha user info is same on all nodes, so might just be defined on maven for ms
        User orchaUser = getUserByName(getMsHost(), ORCHA);
        int exit = 0;
        for (Host host : hosts) {
        	exit = runSingleCommandOnHost(origHost, orchaUser, SSH + " " + SSH_OPTS + " " + username + "@" + host.getHostname() + " " + LS, false);
            //exit = runSingleCommandOnHost(origHost, orchaUser, SSH + " " + SSH_OPTS + " " + host.getHostname() + " " + LS, false);
            if (exit != 0) {
                break;
            }
        }
        return exit;
    }
    
    /**
	 ** checks if MC is online
	 ** @return - true if MC is online
	 **/

	public Boolean checkMCOnline(String MC){
		
		int exitVal = runSingleCommandOnHost(getOssHost() ,"/opt/ericsson/nms_cif_sm/bin/smtool -l", false, true);
        if(exitVal!=0){
 			
 			logger.info("failed to run command on admin server");
 			return false;
 		}
		logger.info("exit value printing "+exitVal);
 		String[] eachLine = getLastOutput().split("\\n" );
		int numline = eachLine.length;
		
		for (int i =0 ; i < numline ; i++)
		{
			if(eachLine[i].contains(MC+" "))
			{
				if(eachLine[i].contains("started"))
				{
					logger.info(""+MC+" MC is online");
					return true;
				}
			}
		}
		logger.info(""+MC+" MC is not online");
		return false;
	}
	
	
	/**
	 **checkforFileAvailability
	 ** @return int
	 **/
	 
	
	public int checkforFileAvailability(Host host, String filePath,
			String maxMCWaitTime) {
		String cmd = "ls  -a " + filePath;
		int exitValue = 1;
		int iterations = Integer.parseInt(maxMCWaitTime) ;
		for (int i = 1; i <= iterations; i++)

		{
			exitValue = runSingleCommandOnHost(host, cmd, true, true);
			if (exitValue == 0) {
				logger.info(filePath + "is present on server and this is  " + i
						+ "  attempt");
				putDelay(60000);// putting this delay expecting MWS will reboot
								// in this time so that in next step we can
								// check
								// for ssh connection
				return exitValue;
			} else {
				logger.info(filePath + "is not  present on server in " + i
						+ "attempt");
				logger.info("waiting for 1 minute interval");
				putDelay(60000);
			}

		}

		logger.error("even after time out " + filePath + "  is not present  ");
		return exitValue;
	}
    
	
	/**Checks for SSH connection of host 
	 * with in the given timeout
	 * @param hostname
	 * @param maxMCWaitTime
	 * @return int 
	 */
	public int checkforSshConnection(String hostname, String maxMCWaitTime) {
		logger.info("checking SSH connectivity to the " + hostname
				+ " from gateway");
		int exitValue = 1;
		String cmd;

		int iterations = Integer.parseInt(maxMCWaitTime) ;
		for (int i = 1; i <= iterations; i++) {
			cmd = "ssh  " + hostname + " 'ls /tmp' ";
			exitValue = runSingleCommandOnHost(getGatewayHost(), cmd, true,
					true);
			if (exitValue == 0) {
				logger.info(cmd + "got executed successfully");
				return exitValue;
			} else if (exitValue == 255) {
				logger.info("ssh connection is not yet available for MWS ");
				logger.info("waiting for some more time this is  " + i
						+ "attempt");
				putDelay(60000);
			}

			else {
				logger.error(cmd + "not executed successfully");
				return exitValue;

			}
		}

		logger.info("even after time out  SSH connection to MWS not yet established");
		return exitValue;
	}
    /**
	 * Executes commnd to transfer pkgcontent to tempath
	 * 
	 * @param mountDirPath
	 * @param tempPath
	 * @param host
	 * @return int value
	 */
	public int executePkgTransCmdOnHost(String mountDirPath, String tempPath,
			Host host) {
		String cmd = "pkgtrans  " + mountDirPath + " " + tempPath + "  all";
		return (runSingleCommandOnHost(host, cmd, true,
				true));
		
		
	}       	
	
	/**  Check if EBAS exists in hosts file
	 *   and do not run if not there 
	*/
	    
	    public boolean checkEbasHostExists(){
	        boolean hostExists = true;
	        List<Host> hosts = DataHandler.getAllHostsByType(HostType.EBAS);
	        if (hosts.isEmpty()) {
	                logger.info("No EBAS server present therefore doing nothing"); 
	                hostExists = false;
	        }
	                return hostExists;
	    }    

	/**
	 * Gets the EBAS ip address 
	 * 
	 * @return - Ebas IP address
	 */
	public String getEBASip() {
	String ebasHostIP;
	    Host ebasHost = DataHandler.getHostByType(HostType.EBAS);
	    if (ebasHost != null) {
	    	ebasHostIP = (String) ebasHost.getIp();
        } else {
            return null;
	   
	    }
	    return ebasHostIP;
	}
}	
