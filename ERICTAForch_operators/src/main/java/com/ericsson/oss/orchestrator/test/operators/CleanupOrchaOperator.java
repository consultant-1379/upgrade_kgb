package com.ericsson.oss.orchestrator.test.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.google.inject.Singleton;

@Operator(context = Context.CLI)
@Singleton
public class CleanupOrchaOperator extends PCJCommonOperator{    
    /** Operator to perform cleanup operations **/
               
    private Logger logger = LoggerFactory.getLogger(CleanupOrchaOperator.class);
                
    /**removes the ldap proxy agent password by executing script
     * @return int ,exit value of the command
     */
    public  int removeLdapProxyAgent()
    {
    	String cmd = DataHandler.getAttribute(LDAP_CLIENT_UPDATE_SCRIPT) + " -r " + getOssHost().getIp();
    	return runSingleCommandOnHost(getMsHost(),cmd,false,false);
	}
    
    /** 
     * Tests if the orcha_ldap_proxy exists
     * 
     * @return 0, if the orcha_ldap_proxy exists, non-zero otherwise
     */
    public  int testProxyPwd(){
    
        String cmd="/opt/ericsson/bin/pwAdmin -get LDAP NORMAL orcha_ldap_proxy";
        return runSingleCommandOnHost(getOssHost(),cmd,false,true);
    
    }
         
    
}

