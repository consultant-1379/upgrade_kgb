package com.ericsson.oss.orchestrator.test.steps;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.oss.orchestrator.test.operators.SetupOrchaUserOperator;
import com.google.inject.Inject;

public class SetupOrchaUserSteps extends TorTestCaseHelper {

    final static Logger logger = LoggerFactory.getLogger(SetupOrchaUserSteps.class);
    
    @Inject
    private SetupOrchaUserOperator upgOperator;
    
    
    /**
     * Creates the orcha hosts file for setup_orcha_user script
     */
    @TestStep(id="createOrchaHostsFile")
    public void createOrchaHostsFile()
    {
        setTestStep("createOrchaHostsFile");
        // Gets all hosts except for Gateway
        List<Host> hosts = upgOperator.getTargetHosts();
        
        boolean success = upgOperator.createOrchaHostsFile(hosts);
        assertTrue(success);
    }
    
    /**
     * Deletes the orcha hosts file for setup_orcha_user script
     */
    @TestStep(id="removeOrchaHostsFile")
    public void removeOrchaHostsFile()
    {
        setTestStep("removeOrchaHostsFile");
        
        boolean success = upgOperator.removeOrchaHostsFile();
        assertTrue(success);
    }
    
    /**
     * Removes the orcha user on the target servers and MWS
     */
    @TestStep(id="removeOrchaUser")
    public void removeOrchaUser()
    {
        setTestStep("removeOrchaUser");
        int exit = upgOperator.runRemoveSetupOrchaUser();
        assertEquals(exit, 0);
    }
    
    /**
     * Verify  the orcha user id 574 is free on the target admins
     */
    @TestStep(id="verifyOrchaIdFree")
    public void verifyOrchaIdFree()
    {
        setTestStep("verifyOrchaIdFree");
        boolean exit = upgOperator.verifyOrchaIdFree();
        assertEquals(exit, true);
    }
    
    /**
     * Removes the orcha user password history on the nodes if present. This is needed for physical servers whereby password security is setup,
     * which prevents us setting the orcha password to one already used.
     */
    @TestStep(id="removeOrchaPasswordHistory")
    public void removeOrchaPasswordHistory()
    {
        setTestStep("removeOrchaPasswordHistory");
        // Gets all hosts except for Gateway
        List<Host> hosts = upgOperator.getTargetHosts();
       
        int exit = upgOperator.removeOrchaPasswordHistory(hosts);
        assertEquals(exit, 0);
    }
    
    /**
     * Creates orcha user on remote nodes, and sets up passwordless ssh from MWS->Orcha user
     */
    @TestStep(id="createOrchaUser")
    public void createOrchaUser()
    {
        setTestStep("createOrchaUser");
        String newpassword = upgOperator.getUserByName(upgOperator.getMsHost(), upgOperator.ORCHA).getPassword();
        int exit = upgOperator.runCreateSetupOrchaUser(newpassword);
        // Verify that script added user
        assertEquals(exit, 0);
        
    }
    
    /**
     * Verifies password less is setup from MWS->all servers
     */
    @TestStep(id="verifyMWSPasswordless")
    public void verifyMWSPasswordless()
    {
        setTestStep("verifyMWSPasswordless");
        int exit = upgOperator.runCheckSetupOrchaUser();   
        assertEquals(exit, 0);
    }

    /**
     * Verifies password less is setup from ADMIN server to peers
     */
    @TestStep(id="verifyAdminPasswordless")
    public void verifyAdminPasswordless()
    {
        setTestStep("verifyAdminPasswordless");
        // Gets peers
        List<Host> hosts = DataHandler.getAllHostsByType(HostType.PEER);
        // No setup_orcha_user on admin server, so need to do just with ssh
        int exit = upgOperator.checkOrchaSSHPasswordless(upgOperator.getOssHost(), hosts, "orcha");
        assertEquals(exit, 0);
        if (upgOperator.getAdminHost2() != null) {
            exit = upgOperator.checkOrchaSSHPasswordless(DataHandler.getHostByName(upgOperator.getAdminHost2()), hosts, "orcha" );
            assertEquals(exit, 0);
        }
    }
    
    /**
     * Verifies orcha uid is same on both admin servers
     */
    @TestStep(id="verifyAdminOrchaIdMatches")
    public void verifyAdminOrchaIdMatches() 
    {
        setTestStep("verifyAdminOrchaIdMatches");
        
        String orcha1 = upgOperator.getOrchaUserIdInfo(upgOperator.getOssHost());
        logger.debug("Orcha UID on " + upgOperator.getOssHost().getHostname() + " is " + orcha1);
        // If more than one admin server then verify uid for orcha on both admin servers is the same
        if (upgOperator.is2NodeHost()) {   
            String orcha2 = upgOperator.getOrchaUserIdInfo(upgOperator.getOssHost2());
            logger.debug("Orcha UID on " + upgOperator.getOssHost2().getHostname() + " is " + orcha2);
            assertEquals("Orcha ids do not match: " + orcha1 + "," + orcha2, orcha1, orcha2);
        } else {
            logger.debug("No need to check orcha uid match as only one admin server");
        }
    }
   

   
}

