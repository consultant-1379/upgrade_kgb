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
import com.ericsson.oss.orchestrator.test.operators.CheckStatusOfMcBeforeUpgradeOperator;
import com.ericsson.oss.orchestrator.test.operators.LiveUpgTarFileDownloadOperator;
import com.ericsson.oss.orchestrator.test.operators.OrchPkgOperator;
import com.ericsson.oss.orchestrator.test.operators.WfEngOperator;
import com.google.inject.Inject;


public class CheckStatusOfMcBeforeUpgradeTestSteps extends TorTestCaseHelper {

    final static Logger logger = LoggerFactory.getLogger(CheckStatusOfMcBeforeUpgradeTestSteps.class);
        
        @Inject
        private  CheckStatusOfMcBeforeUpgradeOperator checkMcOperator;
        public  static String ossrcCacheMediaPath="";
        public  static String mountDirPath="/tmp/ericusck_temp_mount";
        public  static String mcListTempPath="/tmp/usck_mc_loc";
        public  static String mcListFilePath= mcListTempPath +"/ERICusck/reloc/sck/lib/mc_start_list";
        public  static String maxMCWaitTime="";
        public  static String maxMCRestartWaitTime="";
        public  static String usckPkgPath="";
        public  static String remainOfflinedMclist="";
        
        
        // This  method identifies the admin server on which OSS grp is running.
        @TestStep(id="Get Admin server with OSS Grp running")
        public void getOSSGrpRunningAdminServer()
        {    
        	setTestStep("Get Admin server with OSS Grp running ");
        	assertEquals("command execution to find oss Grp running Admin server got failed",checkMcOperator.verifyOSSGrpRunningAdminServer(),0);
        	assertNotNull("oss Grp Admin server running server  OSS host is null  ", checkMcOperator.ossGrpAdminServerHost);	
        	
        }
               
    @TestStep(id="Verify MC status ")
    public void verifyMCstatus()
    {    
    	setTestStep("Verify MC status ");
  
    	//TC wait this much time for the MC's which are in intermediate state to come online
    	TafConfiguration configuration = TafConfigurationProvider.provide();
       	maxMCWaitTime=(String) configuration.getProperty("testware.mcWaitTimeMinutes");
       	  
        logger.info("maxMC wait time while checking MC status after spinup vapp :  passed from jenkins  "+maxMCWaitTime);
         assertNotNull("maxMC WaitTime is NULL  ", maxMCWaitTime);
    			
    	Boolean retVal = checkMcOperator.checkMCInProperState(maxMCWaitTime);
    	assertTrue(" Failed to execute smtool -p /smtool -l command please check logs ", retVal);
  
       
   	   	                
    }
    
    @TestStep(id="Restart MC or Online  MC")
    public void restartOrOnlineMc()
    {    
    	setTestStep("Restart MC or Online  MC");
    	TafConfiguration configuration = TafConfigurationProvider.provide();
    	remainOfflinedMclist=(String) configuration.getProperty("testware.remainOfflinedMclist");
    	assertEquals("smtool -l command to check MC status is not executed properly",checkMcOperator.restartOrOnlineMc(remainOfflinedMclist),0);
    	
    	
    	if (checkMcOperator.allMCsStartedState){
    		logger.info ("skipping this step as all MC's are in started state");
    	}
    	
    	else{
    	    	
    	
    	maxMCRestartWaitTime=(String) configuration.getProperty("testware.mcWaitRestartTimeMinutes");
	 	logger.info("maxMC wait time after restart of MC's :  passed from jenkins  "+maxMCRestartWaitTime);
	 	
	 	 assertNotNull("maxMCRestartWaitTime is NULL  ", maxMCRestartWaitTime);
    	Boolean retVal = checkMcOperator.checkMCInProperState(maxMCRestartWaitTime);
    	assertTrue(" Failed to execute smtool -p /smtool -l command check logs ", retVal);
    	}
    	
    	
    	}
    
    @TestStep(id="Prepare critical MC list from Media")
    public void prepareCriticalMclist()
    {    
    	setTestStep("Prepare critical MC list from Media");
    	
    	if (checkMcOperator.allMCsStartedState){
    		logger.info ("skipping this step as all MC's are in started state");
    	  
    		
    	}else
    	{
    		
    		TafConfiguration configuration = TafConfigurationProvider.provide();
    		ossrcCacheMediaPath=(String) configuration.getProperty(checkMcOperator.OSSRC_CACHE);
          	 
        assertNotNull("Media cache path attribute is NULL ", ossrcCacheMediaPath);
        usckPkgPath=mountDirPath+"/inst_config/common/upgrade/ERICusck.pkg";
    	assertEquals("Creation of mount path directory is not successfull",checkMcOperator.createDirPathOnHost(mountDirPath,checkMcOperator.ossGrpAdminServerHost),0);
    	assertEquals("Mounting of OSS media  to mount path directory is not successfull",checkMcOperator.mountMediaOnHost(mountDirPath,ossrcCacheMediaPath+"/ossrc_base_sw/  ",checkMcOperator.ossGrpAdminServerHost),0);
        assertEquals("creation of temp directory to store critical MC is not successfull",checkMcOperator.createDirPathOnHost(mcListTempPath,checkMcOperator.ossGrpAdminServerHost),0);
        assertEquals("Execution of Pkg trans command to transfer SCK content to temp location got failed",checkMcOperator.executePkgTransCmdOnHost(usckPkgPath,mcListTempPath,checkMcOperator.ossGrpAdminServerHost),0);
        assertEquals("Verification of existence of critical MC list file in temp path is failed ",checkMcOperator.verifyPathAvailability(mcListFilePath,checkMcOperator.ossGrpAdminServerHost),0);
    	}    	 	
    	
        }
    
    @TestStep(id="Verify Critical MC and other MC status")
    public void verifyCriticalMc()
    
    {    setTestStep("Verify Critical MC and other MC status");
    	if (checkMcOperator.allMCsStartedState){
    		logger.info ("skipping this step as all MC's are in started state");
    		
    	}else
    	{
    	
    	assertEquals("verification of critical mc and other mc status not executed properly",checkMcOperator.verifyCriticalMcStatus(),0);
    	}
    
    }
    
    @TestStep(id="Cleanup Mount and Direcorty paths")
    public void cleanupmountAndDirPaths()
    {    
    	if (checkMcOperator.allMCsStartedState){
    		logger.info ("skipping this step as all MC's are in started state");
    		
    		
    	}else
    	{
    	setTestStep("Cleanup Mount and Direcorty paths");
    	assertEquals("unmount media location command not executed successfully",checkMcOperator.umountMediaPath(mountDirPath,checkMcOperator.ossGrpAdminServerHost),0);
    	assertEquals("removal of mount path directory  not executed successfully",checkMcOperator.rmDirPath(mountDirPath,checkMcOperator.ossGrpAdminServerHost),0);
    	assertEquals("removal of critical MC temp path directory  not executed successfully",checkMcOperator.rmDirPath(mcListTempPath,checkMcOperator.ossGrpAdminServerHost),0);
    	
    	}
    
    }
    
   
    @TestStep(id="Fail /success the testcase based on Critical Mc and other MC status")
    public void failTcOnCriticalMcStatus()
    {    
    	setTestStep("Fail /success the testcase based on Critical Mcstatus");
    	if (checkMcOperator.allMCsStartedState){
    		logger.info ("skipping this step as all MC's are in started state");
    		    		
    	}else
    	{
    	assertTrue("Atleast one critical Mc is in non started state /other Mc's are in intermediate state still" ,checkMcOperator.criticalMcOffline);
    	}
    }
    		
   
}
