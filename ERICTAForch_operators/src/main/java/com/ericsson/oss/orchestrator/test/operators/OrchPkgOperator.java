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
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.google.inject.Singleton;

@Operator(context = Context.CLI)
@Singleton
public class OrchPkgOperator extends PCJCommonOperator{    
    /** Operator to execute OrchPkg installation to MWS server **/
  
              

    private Logger logger = LoggerFactory.getLogger(WfEngOperator.class);
               
   
    /**This Method installs the orc Pkg to the MWS server 
     * returns the exit value of the command
     * 
     * @param mediapath
     * @return the exit value of cmd
     */
    public int  runInstallOrcPkg(final String orchInstallScriptPath) {  
    	        String cmd = orchInstallScriptPath + "update_orchupg.bsh  " + orchInstallScriptPath;
    	       logger.info ("Executing Install orc pkg command" + cmd);
    	       return runSingleCommandOnMwsAsRoot(cmd);}
          
    
    /**This Method retrives the Orch pkg version present in the oss  media path
     *  return string 
     * 
     * @param mediapath
     * @return String version of the orc Pkg
     */
    public String  getOrchPkgVersion(final String orchInstallScriptPath){
    	 String cmd= "pkginfo -ld " + orchInstallScriptPath  + "ERICorchupg.pkg | grep -i version | awk '{print $2}'";
         logger.info ("Executing pkginfo   command to get Orc package version from media " + cmd);
          runSingleCommandOnMwsAsRoot(cmd); 
          return (getLastOutput());
          }
    
    
    
}

