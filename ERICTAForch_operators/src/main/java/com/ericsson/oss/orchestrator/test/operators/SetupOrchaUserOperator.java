package com.ericsson.oss.orchestrator.test.operators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.google.inject.Singleton;

/** Operator for running setup_orcha_user script **/
@Operator(context = Context.CLI)
@Singleton

public class SetupOrchaUserOperator extends PCJCommonOperator {
    
    private final static Logger logger = LoggerFactory.getLogger(SetupOrchaUserOperator.class);
    private static final String SETUP_ORCHA_USER = "/ericsson/orchestrator/bin/setup_orcha_user.bsh";
    private static final String SSH = "/usr/bin/ssh";
    private static final String SSH_OPTS = "-o PubkeyAuthentication=yes -o KbdInteractiveAuthentication=no -o PasswordAuthentication=no " + 
                                           "-o ChallengeResponseAuthentication=no -o LogLevel=quiet -o StrictHostKeyChecking=no " + 
                                           "-o HostbasedAuthentication=no -o PreferredAuthentications=publickey";
    private static final String LS = "/usr/bin/ls";
    private static final String REMOVE_ORCHA_PASSHISTORY="/usr/bin/sed '/^orcha:/d' /etc/security/passhistory > /etc/security/passhistory.new";
    private static final String REPLACE_PASSHISTORY="/usr/bin/mv /etc/security/passhistory.new /etc/security/passhistory";
    private static final String RESTORE_PASSHISTORY_FILEPERM="/usr/bin/chmod 400 /etc/security/passhistory";
    private static final String LS_ORCHA_PASSHISTORY=LS + " /etc/security/passhistory";
    private static final String ORCHAHOST_INPUT_FILE = "/tmp/orchahosts.tmp";
    private static final String DELIMITER_PARAM="testware.setup_orcha_delimiter";
    
    private static String orchaHostFile = null; // Instance of orcha host file used in this java run
	private int admin2OrchaIdInUse;

    /**
     * Creates a orcha hosts file on the MWS so that it can be used by the setup_orcha_user script
     * @param hosts - Host list to pass to script
     * @return - exit code from run
     */
    public boolean createOrchaHostsFile(final List<Host> hosts) {
        logger.info("Creating orcha hosts file for hosts: [" + getHostStr(hosts) + "]");
        String localFile = "orchahistory.tmp";
        String delimiter = TafConfigurationProvider.provide().getString(DELIMITER_PARAM, ",");
        File lFile = new File(localFile);
        if (lFile.exists()) {
            logger.warn("Deleting old file " + localFile, true);
            lFile.delete();
        }
        try {
             PrintWriter writer = new PrintWriter(localFile);
             for (Host host : hosts) {
                 String line = getOssType(host.getType()) + delimiter + host.getIp() + delimiter + getUserByName(host, "root").getPassword();
                 writer.println(line);
                 logger.debug("Added line to hostsfile: " + line);
             }
             writer.close();
             logger.info("Created local hosts file");
        } catch (FileNotFoundException e) {
            logger.error("Failed to create hosts file: " + e.getMessage(), true);
            return false;
        }
        Host msHost = getMsHost();
        User root = getUserByName(msHost, "root");
        boolean sent = sendFileRemotely(msHost, root, localFile, getOrchaHostsFile());
        lFile.delete();
        return sent;
    }
    
    /**
     * Removes the orcha hosts file on the MWS that was used by setup_orcha_user script
     * @return - exit code from run
     */
    public boolean removeOrchaHostsFile() {
        logger.info("Deleting orcha hosts file " + getOrchaHostsFile());
        Host msHost = getMsHost();
        User root = getUserByName(msHost, "root");
        boolean deleted = deleteRemoteFile(msHost, root, getOrchaHostsFile());
        return deleted;
    }
    
    /**
     * Runs the setup orcha user script with remove option
     * @return - exit code from run
     */
    public int runRemoveSetupOrchaUser() {
        logger.info("Removing orcha user on all hosts");
        // Output details of hosts file to log
        runSingleCommandOnMwsAsRoot("cat " + getOrchaHostsFile(), false);
        // Now run remove 
        int exit = runSetupOrchaUser(" -r -f " + getOrchaHostsFile());
        return exit;
    }
    
