/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.orchestrator.test.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper; 
import com.ericsson.cifwk.taf.annotations.Context; 
import com.ericsson.cifwk.taf.annotations.TestStep; 
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.*;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.orchestrator.test.operators.PCJCommonOperator;
import com.ericsson.oss.orchestrator.test.operators.ConfigureEBAMCsOperator;
import com.google.inject.Inject;



public class ConfigureEBAMCsTestSteps  extends TorTestCaseHelper implements TestCase{

    final static Logger logger = LoggerFactory.getLogger(ConfigureEBAMCsTestSteps.class);
    @Inject 
    private ConfigureEBAMCsOperator EBAMCs;
    public  static String ldapAdminPwd=" ";
                        
    /**
     * Check if EBAS applications are
     * running on the ebas server and if not update hosts file     * 
    */
    @TestStep(id="checkETCHosts")
    public void checkETCHosts()
    
    {
        setTestStep("checkETCHosts");
        if (EBAMCs.checkEbasHostExists()){
            assertEquals("/etc/hosts file not found", EBAMCs.checkHosts(),0);
        }
        
    }    
    
    /**
     * Check if EBAS processes are
     * Online on ebas server    * 
     */
    
   @TestStep(id="checkEBAprocesses")
    public void checkEBAprocesses()
    {   setTestStep("checkEBAprocesses");
        if (EBAMCs.checkEbasHostExists()){
            assertEquals ("EBA Processes NOT running", EBAMCs.checkEBAprocesses(),0);
        }
       
    }
    
                  
} 
                
 
