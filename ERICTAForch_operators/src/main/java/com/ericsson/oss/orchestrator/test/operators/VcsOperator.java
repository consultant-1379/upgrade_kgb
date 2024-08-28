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
package com.ericsson.oss.orchestrator.test.operators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.*;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.google.inject.Singleton;

/** Operator for installing veritas license **/
@Singleton
public class VcsOperator extends PCJCommonOperator {
	
	private Logger logger = LoggerFactory.getLogger(VcsOperator.class);
	
	private final static String PERMANENT = ".*License Type.*PERMANENT\\s*";
	

	/**
     * get the VCS license on admin server
     * @return - number of days left for license expiry
     */
	
	public int vxLicense() {
		
		String numdaysleft ="0";		
		//cannot rely on this exit code because if license file is not there it will return 1
		runSingleCommandOnHost(getOssHost(), "/sbin/vxlicrep", false, true);
		String pattern = "[^0-9][0-9]{1,3}";        
   		String[] perLines = getLastOutput().split("\\n" ); 
       	
   		//Analyses each line of vxlicrep output and extract number of days left or expired 	
   		for (int i=0; i<perLines.length; i++) {
   			
   			if (perLines[i].contains("no valid VERITAS License keys") || perLines[i].contains("expired")){
   				
   				break;
   			}
   			   			
        	if (perLines[i].contains("days from now)") ) {
        		
                numdaysleft = extractStringFromPattern(pattern, perLines[i]);
        		
        		if (numdaysleft != "" && numdaysleft.startsWith("(")) {
        			numdaysleft = numdaysleft.substring(1);
        		}
        		break;
        	} else if (perLines[i].matches(PERMANENT)) {
        	    // License is permanent rather than demo - so just set to any value
        	    numdaysleft = "1000";
        	}
        	        	
        } 
	    
   		return Integer.parseInt(numdaysleft);
	}
	
	/**
     * get the new license key from DM server
     * @return - New license key value 
     */
	
    public String getnewlickey() {
    	
    	String newLicKey="";
    	
    	String vcsLicensePath = (String) DataHandler.getAttribute("vcsLicense");
    	String cmd = "cat "+vcsLicensePath;
    	int retval = runSingleCommandOnHost(getMsHost(), cmd, false, true);
    	if(0!=retval)
    	{
    		logger.info("Failed to run the command verify the license path manually");
    		return "";
    	}
        
    	//Analyses the license output and extracts x86 license
        String pattern = "@(.*?)$";
        String[] perLines = getLastOutput().split("\\n");
        // extract new license key from output
        for (int i=0; i<perLines.length; i++) {
        	if (perLines[i].trim().contains("License1_i386") ) {
        		String expectedValue = extractStringFromPattern(pattern, perLines[i]);
        		if (expectedValue!="" && expectedValue.startsWith("@")) {
        			newLicKey = expectedValue.substring(1);
        			break;
        			
        		}
        		
        	}
        	
        }
        return newLicKey;
    }

    /**
     * Remove the expired licenses from admin host
     * @param host1 - admin host on which license has to be removed
     */
    
    public void removeOldLicense(Host host1) {
    	
    	runSingleCommandOnHost(host1, "/bin/rm /etc/vx/licenses/lic/*", false, true);
      	
    }
  
    /**
     * Add new veritas license on admin host
     * @param host1 - admin host on which license has to be installed
     * @param str - new license key
     * @return returns license validity days
     */     
    
  public Boolean addNewLicense(Host host1, String str) {	  
    	
	  Boolean expValue = false;
	  User root = getOssHost().getUsers(UserType.ADMIN).get(0);
	  
	  initializeHelper(host1, root);
      
      //command line interface procedure for vxlicinst command      
      runInteractiveScript("vxlicinst"); 
      
      putDelay(1000);       
      expValue = interactWithShell("Enter your license key :", str);
      
      return expValue;
      
    } 
  
  /**
   * Verify veritas license on admin host
   * @param host1 - admin host on which license has to be verified
   * @return number of  days license valid
   */ 
    
  public int verifyNewLicense(Host host1) {
	  
	  int retval = runSingleCommandOnHost(host1, "/sbin/vxlicrep", false, true);
	  
	  if( 1 == retval )
	  {
		  logger.info("Failed to run vxlicrep command on admin server");
		  return -1;
	  }
		   
	  //Extract license remaining days with the below regex
	  String pattern = "[^0-9][0-9]{1,3}";        
 	  String[] perLines = getLastOutput().split("\\n" ); 
 	  String numdaysleft = "0";
     		
 	  for (int i=0; i<perLines.length; i++) 
 	  {
 		  if (perLines[i].contains("days from now)") ) {
      	  
      	      numdaysleft = extractStringFromPattern(pattern, perLines[i]);
      		
      	      if (numdaysleft != "" && numdaysleft.startsWith("(")) 
      	      { 
      		      numdaysleft = numdaysleft.substring(1);
      		  
      	      }
      	      break;
      	  
 		  } else if (perLines[i].matches(PERMANENT)){
 		      // permanent license so just set to some value > 0
 		     numdaysleft = "1000";
 		      break;
 		  }
      	        	
      } 
	  
      return Integer.parseInt(numdaysleft);
  }   
  
  /**
   * Verify HA Daemon is running on admin host
   * @return weather HA is running
   */ 
  
  public int checkIfHADIsRunning(){
	  logger.info("Checking if HA Deamon already running using command <-/usr/bin/pgrep -x had-> on " + getOssHost());
	  int hadIsRunning = runSingleCommandOnHost(getOssHost() ,"/usr/bin/pgrep -x had", false, true);	
  	  return hadIsRunning;
      } 
  
  /**
   * Verify Oss in online after hastart
   * @return true if yes
   */ 

  public boolean checkIfOssOnlined(Host host){
	  
	  logger.info("Waiting for Oss to online using command <-/opt/VRTS/bin/hagrp -wait Oss State ONLINE  -sys " +host.getHostname()+ " -time 1800-> on " + host.getHostname());
	  int startedOk = runSingleCommandOnHost(host,"/opt/VRTS/bin/hagrp -wait Oss State ONLINE  -sys " +host.getHostname()+ "  -time 1800", false, true);	
	  	if ( startedOk == 0 ) 
  	  		{
  	  		return true;
  	  		}
  	  	return false;
  }
}
