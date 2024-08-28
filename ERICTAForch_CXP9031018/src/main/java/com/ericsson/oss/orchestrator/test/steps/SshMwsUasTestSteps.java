package com.ericsson.oss.orchestrator.test.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.HostType;

public class SshMwsUasTestSteps extends SshMwsNodeTestSteps {

    final static Logger logger = LoggerFactory.getLogger(SshMwsUasTestSteps.class);
    
    @TestStep(id="initialize")
    public void initialize() 
    {
        setTestStep("initialize");
        setHostType(HostType.UAS);
    }
    
    @TestStep(id="SetupSshMwsNode")
    public void doSetupSshMwsNode() throws InterruptedException
    {
        // Annotations not being found if in base class
        super.doSetupSshMwsNode();
    }
    
    
    
    /**
     * Verify the Changes made in step SetupSshMwsNode allow passwordless connectivity
     */
    @TestStep(id="VerifyPasswordLess")
    public void verifyMwsNodepasswordless()
    {
         super.verifyMwsNodepasswordless();
    }
   
}
  
    

