package com.ericsson.oss.orchestrator.test.cases;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.oss.orchestrator.test.operators.WfEngOperator;
import com.google.inject.Inject;

public class OrcEngineAmendSudoersTest extends TorTestCaseHelper implements TestCase {

    final static Logger LOG = LoggerFactory.getLogger(OrcEngineAmendSudoersTest.class);

    @Inject
    OperatorRegistry<WfEngOperator> operatorRegistry;

    
    /**
     * Utility method to amend sudoers file on hosts 
     * @param type
     * @param linesToAdd
     */
    private void runAddTest(HostType type, String linesToAdd[]) {
        WfEngOperator engOperator = operatorRegistry.provide(WfEngOperator.class);
        TafConfiguration configuration = TafConfigurationProvider.provide();
        
        List<Host> hosts = DataHandler.getAllHostsByType(type);
        
        for (Host host : hosts) {
            int succeeded = engOperator.addToFile(host, 
                    host.getUsers(UserType.ADMIN).get(0),
                    "/usr/local/etc/sudoers", 
                    linesToAdd);
            assertEquals(0, succeeded);
        }
    }
    
    /**
     * @throws TimeoutException
     * @DESCRIPTION Amends sudoers for admin
     * @PRE Connection to SUT
     * @VUsers 1
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-OrchAdminSudoers-1", title = "Amend sudoers for admin")
    @Context(context = {Context.CLI})
    @Test(groups={"Acceptance"})
    public void verifyAmendAdminSudoers()
                    throws InterruptedException, TimeoutException {
         
        String linesToAdd[] = { "orcha ALL= NOPASSWD:/ericsson/syb/backup/syb_dbdump",
                                "orcha ALL= NOPASSWD:/bin/ssh"};
        runAddTest(HostType.RC, linesToAdd);
               
    }    
    
    /**
     * @throws TimeoutException
     * @DESCRIPTION Amends sudoers for omsas
     * @PRE Connection to SUT
     * @VUsers 1
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-OrchOmsasSudoers-1", title = "Amend sudoers for omsas")
    @Context(context = {Context.CLI})
    @Test(groups={"Acceptance"})
    public void verifyAmendOmsasSudoers()
                    throws InterruptedException, TimeoutException {
         
        String linesToAdd[] = { "orcha ALL= NOPASSWD:/ericsson/sdee/bin/*",
                                "orcha ALL= NOPASSWD:/usr/sbin/svcadm"};
        runAddTest(HostType.OMSAS, linesToAdd);        

       
    }    
    
    /**
     * @throws TimeoutException
     * @DESCRIPTION Amends sudoers for om_services master
     * @PRE Connection to SUT
     * @VUsers 1
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-OrchOmsrvmSudoers-1", title = "Amend sudoers for omservices master")
    @Context(context = {Context.CLI})
    @Test(groups={"Acceptance"})
    public void verifyAmendOmsrvmSudoers()
                    throws InterruptedException, TimeoutException {
         
        String linesToAdd[] = { "orcha ALL= NOPASSWD:/ericsson/sdee/bin/*"};
        runAddTest(HostType.OMSRVM, linesToAdd);    
    }
    
    /**
     * @throws TimeoutException
     * @DESCRIPTION Amends sudoers for om_services slave
     * @PRE Connection to SUT
     * @VUsers 1
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-OrchOmsrvsSudoers-1", title = "Amend sudoers for omservices slave")
    @Context(context = {Context.CLI})
    @Test(groups={"Acceptance"})
    public void verifyAmendOmsrvsSudoers()
                    throws InterruptedException, TimeoutException {
         
        String linesToAdd[] = { "orcha ALL= NOPASSWD:/ericsson/sdee/bin/*"};
        runAddTest(HostType.OMSRVS, linesToAdd);
    }       
}

