package com.ericsson.oss.orchestrator.test.steps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.oss.orchestrator.test.operators.OrchPkgOperator;
import com.google.inject.Inject;


public class OrcPkgInstallTestSteps extends TorTestCaseHelper {

    final static Logger logger = LoggerFactory.getLogger(OrcPkgInstallTestSteps.class);
      
  
            @Inject
        private OrchPkgOperator pkgOperator;
        private final static String OrchExpectedHomePath="/orcha";
        private final static String uidCmdExpectedOutput ="uid=574(orcha) gid=617(orcha) groups=617(orcha)";
        private final static String OrchPathExpected="/ericsson/orchestrator";
        private String ossrcCachePath=" ";
        private String ossCachePathContent="/ossrc_base_sw/inst_config/i386/upgrade/";
        private String orchInstallScriptPath;
        
        /* This Step Verfies the availability of OSS media path */
    @TestStep(id="CheckMediaPathExistence")
    public void checkMediaPathExistence()
    {     
    	setTestStep("CheckMediaPathExistence");
    	//get OSS media path from jenkins variable
    	TestContext context = TafTestContext.getContext();
        ossrcCachePath = (String)DataHandler.getAttribute(pkgOperator.OSSRC_CACHE);
    	
      
         //Verify Mediapath availability
        orchInstallScriptPath=ossrcCachePath + ossCachePathContent;
        assertEquals("OSS media path does not exist", pkgOperator.verifyPathAvailabilityOnMws(orchInstallScriptPath),0);}
                	
    /* This Step Verfies Orc Pkg installation to MWS */
        @TestStep(id="PkgorchInstall")
    public void pkgOrchInstall()
    {
    	setTestStep("PkgorchInstall");	
        //install  orchPkg to MWS Server
    	assertEquals("Orch Pkg install is not successfull", pkgOperator.runInstallOrcPkg(orchInstallScriptPath),0); }
                        
    
        /* This Step Verfies Orcha user availablity,its home path and orcha user uid info on MWS */
    @TestStep(id="CheckOrchaUserExistence")
    public void checkOrchaUserExistence()
    {
    	setTestStep("CheckOrchaUserExistence");	
    	   	    	
    	//This step verifies the orcha Homepath after login using orcha user
    	 assertEquals("Orch user homepath does not exist ",pkgOperator.getOrchaHomePath(),OrchExpectedHomePath);
          
        //This step verifies the UID output of Orcha user is as expected or not
          
         assertEquals("Orch User UID verification failed",pkgOperator.getUserUidInfo("orcha"),uidCmdExpectedOutput );}
       
    
    /* This Step Verfies installation of orc Pkg by comparing the Pkginfo version of installed orch pkg and pkg version 
     * of orch pkg  present  in media */
    @TestStep(id="OrchPkginfo")
    public void orchPkginfo()
    {
    	setTestStep("OrchPkginfo");	
    	   	
    	//Get MediaPathOrchPkg version     	   
        //Get Pkginfo status of orchPkg on MWS using pkginfo command                       
          assertEquals("Orch Pkginfo command verification failed" ,pkgOperator.getPkgInfoCmdVersion("ERICorchupg"),pkgOperator.getOrchPkgVersion(orchInstallScriptPath)); }
       
    	
      
    /* This Step Verfies Orch path existence on  MWS */
    @TestStep(id="CheckOrchpathExistence")
    public void checkOrchpathExistence()
    {
    	setTestStep("CheckOrchpathExistence");	
       	//Verify for Orch Path existence
    	assertEquals("Orch path does not exist",pkgOperator.verifyPathAvailabilityOnMws(OrchPathExpected),0); }
       
    
    
}