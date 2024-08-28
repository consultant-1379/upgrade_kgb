package com.ericsson.oss.orchestrator.test.steps;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.oss.orchestrator.test.operators.CacheLatestMediaOperator;
import com.ericsson.oss.orchestrator.test.operators.CleanupOrchaOperator;
import com.ericsson.oss.orchestrator.test.operators.SetupOrchaUserOperator;
import com.google.inject.Inject;

public class CleanupUpgradeSteps extends TorTestCaseHelper {

    final static Logger logger = LoggerFactory.getLogger(CleanupUpgradeSteps.class);
    
    
    @Inject
    private CleanupOrchaOperator cleanupOp;
    // Re-use operators that already have information we want. Although not generic cases they are useful for this test
    @Inject
    private SetupOrchaUserOperator setupOp;
    @Inject
    private CacheLatestMediaOperator cacheOperator;
    
    /**
     * Verifies the depo directory does not exist
     */
    @TestStep(id="verifyDepoRemoved")
    public void verifyDepoRemoved()
    {
        setTestStep("verifyDepoRemoved");
        assertEquals("Upgrade_directory still exists", cleanupOp.verifyPathAvailabilityOnMws((String)DataHandler.getAttribute(cleanupOp.ORCHA_DEPOT)), 2);
    }
    
    /**
     * Sets the cache path attribute as we won't be re-running cache media
     */
    @TestStep(id="setOssrcCache")
    public void setOssrcCache()
    {
        setTestStep("setOssrcCache");
        
        // Make this work for when we are using media already cached or media downloaded
        String ossrcMediaPath = (String) TafConfigurationProvider.provide().getProperty("testware.orch_ossrc_media");
        if (ossrcMediaPath == null) {
            // Not using media that was already cached, so find out cached path
        
            String shipment = cacheOperator.getShipment();
            assertNotNull("Unable to find Shipment details", shipment);
            
            //Get the mode of the run
            String mode = cacheOperator.getMode();
            assertNotNull("Unable to find the mode of the RUN ", mode);
            
            // Get the product set version for given shipment
            String psv = cacheOperator.getPsv(shipment,mode);
            assertEquals("Unable to get Product set version",cacheOperator.getLastExitCode(),0);
           
            // Get the shipment of the media with which they are built
            String builtShipment = cacheOperator.getShipmentOfPsvMedia(psv, shipment, "OSSRC");
            
            // Now work out the path and set on DataHandler
            ossrcMediaPath = cacheOperator.getMediaExpectedCachePath("OSSRC", builtShipment);
        }
        logger.debug("Setting cache path to " + ossrcMediaPath);
        DataHandler.setAttribute(cleanupOp.OSSRC_CACHE, ossrcMediaPath);
        assertNotNull(DataHandler.getAttribute(cleanupOp.OSSRC_CACHE));
    }
    
    
    
    /**
     * Deletes the orcha ldap proxy
     */
    @TestStep(id="removeLdapProxy")
    public void removeLdapProxy()
    {
        setTestStep("removeLdapProxy");
        assertEquals("Failed to remove LDAP Proxy password", cleanupOp.removeLdapProxyAgent(), 0);

    }
    
    
    /**
     * Verifies the orcha ldap proxy is deleted
     */
    @TestStep(id="verifyNoLdapProxy")
    public void verifyNoLdapProxy()
    {
        setTestStep("verifyNoLdapProxy");
        assertEquals("orcha_ldap_proxy still found", cleanupOp.testProxyPwd(), 1);

    }
    
    /**
     * Removes entry on each UAS for orcha@<mws> in the authorized_hosts, hence removing
     * passwordless ssh from orcha on MWS to root on UAS
     */
    @TestStep(id="removeUASPasswordless")
    public void removeUASPasswordless()
    {
        setTestStep("removeUASPasswordless");
                
        List<Host> uases = DataHandler.getAllHostsByType(HostType.UAS);
        removeNodePasswordless(uases);
    }
    
    /**
     * Removes entry on each UAS for orcha@<mws> in the authorized_hosts, hence removing
     * passwordless ssh from orcha on MWS to root on PEER
     */
    @TestStep(id="removePEERPasswordless")
    public void removePeerPasswordless()
    {
        setTestStep("removePeerPasswordless");
                
        List<Host> peers = DataHandler.getAllHostsByType(HostType.PEER);
        removeNodePasswordless(peers);
    }
    
