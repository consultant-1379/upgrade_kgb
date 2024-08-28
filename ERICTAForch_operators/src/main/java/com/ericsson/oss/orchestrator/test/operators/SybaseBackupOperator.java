package com.ericsson.oss.orchestrator.test.operators;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;

public class SybaseBackupOperator extends PCJCommonOperator {

	
    private Logger logger = LoggerFactory.getLogger(SybaseBackupOperator.class);
    
    /**  Check if ADMIN exists in hosts file
     *   and do not run if not there 
     */
    
    public boolean checkHostExists(){
    	boolean hostExists = true;
    	List<Host> hosts = DataHandler.getAllHostsByType(HostType.RC);
    	if (hosts.isEmpty()) {
    		logger.info("No ADMIN server present therefore doing nothing");	
    		hostExists = false;
    	}
    		return hostExists;
    }
	
    /**  Launch dba_tools and carry out syb health check 
     *   return  backup needed y/N to test steps
     */

    public boolean  sybaseDbCheck()
    {
        boolean isVerified = false;
    	
      	User rootUser = getOssHost().getUsers(UserType.ADMIN).get(0);
    	initializeHelper(getOssHost(), rootUser);
    	runInteractiveScript("su - sybase -c /ericsson/syb/util/dba_tools");
    	
        for (int i=1 ; 3 > i  ; i++)
    	{
            if(1==i)
            {
    	     putDelay(1000);
    	     interactvShell("Enter your choice:", "13"); 
            }
    	    if(2==i)
    	    {
    	     logger.info("Sybase Healthcheck Validation completing... Please wait..." );
    	     putDelay(600000); 
    	     isVerified = interactvShell("Backup Validation", "\\r");
    	    }
    	}  		
    	return isVerified; // if true backup not needed
    	
    }

    /**  Check each line of output for pattern match #
    *    and set true/false based on this   
    */
    
    public boolean interactvShell(String qes, String answer)
    {
        boolean backupnotreq = false;           
        try {									
            String otpt3 = expect(qes);
            logger.info("Printing option selected: " +otpt3);
            String[] perlines = otpt3.split("\\n");  
            for (int i=0; i<perlines.length; i++) 
            {  
                if (perlines[i].contains("Backup Validation.....................................................OK!") )
            	{ 
                    logger.info("Back up validation successful"); 
                    logger.info("Sybase Backup exists. Additional Sybase Backup is not required");
                    backupnotreq = true;   
                     
                    break ;  
                }
             
             }
             
             runInteractiveScript(answer);
             System.out.println();
             } catch(Exception e)
             {
                 logger.error( e.getMessage());
                 logger.error("Exception caughtttttttttttttt");
             }
             return backupnotreq;
     }

    	
    /**  Run a Sybase backup of all db's  
    *
    */
    public int  sybaseDbBackup() {
        logger.info("Running a Sybase Backup...please wait");
    	String str = "/ericsson/syb/backup/syb_dbdump -B -D all -e -Z -S 30 -s 2 db masterdataservice";
    	return(runSingleCommandOnHost(getOssHost() ,str, false, true));
    		    		
    	}    
    
    public boolean checkMCStatus()
    {
 	    boolean mcCheckPassed = false;
    	// before triggering sybase, check for tss and  selfmgmt mc to be online
        String maxMCWaitTime = (String) DataHandler.getAttribute("MCWaitTimeMinutes");
		if (null == maxMCWaitTime )
		{
			maxMCWaitTime = "60";
		}
		
		int iterations = Integer.parseInt(maxMCWaitTime)/3;
		logger.info("printing iterations"+iterations);
	 	for (int i=0; iterations >= i ; i++) 
	 	{
	 		if(checkMCOnline("SelfManagement")&& checkMCOnline("TSSAuthorityMC"))
	 		{
	 			logger.info("SelfManagement and TSSAuthorityMC are online");
	 			mcCheckPassed = true;
	 			break;
	 		}
	 		else
	 		{
	 			putDelay(180000); // 3 mins
	 		}	 				
	 	}
    	return mcCheckPassed;
    }
    
    
}
