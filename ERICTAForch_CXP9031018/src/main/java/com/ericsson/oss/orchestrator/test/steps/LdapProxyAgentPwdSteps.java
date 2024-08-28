package com.ericsson.oss.orchestrator.test.steps;
 
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
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.oss.orchestrator.test.operators.LdapProxyAgentPwdOperator;
import com.ericsson.oss.orchestrator.test.operators.LiveUpgTarFileDownloadOperator;
import com.ericsson.oss.orchestrator.test.operators.OrchPkgOperator;
import com.ericsson.oss.orchestrator.test.operators.PCJCommonOperator;
import com.ericsson.oss.orchestrator.test.operators.WfEngOperator;
import com.google.inject.Inject;
 
 
public class  LdapProxyAgentPwdSteps extends TorTestCaseHelper {
final static Logger logger = LoggerFactory.getLogger(LdapProxyAgentPwdSteps.class);
       
        @Inject
       private LdapProxyAgentPwdOperator ldapProxyAgentPwd;
        public  static String ldapPAgentPwd ="";
        public  static String ldapAdminPwd=""; 
        public static String  ldapMainPwd="";
        public static String bSIMPwd="";
        public static String cOMPwd="";
        public static String ipsecPwd="";
        public static String biSMRSUpg="";
        public static String pwdSMRS_Master="";
        public static String pwdSMRS_ftp="";
        public static String oMSASIP="";
        public static String rootPwd="";
       
        /* This Step Executes the script to set ldapProxyAgent Pwd  */
    @TestStep(id="SetLdapProxyAgentPwd")
    public void setLdapProxyAgentPwd()
    {    setTestStep("SetLdapProxyAgentPwd");
                TafConfiguration configuration = TafConfigurationProvider.provide();
                 ldapPAgentPwd=(String) configuration.getProperty("testware.ldapProxyAgentPwd");
                 ldapAdminPwd=(String) configuration.getProperty("testware.ldapAdminPwd");
                 ldapMainPwd=(String) configuration.getProperty("testware.ldapMainPwd");
                 bSIMPwd=(String) configuration.getProperty("testware.bSIMPwd");
                 cOMPwd=(String) configuration.getProperty("testware.cOMPwd");
                 ipsecPwd=(String) configuration.getProperty("testware.ipsecPwd");
                 biSMRSUpg=(String) configuration.getProperty("testware.biSMRSUpg");
                 pwdSMRS_Master=(String) configuration.getProperty("testware.pwdSMRS_Master");
                 pwdSMRS_ftp=(String) configuration.getProperty("testware.pwdSMRS_ftp");
                 Host oMSASHost=DataHandler.getHostByType(HostType.OMSAS);
                 logger.info("Type of host is"+oMSASHost);
                 if(oMSASHost != null)
                 {	 oMSASIP = oMSASHost.getIp();
                 } else {
                	 oMSASIP = null; 
                 }
                 Host Admin=ldapProxyAgentPwd.getOssHost();
                 logger.info("Admin is"+Admin);
                 if(Admin != null){
                  rootPwd = ldapProxyAgentPwd.getUserByName(Admin, "root").getPassword();
                 } else {
                  rootPwd = "shroot"; 
                 }
                 
        assertNotNull("Ldap proxy Agent Pwd is NULL",ldapPAgentPwd);
        assertNotNull("Ldap Admin Pwd is NULL",ldapAdminPwd);
        assertNotNull("Ldap Maintenance Account Pwd is NULL",ldapMainPwd);
        assertNotNull("BSIM User Pwd is NULL",bSIMPwd);
        assertNotNull("COM User Pwd is NULL",cOMPwd);
        assertNotNull("Ipsecsmrs UNIX User Pwd is NULL",ipsecPwd);
        assertEquals("BI-SMRS Upgrade requires only y/n value",ldapProxyAgentPwd.Checkforvalue(biSMRSUpg),0);
        assertNotNull("Root user password on SMRS Master server Pwd is NULL",pwdSMRS_Master);
        assertNotNull("SMRS ftpservice Pwd is NULL",pwdSMRS_ftp);
        
        assertEquals("command execution to find oss Grp running Admin server got failed", ldapProxyAgentPwd.verifyOSSGrpRunningAdminServer(),0);
        assertNotNull("oss Grp Admin server running server  OSS host is null  ", ldapProxyAgentPwd.ossGrpAdminServerHost);
        assertEquals("Script is not executed properly",ldapProxyAgentPwd.executeProxyAgentCommand(ldapPAgentPwd,ldapAdminPwd,ldapMainPwd,bSIMPwd,cOMPwd,ipsecPwd,rootPwd,oMSASIP,biSMRSUpg,pwdSMRS_Master,pwdSMRS_ftp),0);
                         
    }
    /* This Step Fetches the password and validates it  */
    @TestStep(id="VerifyProxyAgentPwd")
    public void verifyProxyAgentPwd()
    {    
                setTestStep("VerifyProxyAgentPwd");
                assertEquals("ldapProxyAgentPwd is not set properly ",ldapProxyAgentPwd.verifyProxyPwd(),ldapPAgentPwd);
                       
    }
     
    
}