    /**
     * Removes entry on each UAS for orcha@<mws> in the authorized_hosts, hence removing
     * passwordless ssh from orcha on MWS to root on EBAS
     */
    @TestStep(id="removeEBASPasswordless")
    public void removeEBASPasswordless()
    {
        setTestStep("removeEBASPasswordless");
                
        List<Host> ebases = DataHandler.getAllHostsByType(HostType.EBAS);
        removeNodePasswordless(ebases);
    }    
    
    
    /**
     * Verifies password less is not setup from MWS->all servers. Have to call once per server
     */
    @TestStep(id="verifyNoMWSPasswordless")
    public void verifyNoMWSPasswordless()
    {
        setTestStep("verifyNoMWSPasswordless");
        // Gets all hosts except for Gateway
        List<Host> hosts = setupOp.getTargetHosts();
        
        for (Host host : hosts) {
            if (!host.getType().equals(HostType.MS)) {
                List<Host> singleHosts = new ArrayList<Host>();
                singleHosts.add(host);
                assertTrue("Created hosts file for: " + host.getHostname(), setupOp.createOrchaHostsFile(singleHosts));
                logger.info("-------------------------------");
                logger.info("Errors are expected to be displayed to screen as ssh check should fail as passwordless ssh has been removed");
                logger.info("-------------------------------");
                assertEquals("Expected to fail check of passwordless for host " + host.getHostname(), setupOp.runCheckSetupOrchaUser(), 1);
                logger.info("SUCCESS: Errors seen above are expected as passwordless ssh has been removed");
                assertTrue("Removed hosts file for: " + host.getHostname(), setupOp.removeOrchaHostsFile());
            }
        }
        

    }
    
    /**
     * Verifies password less is not setup from mws orcha -> root UAS
     */
    @TestStep(id="verifyNoUASPasswordless")
    public void verifyNoUASPasswordless()    {
        setTestStep("verifyNoUASPasswordless");
        List<Host> uases = DataHandler.getAllHostsByType(HostType.UAS);
        verifyNoNodePasswordless(uases);
    }
    
    /**
     * Verifies password less is not setup from mws orcha -> root EBAS
     */
    @TestStep(id="verifyNoEBASPasswordless")
    public void verifyNoEBASPasswordless()    {
        setTestStep("verifyNoEBASPasswordless");
        List<Host> ebases = DataHandler.getAllHostsByType(HostType.EBAS);
        verifyNoNodePasswordless(ebases);
    }
    
    /**
     * Verifies password less is not setup from mws orcha -> root PEER
     */
    @TestStep(id="verifyNoPEERPasswordless")
    public void verifyNoPEERPasswordless()    {
        setTestStep("verifyNoPEERPasswordless");
        List<Host> peers = DataHandler.getAllHostsByType(HostType.PEER);
        verifyNoNodePasswordless(peers);
    }    
    
    /**
     * Verifies that no orcha user exists on all hosts (except MWS)
     */
    @TestStep(id="verifyNoOrchaUser")
    public void verifyNoOrchaUser()
    {
        setTestStep("verifyNoOrchaUser");
        
        // Gets all hosts except Gateway
        List<Host> hosts = cleanupOp.getTargetHosts();
        
        for (Host host : hosts) {
            if (!host.getType().equals(HostType.MS)) {
                String command = "id orcha";
                assertEquals("Verify that orcha user does not exist on host " + host.getHostname(), cleanupOp.runSingleCommandOnHost(host, command, true, true),1);
                logger.info("SUCCESS: orcha user has been removed");
            }
        }

    }
    
    /**
     * Removes passwordless ssh from orcha@mws -> root on each of nodes listed
     */
    private void removeNodePasswordless(List<Host> nodes)
    {
        
        String mws = DataHandler.getHostByType(HostType.MS).getHostname();
        String command = "sed '/.*orcha@" + mws + "/d' .ssh/authorized_keys > .ssh/authorized_keys.tmp";
        for (Host node : nodes) {
            assertEquals("Failed to create temp authorized_keys on UAS " + node.getHostname(), cleanupOp.runSingleCommandOnHost(node, command, true, true),0);
            command = "mv .ssh/authorized_keys.tmp .ssh/authorized_keys";
            assertEquals("Failed to rename temp authorized keys on UAS " + node.getHostname(), cleanupOp.runSingleCommandOnHost(node, command, true, true),0);
        }
    }    
    
    
    /**
     * Verifies password less is not setup from mws orcha -> root node
     */
    private void verifyNoNodePasswordless(List<Host> nodes)    {
        for (Host node : nodes) {
            List<Host> hosts = new ArrayList<Host>();
            hosts.add(node);
            int exit = setupOp.checkOrchaSSHPasswordless(DataHandler.getHostByType(HostType.MS), hosts, "root");
            assertEquals("Expected ssh error as expect passwordless ssh to have been removed", exit, 255);
            logger.info("SUCCESS: ssh command failed as expected as passwordless ssh has been removed");
        }
    }    
   
}