    /**
     * Runs the setup orcha user script to create orcha user
     * @param newpassword - Password to set
     * @return - exit code from run
     */
    public int runCreateSetupOrchaUser(final String newpassword) {
        logger.info("Creating orcha user on all hosts");
        int exit = runSetupOrchaUser("-f " + getOrchaHostsFile() + " -p '" + newpassword + "'");
        return exit;
    }
    
    
    /**
     * Initialise operator ready to run setupOrchaUser, and returns hostStr parameter to use
     * @param args - Arguments to pass to setup_orcha_user script
     * @return
     */
    private int runSetupOrchaUser(final String args) {
        Host ms = getMsHost();
        logger.info("Running setup_orcha_user with args: [" + args + "]");
        // At this stage we do not know the orcha password, as the setupOrchaUser is what will set the orcha password to the TAF value
        // So we therefore need to logon as root and su to orcha
        int exit = runSingleCommandOnMwsAsRoot("su - " + ORCHA + " -c \"" + SETUP_ORCHA_USER + " " + args + "\"", false);
        logger.debug("Output was: " + getLastOutput());
        return exit;
    }
    
    /**
     * Runs the setup orcha user script to check passwordless ssh is setup
     * @return - exit code from run
     */
    public int runCheckSetupOrchaUser() {
        logger.info("Checking orcha user on hosts");
        return runSetupOrchaUser("-c -f " + getOrchaHostsFile());
    }
    

    /**
     * If the password history file is present on set of hosts, then replace it with one that doesn't contain orcha history
     * @param hosts - List of hosts to remove history on
     * @return - 0 if successful, else error of failed command
     */
    public int removeOrchaPasswordHistory(List<Host> hosts) {
        int exit = 0;
        for (Host host : hosts) {
            int hostexit = removeOrchaPasswordHistory(host);
            if (hostexit != 0) {
                exit = hostexit;
            }
        }
        return exit; 
    }
 
    /**
     * Returns the orcha user id info for specified host
     * @param host
     * @return
     */
    public String getOrchaUserIdInfo(Host host) {
        runSingleCommandOnHost(host,  "id orcha", false, true);
        return getLastOutput();
        
    }
               
    /**
     * Removes orcha entry in password hisotry file on single host
     * @param host  - Host to remove history on
     * @return - 0 if successful, else error of failed command
     */
    private int removeOrchaPasswordHistory(Host host) {
        logger.info("Remove orcha password history entry if present on " + host.getHostname());
        int exit = runSingleCommandOnHost(host, LS_ORCHA_PASSHISTORY, false, true);
        if (exit != 0) {
            logger.info("No password history file present, no action required");
            exit = 0;
        } else {
            logger.info("Password history file found, so remove orcha entry");
            exit = runSingleCommandOnHost(host, REMOVE_ORCHA_PASSHISTORY, false, true);
            if (exit == 0) {
                exit = runSingleCommandOnHost(host, REPLACE_PASSHISTORY, false, true);
                if (exit == 0) {
                    exit = runSingleCommandOnHost(host, RESTORE_PASSHISTORY_FILEPERM, false, true);
                } else {
                    logger.debug("Failed to replace password history file");
                }
            } else {
                logger.debug("Failed to create temporary history file with orcha removed");
            }
        }
        return exit;
    }
    
    private static String getOrchaHostsFile() {
        if (orchaHostFile == null) {
            orchaHostFile = ORCHAHOST_INPUT_FILE + "." + System.currentTimeMillis();
        }
        return orchaHostFile;
    }
    
    
    /**
     * Determines if Orcha Id 574 is free on both Admins
     * if occupied by nmsrole we fix on the fly.
     * if it is occupied by anything else the method returns a fail. 
     * @return - 0 if free , else error if not free for Orcha.
     */
    
