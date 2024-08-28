package com.ericsson.oss.orchestrator.test.operators;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.google.inject.Inject;


public class ConfigureEBAMCsOperator extends PCJCommonOperator {
    
    private static final String ebasApplications[] = {"ebswservice", "ebssservice", "rpmoservice"};
    private static final String ebasMCs[] = {"EBA_EBSS", "EBA_EBSW", "EBA_RPMO"};
    private static final String ebasProcesses[] = {"eba_rpmo_server", "eba_ebsw_server", "eba_ebss_server"};
    private static final String hostFile = "/etc/hosts";
    private static final String tempHostFile = "/etc/hosts.tmp";
     
    @Inject
    private CLICommandHelper cli;
    final static Logger logger = LoggerFactory.getLogger(ConfigureEBAMCsOperator.class);
    
    List<String> names = new ArrayList<String>();
    public  static String ldapAdminPwd;

 
/**
 * Checks for EBAS processes on the EBAS server
 * 
 * @return - exit code from run
 */    
public int checkEBAprocesses(){
      int lastoutput =0;  
     Host ebasHost = DataHandler.getHostByType(HostType.EBAS);
     logger.info("About to check for EBAS processes.................");   
    
     logger.info("Checking EBA Services on EBAS Server   " +getEBASip());
     runSingleCommandOnHost(ebasHost, "ls -l", false, true);
     for(String process : ebasProcesses) {
         logger.info("Checking EBA Services on EBAS Server   " +process);
         putDelay(5000);
         int lastExit = runSingleCommandOnHost(ebasHost,"/usr/bin/ps -ef | grep " +process +" |grep -v 'grep'", false, true);
         if (lastExit != 0) {
             lastoutput = lastExit;
             logger.debug(process +" process is not found on ebas server " + getEBASip());
             
             return lastoutput;
         }
         else
         { logger.info(process +" process is found on ebas server"+ getEBASip());}
         
         
     }
         return lastoutput;
    }//checkEBAProcesses


/**
 * Removes and updates Ebas Applications if running against Masterservice IP
 * in /etc/hosts. 
 */

public int updateHostsFile(){
    int output =0;
    
   
    for(String application: ebasApplications) {
        logger.info("Deleting Application from Hosts file");
      runSingleCommandOnHost(getOssHost(),"cat "+hostFile +" | sed  ' s/"+application +"//g'  > " +tempHostFile +";cat " +tempHostFile +" | sed 's/"+application +"//g'  > "+hostFile, false, true);
        
    } 
  for(String application: ebasApplications) {
      logger.info("Adding Application to Hosts file");  
    int lastExit = runSingleCommandOnHost(getOssHost(), "cat " +hostFile + "| sed '/^"+getEBASip() +"/ s/$/ " +application +"/' >" +tempHostFile +";cat " +tempHostFile +"> " +hostFile, true, true);
          if (lastExit != 0) { output = lastExit;    }
    }
  
  logger.info("Contents of /etc/hosts file:");
  runSingleCommandOnHost(getOssHost(),"cat /etc/hosts", false,true);
    
    return output;
}

/**
 * Checks /etc/hosts for applications currently running against the EBAS server.
 * if not found: etc/hosts gets updated, maintain_ldap and hatool scripts are run
 * and coldrestart MCs
 * @return - exit code from run
 */
public int checkHosts() {
    
    int output = 0;   
    String cmd = "/bin/cat /etc/hosts | grep " + getEBASip();
    runSingleCommandOnHost(getOssHost() ,cmd, false, true);
    logger.info("etc/hosts output:    "+getLastOutput());
    String str = getLastOutput();
         
    if ((str.contains(ebasApplications[0]) && str.contains(ebasApplications[1]) && str.contains(ebasApplications[2])))
    {
        logger.info("Applications are running against EBAS ip Address");   
        logger.info("Checking that EBA MCs are online");
        for(String mc: ebasMCs) {
           boolean mcOnlineReturn = checkMCOnline(mc);
           if(mcOnlineReturn){
               logger.info(mc +" MC is online");
               mcOnlineReturn = true;
                 }

           else {
               logger.info(mc +" MC is not online");
               logger.info("Online of MC Needed");
               int smtoolReturn = runSingleCommandOnHost(getOssHost(), "/opt/ericsson/nms_cif_sm/bin/smtool -online " + mc + "" , true, true);
               putDelay(2000);
               if (smtoolReturn!=0) {
            	   logger.info("Smtool command did not work correctly");
            	   mcOnlineReturn = false;
            	   }
               else {
            	   logger.info("Checking MCs online one last time");
            	   for(String mc1:ebasMCs) {
            		   boolean finalMConline = checkMCOnline(mc1);
            		   if (!finalMConline){
            			   logger.info(mc1 +" MC is not online");
            			   finalMConline= false;
            		   }
            		   else { logger.info( mc1 +" MC IS online");}
                      		   
            	   }
               }
                                
               }
             
         }
         return output;
         
         }
    
    else {
        
        logger.info("/Etc/hosts file needs updating");
        int returncode = updateHostsFile();
        if (returncode != 0) {
        	 logger.debug("Unable to update etc/hosts");
        	 output = 1;
        	 }
         
        logger.info("Maintain_ldap Script needs running....");
        boolean returnldap = maintainLDAP(ldapAdminPwd);
        if( returnldap != true) {
        	logger.debug("Maintain_ldap script unable to run");
        	output = 1;
        	}
         
        logger.info("Hatool Utility about to run");
        int returncode1 = updateEtcFiles();
        if (returncode1 != 0) {
        	logger.debug("Unable to update hosts file on remote Admin");
        	output = 1;
        	}
         
        logger.info("Cold restart required for MCs to come online...please wait");
        int returncode2 = coldrestart();
        logger.info("Return code is:  " +returncode2);
        if (returncode2 != 0) {
        	logger.debug("Unable to run coldrestart ");
        	output = 1;
        	}
         
        logger.info("Checking to see if EBAS MCs are online");
        for(String mc: ebasMCs) {
        	boolean mcOnlineReturn = checkMCOnline(mc);
        	 
        	if(mcOnlineReturn){
        		 
        		logger.info(mc +" MC is online");
        		output=0;
                 }

        	else {
        		logger.warn(mc +" MC is not online");
        		logger.info("Online of MC Needed");
        		 
        		int smtoolReturn = runSingleCommandOnHost(getOssHost(), "/opt/ericsson/nms_cif_sm/bin/smtool -online " + mc + "" , true, true);
        		putDelay(2000);
        		if (smtoolReturn!=0) {
        			logger.debug("Smtool command did not work correctly");
        			returnldap = false;
        			output = 1;
        			 }  
        	 }
      }                  
     }      
     
      return output;   
   }


/**
 * Runs the hatool script with option 8 Utilities and option 1
 * Copy /etc files -  to remote Admin Server
 * @return - exit code from run
 */
public int updateEtcFiles() {
    logger.info("Copying updated etc files to Remote Admin Server..");
                
    return(runSingleCommandOnHost(getOssHost() ,"/ericsson/core/cluster/bin/hatool 8 1", false, true));
                                
}

/**
 * Runs maintain_ldap script 
 * using ldapadmin as the password  - got from jenkins job
 * @return - boolean exit code from run
 */
public boolean maintainLDAP(String ldapAdminPwd) {
	cli.DEFAULT_COMMAND_TIMEOUT_VALUE = 144000;
    TafConfiguration configuration = TafConfigurationProvider.provide();
    ldapAdminPwd=(String)configuration.getProperty("testware.ldapAdminPwd");
    
    User rootUser = getOssHost().getUsers(UserType.ADMIN).get(0);
    initializeHelper(getOssHost(), rootUser);
    boolean ldapreturn = false;
    String cmd = "/opt/ericsson/sck/bin/maintain_ldap.bsh -y";
    runInteractiveScript(cmd);
    putDelay(10000);
    logger.info("PASWORD IS :   " +ldapAdminPwd);
    
    String output = expect("LDAP maintenance bind password:");
    logger.info("printing otpt3 valvalval"+output);
    String[] perlines = output.split("\\n");  
    for (int i=0; i<perlines.length; i++) 
    {  
        if (perlines[i].contains("LDAP maintenance bind password:") )
        { 
            logger.info("LDAP Update is Required.......................");
            logger.info("LDAP PAsSSWORD IS:                        " +ldapAdminPwd);
            runInteractiveScript(ldapAdminPwd);
            putDelay(5000);
            ldapreturn = true;
             logger.info("LDAP Update has completed.........................................");
            break;  
        }
     
     }
     putDelay(1000);
       
    closeShell();
    return ldapreturn;
    }

/**
 * Runs smtool utility with coldrestart option
 * to start MCs after Migration
 * @return - exit code from run
 */
public int coldrestart() {
    int exit=1;
    Host adminHost = DataHandler.getHostByName(getAdminHost1());
    for(String mc: ebasMCs) {
        
    logger.info("Cold restart needed on EBAS MCs " +mc);
 
    if(runSingleCommandOnHost(adminHost, "/opt/ericsson/nms_cif_sm/bin/smtool -coldrestart " + mc + " -reason=planned -reasontext=\"CDB\"" , true, true) !=0) {
        logger.debug("smtool -coldrestart command command not executed properly");
        return exit;
    }
    else
    {
        logger.info("smtool -coldrestart command executed properly");                
        }
    }
    
    putDelay(5000);
    int smtoolExit = runSingleCommandOnHost(adminHost, "/opt/ericsson/nms_cif_sm/bin/smtool -p", false, true);
    return smtoolExit;  
    
}

} //ConfigureEBAMCOperator    
