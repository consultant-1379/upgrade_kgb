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
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Operator(context = Context.CLI)
@Singleton
public class LdapProxyAgentPwdOperator extends PCJCommonOperator{    
    /** Operator to populate LdapProxy Agent Pwd **/
                
                @Inject
    private CLICommandHelper cli;
               
    private Logger logger = LoggerFactory.getLogger(LdapProxyAgentPwdOperator.class);
    
               
    /**sets the ldap proxy agent password by executing script
     * @param  ldapPAgentPwd String parameter
     * @return int ,exit value of the command
     */
    
    public  int executeProxyAgentCommand(String ldapPAgentPwd,String ldapAdminPwd,String ldapMainPwd,String bSIMPwd,String cOMPwd,String ipsecPwd,String rootPwd,String oMSASIP,String biSMRSUpg,String pwdSMRS_Master, String pwdSMRS_ftp)
    {
                String shipment1= (String) DataHandler.getAttribute("shipment");
                String delimiter = "\\.";
                String []splitted=shipment1.split(delimiter);
                String firstTerm =splitted[0];
                String middleTerm=splitted[1];
                String lastTerm =splitted[2];
                int no_1=Integer.parseInt(firstTerm);
                int no_2=Integer.parseInt(middleTerm);
                int no_3=Integer.parseInt(lastTerm);
                  
      
      if ( (no_1 == 16 && no_2 == 0 && no_3 >= 4) || ( no_1 == 16 && no_2 > 0 ) || (no_1 >=17) )
      {
        String cmd="/ericsson/orchestrator/bin/orc_prepare_tss_for_upgrade.bsh " + PCJCommonOperator.ossGrpAdminServerHost.getIp();
        logger.info ("Executing Ldap Proxy Agent Command on MWS  " + cmd);
        User orchaUser=getMsHost().getUsers(UserType.OPER).get(0);
        initializeHelper(getMsHost(),orchaUser);
        cli.DEFAULT_COMMAND_TIMEOUT_VALUE = 244000;
        runInteractiveScript(cmd);
        putDelay(30000);
        int exit=1;
        int flag=0;
        
        if(interactWithShell("Password for LDAP proxy agent (press enter not to set password):",ldapPAgentPwd))
     { putDelay(20000);
           	if (interactWithShell("Confirm password for LDAP proxy agent:",ldapPAgentPwd))          
         {	flag++;
     }  else   {
             logger.info("Confirm password for LDAP proxy agent:expect interactive command did not occur");
             closeShell();
             return exit;
         }
     }  else    {
              logger.info("password for LDAP proxy agent:expect interactive command did not occur");
              closeShell();
              return exit;
     }

       if (interactWithShell("Password for LDAP Directory Manager (press enter not to set password):",ldapAdminPwd))                 
     { putDelay(20000);
     		if(interactWithShell("Confirm password for LDAP Directory Manager:",ldapAdminPwd))
         {	flag++;
     }	else	{             
             logger.info("Confirm Password for LDAP Directory Manager:expect interactive command did not occur");
             closeShell();
             return exit;
         }
     }	else	{
             logger.info(" Password for LDAP Directory Manager:expect interactive command did not occur");
             closeShell();
             return exit;
         }


       if (interactWithShell("Password for LDAP Maintenance account  (press enter not to set password):",ldapMainPwd))
      {	putDelay(20000);
           if(interactWithShell("Confirm password for LDAP Maintenance account :",ldapMainPwd))
         { flag++;
     }	else	{                           
            logger.info("Confirm Password for LDAP Maintenace Manager:expect interactive command did not occur");
            closeShell();
            return exit;
         }
      }	else	{
              logger.info(" Password for LDAP Maintenance Manager:expect interactive command did not occur");
              closeShell();
              return exit;
         }


putDelay(60000);
String cmd1 = "ls -lrt /ericsson/orchestrator/log/orc_prepare_tss_for_upgrade_* > /test1";
String cmd2 = "awk '{print $NF}' /test1 | sed -n '$p' | xargs grep 'Retrieved password of existing bsimuser from TSS - no user input required'" ;

int value1=this.runSingleCommandOnMwsAsRoot(cmd1, false);
int value2=this.runSingleCommandOnMwsAsRoot(cmd2, false);
String output_match = getLastOutput();
if(!output_match.contains("Retrieved password of existing bsimuser from TSS - no user input required"))
{
	if (interactWithShell("Password for BSIM User (press enter not to set password):",bSIMPwd))
         {	putDelay(20000);
            if(interactWithShell("Confirm password for BSIM User:",bSIMPwd))
     { 	flag++;
     }	else	{                       
            logger.info("Confirm password for BSIM User:expect interactive command did not occur");
            closeShell();
            return exit;
         }
    }	else	{
             logger.info("Password for BSIM User (press enter not to set password):expect interactive command did not occur");
             closeShell();
             return exit;
     } 
	}  else {
		logger.info("Retrieved password of existing bsimuser from TSS - no user input required");
    }

	if (interactWithShell("Password for COM User (press enter not to set password):",cOMPwd))
         {	putDelay(20000);
         	if(interactWithShell("Confirm password for COM User:",cOMPwd))
         	{	flag++;
         }	else	{                  
                logger.info("Confirm password for COM User:expect interactive command did not occur");
                closeShell();
                return exit;
         } 
         }  else	{
                logger.info("Password for COM User (press enter not to set password):expect interactive command did not occur");
                closeShell();
                return exit;
         }  

	if (interactWithShell("Password for ipsecsmrs UNIX User (press enter not to set password):",ipsecPwd))
     	{  putDelay(20000);
     	if(interactWithShell("Confirm password for ipsecsmrs UNIX User:",ipsecPwd))
     	{	 flag++;
     	}  else	{                  
               logger.info("Confirm password for ipsecsmrs UNIX User:expect interactive command did not occur");
               closeShell();
               return exit;
         }
         }	else	{
                logger.info("Password for ipsecsmrs UNIX User (press enter not to set password):expect interactive command did not occur");
                closeShell();
                return exit;
         } 

if (interactWithShell("Password for root user password on ADMIN server(s). AES Encrypted (press enter not to set password):",rootPwd))
     {	putDelay(20000);
     	if(interactWithShell("Confirm password for root user password on ADMIN server(s). AES Encrypted:",rootPwd))
     {	flag++;
     }  else	{                  
            logger.info("Confirm password for root user password on ADMIN server(s). AES Encrypted:expect interactive command did not occur");
            closeShell();
            return exit;
     }
     }  else	{
             logger.info("Password for root user password on ADMIN server(s). AES Encrypted (press enter not to set password):expect interactive command did not occur");
             closeShell();
             return exit;
         }

if(oMSASIP == null)
	{	putDelay(20000);
		if (interactWithShell("Please enter the OMSAS IP Address (just press enter if no OMSAS):","\n"))
     {	flag++;
     }  else	{
            logger.info("Please enter the OMSAS IP Address (just press enter if no OMSAS):expect interactive command did not occur");
            closeShell();
            return exit;
      }
	  }	else 	{	
		  putDelay(20000);
	if (interactWithShell("Please enter the OMSAS IP Address (just press enter if no OMSAS):",oMSASIP))
	  {	flag++;
	  }	else	{
	        logger.info("Please enter the OMSAS IP Address (just press enter if no OMSAS):expect interactive command did not occur");
	        closeShell();
	        return exit;
	          }
}

	putDelay(150000);
	if(interactWithShell("Will BI-SMRS upgrade be carried out as part of this upgrade?(y/[n])?:",biSMRSUpg))
	{	
		putDelay(80000);   
      	if(biSMRSUpg.equals("y"))
      	{	
      		putDelay(80000);
      		if (interactWithShell("Password for root user password on SMRS Master server. AES Encrypted (press enter not to set password):",pwdSMRS_Master))
      			{	
      			putDelay(30000);
      			if(interactWithShell("Confirm password for root user password on SMRS Master server. AES Encrypted:",pwdSMRS_Master))
      			{	
      				flag++;
      			}	
      			else 
      			{                  
      				logger.info("Confirm password for root user password on SMRS Master server. AES Encrypted:expect interactive command did not occur");
      				closeShell();
      				return exit;                   
      			}
      			}	
      		else	
      		{
      				logger.info("Password for root user password on SMRS Master server. AES Encrypted (press enter not to set password):expect interactive command did not occur");
      				closeShell();
      				return exit;
      		}
      		
      		putDelay(70000);                               
if (interactWithShell("Password for SMRS ftpservice (press enter not to set password):",pwdSMRS_ftp))
    {	
	putDelay(60000);
    	
	if(interactWithShell("Confirm password for SMRS ftpservice:",pwdSMRS_ftp))
    	{	
		flag++;                                        
    	}	
	else	
	{                  
            logger.info("Confirm password for SMRS ftpservice:expect interactive command did not occur");
            closeShell();
            return exit;
    }
    }	
else	{
             logger.info("Password for SMRS ftpservice (press enter not to set password):expect interactive command did not occur");
             closeShell();
             return exit;
    }
    }	
      	
      	else	{
    		 logger.info("BI-SMRS Upgrade does not required");
    }
	} 
        putDelay(40000);
        exit=getCommandExitValue();
        putDelay(40000);
        logger.info("getLASTexitcode :"+exit);
        logger.info("interactive shell for orc_prepare_tss_for_upgrade.bsh is executed properly ,Check the successful logs");
        closeShell();      
        return exit;
      }	else	{
        String cmd="/ericsson/orchestrator/bin/admin_prepare_for_ldap_client_update.bsh " + PCJCommonOperator.ossGrpAdminServerHost.getIp();
        logger.info ("Executing Ldap Proxy Agent Command on MWS  " + cmd);
        User orchaUser=getMsHost().getUsers(UserType.OPER).get(0);
        initializeHelper(getMsHost(),orchaUser);
        cli.DEFAULT_COMMAND_TIMEOUT_VALUE = 144000;
        runInteractiveScript(cmd);
        putDelay(30000);
        int exit=1;
            
        if(interactWithShell("Password for LDAP proxy agent:",ldapPAgentPwd))
        {  putDelay(30000);  
        		if (interactWithShell("Confirm password for LDAP proxy agent:",ldapPAgentPwd))
                {     putDelay(30000);
                      if (interactWithShell("Password for LDAP Directory Manager:",ldapAdminPwd))
                      {	putDelay(30000);
                            if(interactWithShell("Confirm password for LDAP Directory Manager:",ldapAdminPwd))
                                { putDelay(30000);
                                  exit=getCommandExitValue();
                                  putDelay(30000);
                                  logger.info("getLASTexitcode"+exit);
                                  logger.info("interactive shell for admin_prepare_for_ldap_client_update.bsh is executed properly ");
                                  closeShell();      
                                  return exit;
                        }	else	{                          
                                logger.info("Confirm Password for LDAP Directory Manager:expect interactive command did not occur");
                                closeShell();
                                return exit;
                        }
                    }	else   {
                    	logger.info(" Password for LDAP Directory Manager:expect interactive command did not occur");
                    	closeShell();
                    	return exit;         
                  }
                 }	else	{
                	 logger.info("Confirm password for LDAP proxy agent:expect interactive command did not occur");
                	 closeShell();
                	 return exit;
                  }
            }	else	{
                logger.info("password for LDAP proxy agent:expect interactive command did not occur");
                closeShell();
                return exit;
            }
                }
    }
    /**Fetches the password from tss using PwAdmin command 
     * 
     * @return String, password which is fetched using command 
     */
    public  String verifyProxyPwd(){
    
    String cmd="/opt/ericsson/bin/pwAdmin -get LDAP NORMAL orcha_ldap_proxy";
   
    String exitString="";
    if (runSingleCommandOnHost(PCJCommonOperator.ossGrpAdminServerHost,cmd,true,true)!=0)
    {	putDelay(30000);
    	logger.info("fetch password command pwAdmin command not executed properly");
        return exitString;
                
    }	else	{
    	putDelay(30000);
        logger.info("fetch password command pwAdmin command executed properly");                            
    }
    	return(getLastOutput());
    }
    
    public int Checkforvalue(String value){
    	if(value.equals("y")||value.equals("n") )
    	{
    		return 0;
    	}
    	return 1;
    }
     
}
