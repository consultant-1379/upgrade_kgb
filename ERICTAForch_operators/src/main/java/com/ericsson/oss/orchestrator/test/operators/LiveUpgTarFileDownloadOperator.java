package com.ericsson.oss.orchestrator.test.operators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.oss.orchestrator.test.operators.*;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.google.inject.Singleton;

@Operator(context = Context.CLI)
@Singleton
public class LiveUpgTarFileDownloadOperator extends PCJCommonOperator{    
    /** Operator to download live upgrade tar file to MWS depo path**/
               
    private Logger logger = LoggerFactory.getLogger(LiveUpgTarFileDownloadOperator.class);
            
    /**This Method executes Curl command settings command 
     * @param  curlProxyUser String parameter
     * @param  curlProxyUserPwd String parameter
     * @return int , exit value of the command 
     */
    public int executeCurlSettings(String curlProxyUser,String curlProxyUserPwd)
    {
    	String cmd="curl -k --data " + " \"SMENC=ISO-8859-1&SMLOCALE=US-EN&target=%24SM%24http%3A%2F%2Fgask2web.ericsson.se%2Fservice%2Fget%3FDocNo%3D21%2F15372-CSA113084%26Lang%3DEN%26Rev%3DG40%26Format%3DPDFV1R2&smauthreason=0&postpreservationdata=&smagentname=gask2web-ibmb-prod&USER="+curlProxyUser+"&PASSWORD="+ curlProxyUserPwd + "\" " + "--dump-header headers https://login.ericsson.se/autologinnew/EricssonLogin.fcc";
    	logger.info ("Executing Curl settings command to download the taf file from Gask " + cmd);
    	return runSingleCommandOnGatewayAsRoot(cmd);
     }
    
       
    /**
     * Downloads TarFile to Gateway using curl command 
     * @param tarFileDownloadLocation String parameter
     * @param documentNumber String parameter
     * @param Destination String parameter
     *  @return int, exit value of the interactive shell
     */
    public  int downloadTarToGateway(String tarFileDownloadLocation,String documentNumber,String DestinationFileName)
    {   
    	String cmd= "curl -o  "+ tarFileDownloadLocation +"/"+ DestinationFileName +" -L -b headers http://gask2web.ericsson.se/pub/get?DocNo="+documentNumber;
    	return runSingleCommandOnGatewayAsRoot(cmd); 
    }
      
    /**Verifies Tarfile integrity using gunzip command
     * 
     * @param tarFileDownloadedpath String parameter
     * @return int ,exit value of the cmd
     */
    public  int VerifyTarFileIntergrity(String tarFileDownloadedpath)
    {
    	String cmd="gunzip -t "+ tarFileDownloadedpath ;
    	logger.info ("Executing gunzip command to verify integrity  of downloaded tar file " + cmd);
    	return (runSingleCommandOnGatewayAsRoot(cmd));
        	
    }
    
    /**Copies TAR File to MWS
     *    
     * @param fileToCopy String parameter
     * @param remoteFileLocation String parameter
     * @param tarFilename String parameter and name of the downloaded tar file name
     * @return int  value ,exit value of the command
     */
    public int copyTarFileToMWS(String fileToCopy,String remoteFileLocation)
    {    	
    	 	
    	return sendFileRemotely(getGatewayHost(),getMsHost(),fileToCopy,remoteFileLocation,true);
    }
    
    
    	       
     /**Method returns the CXPnumber of live upgrade tar file from omMedia path
     * 
     * @param omMediaPath String parameter
     * @return String 
     */
     public String getLiveUpgradeCxpNumber(String omMediaPath)
   {
     String cmd= "cat " + omMediaPath +"/om/Liveupgrade/cxp_info |   grep -i CXP  | awk -F'=' '{print $2}' ";
    if(runSingleCommandOnMwsAsRoot(cmd)==0)
      {
    	  logger.info(cmd + " is executed successfully");
		  return(getLastOutput());
    	        }else
    	        {
    	        	logger.info(cmd + "is not executed successfully");
    	        	return "";
    	        }
    
    
    }
     
     /** returns the part of Document name and the same name will be used
      * to store the document
      * 
      * @param documentNumber
      * @return String parameter
      */
     
     public String  fetchTarFileName(String documentNumber){
    	 
    	   	 return documentNumber.substring(documentNumber.lastIndexOf("/")+ 1); 
     }
    
      		 
    		            
    
      
    
      
    
}


