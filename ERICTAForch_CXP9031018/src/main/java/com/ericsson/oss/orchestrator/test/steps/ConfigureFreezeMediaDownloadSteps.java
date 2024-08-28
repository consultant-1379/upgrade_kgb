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
import com.ericsson.oss.orchestrator.test.operators.LiveUpgTarFileDownloadOperator;
import com.ericsson.oss.orchestrator.test.operators.OrchPkgOperator;
import com.ericsson.oss.orchestrator.test.operators.WfEngOperator;
import com.google.inject.Inject;

public class ConfigureFreezeMediaDownloadSteps extends TorTestCaseHelper {

    final static Logger logger = LoggerFactory.getLogger(ConfigureFreezeMediaDownloadSteps.class);
        
        @Inject
        private LiveUpgTarFileDownloadOperator upgTarFileOperator;
        public  static String documentNumber ="";
        public  static String downloadLocation ="/root";
        public  static String depoPath ="";
        public  static String tarFileDownloadedpath;
        public  static String tarFileName="";
        public  static String curlProxyUser="";
        public  static String curlProxyUserPwd="";
		public  static String omMediaCachePath="";
		public  static String downloadFileName="";
        
        
        /* This Step Downloads the Tar file to Gateway */
    @TestStep(id="DownloadToGateway")
    public void downloadToGateway()
    {    
    	setTestStep("DownloadToGateway");
    	  	
    	TafConfiguration configuration = TafConfigurationProvider.provide();
        curlProxyUser=(String) configuration.getProperty("testware.curlProxyUser");
        curlProxyUserPwd=(String) configuration.getProperty("testware.curlProxyUserPwd");
        depoPath= upgTarFileOperator.getDepoPath() + "/sw";
    	documentNumber=(String) configuration.getProperty("testware.configureFreezeTarDocNumber");
    	 
    	 assertNotNull("workspace path is empty",downloadLocation);
    	 assertNotNull("Orcha proxy user is missing ",curlProxyUser);
     	 assertNotNull("Orcha proxy user password is missing",curlProxyUserPwd);
    	 assertNotNull("Unable to get Orcha Depo Path", depoPath);//Verifies depoPath is not NULL
		 assertNotNull("Document number is not fetched correctly ", documentNumber);//Verifies Document Number is not NULL
   	
    	 String downloadFileName =upgTarFileOperator.fetchTarFileName(documentNumber);
    	 assertNotNull("DownloadDocumentName is not fetched properly",downloadFileName);
    	 tarFileName=downloadFileName+ ".tar.gz";
    	 tarFileDownloadedpath= downloadLocation+"/"+tarFileName;
		   	 
    	assertEquals("Curl Settings command is not executed properly",upgTarFileOperator.executeCurlSettings(curlProxyUser,curlProxyUserPwd),0);
    	assertEquals("Downloading of Tar file curl command is not executed properly",upgTarFileOperator.downloadTarToGateway(downloadLocation,documentNumber,tarFileName),0);
    	assertEquals("Tar file is not present in the expected Location on Gateway",upgTarFileOperator.verifyPathAvailabilityOnGateway(tarFileDownloadedpath),0);   			
    	   	
             
    }
    
    
   /* Verifies the downloaded file integrity*/
    @TestStep(id="VerifyDownloadedTarFile")
    public void verifyDownloadedTarFile()
    {     
    	setTestStep("VerifyDownloadedTarFile");
        assertEquals("Tar file is not downloaded properly",upgTarFileOperator.VerifyTarFileIntergrity( tarFileDownloadedpath),0);
        
    }
    
    /*Copies TarFile to MWS*/
    @TestStep(id="CopyTarFileToMWS")
    public void copyTarFileToMWS()
    {     
    	setTestStep("CopyTarFileToMWS");
    	assertEquals("Tar file is not copied properly to MWS ",upgTarFileOperator.copyTarFileToMWS(tarFileDownloadedpath,depoPath),0);
       
    }
    
    /*Verifies Tar file existence on MWS in depo path */ 
    @TestStep(id="VerifyTarFileExistenceOnMWS")
    public void verifyTarFileExistenceOnMWS()
    {     
    	setTestStep("VerifyTarFileExistenceOnMWS");
        assertEquals("Live Upgrade tar file is not copied to  the depo path ",upgTarFileOperator.verifyPathAvailabilityOnMws(depoPath+"/"+tarFileName),0);
        
    }
}