    public  boolean  verifyOrchaIdFree() {
    	boolean idisfree = true ;
    	String nmsRoleId = "nmsrole"; 
    	String orchaId ="orcha";
    	String roleDelCmd ="roledel nmsrole";
    	
    	// If no Oss hosts Skip check  
    	List<Host> hosts = DataHandler.getAllHostsByType(HostType.RC);
        if (hosts.isEmpty()) {
        	logger.warn("No Admins defined in host.properties so test has nothing to do");	
        	return idisfree;
        }
        else {        
        	// we have at least one admin server .. more work to do 
        	Host admin1 = getOssHost();
        	runSingleCommandOnHost(admin1, "getent passwd 574 | cut -d':' -f3 | sort -n | tail -1", false, true);
        	String admin1OrchaIdInUse=getLastOutput();

        	// Test if Id is free on admin1  
        	if (admin1OrchaIdInUse.length() == 0){
        		logger.info(" admin1 orcha ID is free on : " +admin1 );
        	}
        	else {
        		//If IDI is not free,  we identify what is using it. 
        		logger.info(" admin1 orcha ID alreday in use on : " +admin1 );
        		runSingleCommandOnHost(admin1, "getent passwd 574 | cut -d':' -f1 | sort -n | tail -1", false, true);
         		String admin1ProblemUser=getLastOutput();
         		String admin1Problem = admin1ProblemUser.replaceAll("[\n\r]", "");
         		// Admin1 574  in use and we can fix on the fly we will, else we fail TC  
         		if (admin1Problem.equals(nmsRoleId)){
             		logger.warn("nmsrole is using reserved id on " +admin1 );
             		runSingleCommandOnHost(admin1, roleDelCmd, false, true);                 		
             	} 
         		else if (admin1Problem.equals(orchaId)){
         			logger.info(" Orcha user alreday exists with id 574 " );             			
         		}
         		
         		else {
         			logger.error ("problem user account " +admin1Problem +" is using the reserved orcha Id 574"   );
         			idisfree=false;
         		}  // end problem id check 
         		
        		
        	}
        	
        
        	if (is2NodeHost()) { 
             	Host admin2 = getOssHost2();
             	runSingleCommandOnHost(admin2, "getent passwd 574 | cut -d':' -f3 | sort -n | tail -1", false, true);             	
             	String admin2OrchaIdInUse=getLastOutput();
             	// Test if Id is free on admin2 
             	if (admin2OrchaIdInUse.length() == 0){
             		logger.info("admin2 orcha ID is free on  " +admin2 );
             	}
             	else{
             		// Admin2 if 574 in use and we can fix on the fly we will, else we fail TC
             		runSingleCommandOnHost(admin2, "getent passwd 574 | cut -d':' -f1 | sort -n | tail -1", false, true);
             		String admin2ProblemUser=getLastOutput();
             		String admin2Problem = admin2ProblemUser.replaceAll("[\n\r]", "");
             		logger.info("Problem admin2 orcha ID is taken up by " +admin2Problem );
             		logger.info("compare  " +admin2ProblemUser  +" to nmsrole" );
             		if (admin2Problem.equals(nmsRoleId)){
             			// get nmsrole  detail from   admin1,  we need it for  admin2. 
                    	runSingleCommandOnHost(admin1, "getent passwd nmsrole | cut -d':' -f3 | sort -n | tail -1", false, true);
                    	String admin1NmsRoleId=getLastOutput();
                    	String admin1NmsRole = admin1NmsRoleId.replaceAll("[\n\r]", "");
                 		logger.info("nmsrole needs to change id " );
                 		String roleCmd ="rolemod -u " +admin1NmsRole + " nmsrole";
                 		if (admin1NmsRole.length() == 0){
                    		logger.info(" nmsrole not set on " +admin1 );
                    		roleCmd = roleDelCmd;                    		
                    	}              	 
                   		int roleModStat = runSingleCommandOnHost(admin2, roleCmd, false, true);
                 		if(0 != roleModStat)	 			
    	 					{
                 			logger.error(  roleCmd +" failed on " +admin2);	
                 			idisfree=false;       		
    	 					}
                 	                		
                 	} 
             		else if (admin2Problem.equals(orchaId)){
             			logger.info("Orcha user alreday exists with id 574 " );             			
             		}
             		
             		else {
             			logger.error ("problem user account " +admin2Problem +" is using the reserved orcha Id 574"   );
             			idisfree=false;
             		}
             		
             	} //End admin2 not null
             		
         }	//end if we have a node 2 
        return idisfree;
        } 

    }
    } 


