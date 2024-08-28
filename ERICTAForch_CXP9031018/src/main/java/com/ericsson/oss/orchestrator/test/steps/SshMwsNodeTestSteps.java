package com.ericsson.oss.orchestrator.test.steps;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.oss.orchestrator.test.operators.SshMwsNodeOperator;
import com.google.inject.Inject;

public abstract class SshMwsNodeTestSteps extends TorTestCaseHelper {

    final static Logger logger = LoggerFactory.getLogger(SshMwsNodeTestSteps.class);
    boolean exceptionRaised=false;
    private HostType type;
   
    @Inject
    private  SshMwsNodeOperator upgOperator;

    public void setHostType(HostType type) {
        this.type = type;
    }
        
    
    @TestStep(id="SetupSshMwsNode")
    public void doSetupSshMwsNode() throws InterruptedException
    {
        setTestStep("SetupSshMwsNode");
        // get MWS defined in host.properties
        logger.info("Test with MWS: " +upgOperator.getMsHost());      
        List<Host> hosts = DataHandler.getAllHostsByType(type);                     
        // Print to Screen all hosts of type defined in host.properties
        for (Host host : hosts) {
        	logger.info("Test with: "  +host);	     
        }

		// Exchange the Key 	
		boolean exit1 = upgOperator.setupPasswordlessSSHMwsNode(type);
		assertTrue(exit1);
				              
    }
    
    
    
    /**
     * Verify the Changes made in step SetupSshMwsNode allow passwordless connectivity
     */
   
    
    @TestStep(id="VerifyPasswordLess")
    public void verifyMwsNodepasswordless()
    {
    	 List<Host> nodes = DataHandler.getAllHostsByType(type);
         for (Host node : nodes) {
             List<Host> nodehosts = new ArrayList<Host>();
             nodehosts.add(node);             
             int exit = upgOperator.checkOrchaSSHPasswordless(upgOperator.getMsHost(), nodehosts, "root");
             assertEquals("Verify Passwordless ssh connection Exists ", exit,0);


         }

       
    }
   
}
  
    

