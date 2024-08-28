
package com.ericsson.oss.orchestrator.test.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.google.inject.Singleton;
import com.ericsson.cifwk.taf.data.DataHandler;

/** Operator for generating MC exclusion list **/
@Singleton

public class MigratePeerMCOperator extends PCJCommonOperator {

	private Logger logger = LoggerFactory.getLogger(MigratePeerMCOperator.class) ;
	private Boolean allMCsUp = false;
	Boolean create_fth1= false;
	
	/**
	 ** verify and migrate bootstrap if needed
	 ** @return - true if bootstrap is configured on peer
	 **/
	public Boolean verifyandMigrateBootstrapMC(){
		
		int exitVal = runSingleCommandOnHost(getOssHost() ,"cat /etc/hosts", false, true);
        if(exitVal!=0)
        { 			
 			logger.info("failed to run command on admin server");
 			return false;
 		}
		logger.info("exit value printing "+exitVal);
 		logger.info("printing command output"+getLastOutput());
 		String[] eachLine = getLastOutput().split("\\n" );
		int numline = eachLine.length;
		String peerIP = null;
		peerIP = (String)getPeerServerIP("peer1");
		String peerhostname = getPeerServer("peer1");
		logger.info("peerIP and peerhostname is "+peerIP+" "+peerhostname);
		for (int i =0; i < numline ; i++)
		{
			if(eachLine[i].contains("ftpmservice"))
			{
				if(eachLine[i].contains(peerIP))
				{ 
					if(checkMCOnline("fth_bootstrap"))
					{
						logger.info("boot strap is mapped to peer1");
						if(checkProcessonPeer(peerhostname, "fth_bootstrap"))
						{
							return true;
						}
						
					}
					else
					{
						if(onlineMC("fth_bootstrap"))
						{
							if(checkProcessonPeer(peerhostname, "fth_bootstrap"))
							{
								return true;
							}
							
						}
					}
				}
				else
				{
					if(migrateBootStrapMC())
					{
						if(verifyandMigrateBootstrapMC())
						{
							return true;
						}
					}
					
				}
						
		}
				
		}
		
		logger.info(" There is no ftmp service in /etc/hosts file");
		return false;
	}
	
	/**
	 ** Migrates bootstrap MC to peer server
	 ** @return - true if bootstrap is migrated to peer
	 **/
	
	public Boolean migrateBootStrapMC()
	{
		
		String peerhostname = getPeerServer("peer1");
				
		String offline = "/opt/ericsson/nms_cif_sm/bin/smtool -offline fth_bootstrap -reason=other -reasontext=other" ; 
		String online = "/opt/ericsson/nms_cif_sm/bin/smtool -online fth_bootstrap";
		String cmd1 = "sed 's/ftpmservice//g' /etc/hosts > /etc/hosts_new";
		String cmd2 = "sed '1,/"+peerhostname+"/s//"+peerhostname+" ftpmservice/' /etc/hosts_new > /etc/hosts_new2 ";
		String cmd3 = "cp /etc/hosts_new2 /etc/hosts";
		String cmd4 = "rm /etc/hosts_new /etc/hosts_new2";
		String finalcmd = cmd1+";"+cmd2+";"+cmd3+";"+cmd4;
		
		int exitVal = runSingleCommandOnHost(getOssHost() ,offline, false, true);
        
		if(exitVal!=0){
 			
 			logger.info("failed to run command on admin server");
 			return false;
 		}
		
		putDelay(60000);
		exitVal = runSingleCommandOnHost(getOssHost() ,finalcmd, false, true);
        if(exitVal!=0){
 			
 			logger.info("failed to run command on admin server");
 			return false;
 		}         
		
		exitVal = runSingleCommandOnHost(getOssHost() ,online, false, true);
        if(exitVal!=0){
 			
 			logger.info("failed to run command on admin server");
 			return false;
 		}
		
        putDelay(120000);
		if(checkMCOnline("fth_bootstrap"))
		{
			logger.info("MC is online");
			if(checkProcessonPeer(peerhostname, "fth_bootstrap"))
			{
				logger.info("fth_bootstrap process is running on peer server");
				return true;
			}
			else
			{
				logger.info("fth_bootstrap process is not running on peer server, verify it");
				return false;
			}
		}
		else
		{
			logger.info("fth_bootstrap MC is not online : ");
			return false;
		}
				
	}
	
	/**
	 ** verifies fth instance and creates if required
	 ** @param fthnum - fth number
	 ** @param peerserver - hostname of peerserver
	 ** @return - true fth instance is created against corresponding peer
	 **/
	public Boolean verifyandCreateFthInstance(String fthnum, String peerserver){
		
		String peerhostname = null ;
		String peerIP = null;
		Boolean IPfound = false;
		Boolean asnFound = false;
		Boolean asnFound1 = false;
		
		logger.info("verifying fth"+fthnum+"on "+peerserver);
 		String asn = "asn_"+fthnum;
 		String fthmc = "fth_"+fthnum;
 		int exitVal = runSingleCommandOnHost(getOssHost() ,"cat /etc/hosts", false, true);
        if(exitVal!=0){
 			
 			logger.info("failed to run command on admin server");
 			return false;
 		} 
		
        String[] eachLine = getLastOutput().split("\\n" );
		int numline = eachLine.length;
		
		peerhostname = getPeerServer(peerserver);
		peerIP = getPeerServerIP(peerserver);
		
		logger.info("peer host name is : "+peerhostname +"server ip is : "+peerIP);
						
		for (int i =0; i < numline ; i++)
		{
			if(eachLine[i].contains(peerIP))
			{
				IPfound = true;
				if(eachLine[i].contains(asn))
				{
					asnFound = true;
					break;
				}
			}
			if(eachLine[i].contains(asn))
			{
				asnFound1 = true;
				
			}
		}
		
		// if FTH MC instance is already created
		if(IPfound == true && asnFound == true ) 
		{
			if(checkMCOnline(fthmc))
			{
				if(checkProcessonPeer(peerhostname, "fth_"+fthnum))
				{
					return true;
				}
				else
				{
					logger.info("PID of " + "fth_"+fthnum + " doesnot exists on "+peerhostname);
					return false;
				}
				
			}
			else
			{
				if(onlineMC(fthmc) && checkMCOnline(fthmc) && checkProcessonPeer(peerhostname, "fth_"+fthnum))
				{
					return true;
				}
				else
				{
					logger.info("failed to online MC and verify PID of " + "fth_"+fthnum + "on peer: "+peerhostname);
					return false;
				}
								
			}			
		}
				
		// fth instance is created against other peer
		if(asnFound1 == true)
		{
			if (moveMCInstance(fthnum , peerserver) && checkMCOnline(fthmc) && checkProcessonPeer(peerhostname, "fth_"+fthnum))
			{
				return true;
			}
			else
			{
				logger.info(" failed to move fth instance, its status and process id on peer");
				return false;
			}
			
		}
		
		// No fth instance existing , create the fth instance
		if(IPfound == true && asnFound == false)
		{
			if(createMCinstance(peerhostname) && verifyandCreateFthInstance(fthnum, peerserver))
			{
				return true;
			}
			else
			{
				logger.info("while creating new fth instance, failed to create instance and verify it");
				return false;
			}
					
		} 		 		
 		return false;
	   
	}
	
	
		
	/**
	 ** Onlines MC
	 ** @param fthmc MC to be onlined
	 ** @return - true if MC is online
	 **/
	
	public Boolean onlineMC(String fthmc){
		
		String cmd = "/opt/ericsson/nms_cif_sm/bin/smtool -online "+fthmc;
		int exitVal = runSingleCommandOnHost(getOssHost() ,cmd, false, true);
        if(exitVal!=0){
 			
 			logger.info("failed to run command on admin server");
 			return false;
 		}
		
        putDelay(7000);
        
        if(checkMCOnline(fthmc))
        {
        	logger.info(""+fthmc+" MC is ONLINE");
        	return true;
        }
		return false;
	}
	
	/**
	 ** verifies process id of fth on peerserver
	 ** @param peerhost - hostname of peerserver
	 ** @param fthmc - fth MC instance
	 ** @return - true if pid of fth exists on peerserver
	 **/
	
	public Boolean checkProcessonPeer(String  peerhost , String fthmc){		
		
		User root = getOssHost().getUsers(UserType.ADMIN).get(0);
		initializeHelper(getOssHost() , root);
	    runInteractiveScript("ssh root@"+peerhost);
	    putDelay(10000);
	    Boolean retVal = interactvShell("Password:", "shroot");
	    putDelay(5000);
 		
 		retVal = interactvShell("#", "/usr/bin/ps -ef | grep fth");
 		putDelay(5000);
 		
 		String output2 = expect("fth");
 		 		
 		logger.info("printing output value"+output2);
 		
 		String[] eachLine = output2.split("\\n" );
		int numline = eachLine.length;
		
		logger.info("val for number of lines"+numline);
 		
		for(int i=0; i< numline ; i++)
		{
			logger.info("printing each line"+eachLine[i]);
			if(eachLine[i]!= null &&eachLine[i].contains(fthmc+" "))
	 		{
	 			logger.info("Pid is already created in peer server for "+fthmc);
	 			return true;
	 		}
		} 
 				
		logger.info("returning false ");
		return false;
	}
	
	/**
	 ** moves MC instance from one peerserver to other
	 ** @param mcinstance - mcinstance
	 ** @param targetpeer - target peerserver
	 ** @return - true if MC instance is moved sucessfully
	 **/
	
	public Boolean moveMCInstance(String mcinstance , String targetpeer){
		
		User root = getOssHost().getUsers(UserType.ADMIN).get(0);
		initializeHelper(getOssHost() , root);
	    runInteractiveScript("/opt/ericsson/sck/peer_management/manage_peer");
	    putDelay(10000);
	    Boolean retVal = interactvShell("Please enter selection:", "3");
	    if(!retVal)
		 {
			 logger.info("exception happened in interactive script");
			 closeShell();
			 return retVal;
		 }
	    putDelay(10000); //1 min(1*60*1000)
	    
	    retVal = interactvShell("Please enter the corresponding application number", mcinstance);
	    if(!retVal)
		 {
			 logger.info("exception happened in interactive script");
			 closeShell();
			 return retVal;
		 }
	    putDelay(10000); //1 min(1*60*1000)
	    
		for (int i=0 ; 4 > i  ; i++)
		{
			logger.info("inside for loop");
			retVal = interactvShell(":", targetpeer);
			putDelay(10000);
			if(!retVal)
			 {
				 logger.info("exception happened in interactive script");
				 closeShell();
				 return retVal;
			 }
		}
				
		return true;
	}
	
	/**
	 ** Verify password less connection between admin and peer for nmsadm user
	 ** @param  peerserver 
	 ** @return - true if MCs are in proper state other wise false
	 **/
	
	public Boolean nmsadmpasswordless(String peerserver){
		
		User root = getOssHost().getUsers(UserType.ADMIN).get(0);
		String peerhostname = getPeerServer(peerserver);
		String peerIP = getPeerServerIP(peerserver);
		String cmd = "su - nmsadm -c "+"\""+"/usr/bin/ssh -o StrictHostKeyChecking=no "+peerIP+"\"" ;
		initializeHelper(getOssHost() , root);
		runInteractiveScript(cmd);		
		
		putDelay(3000);
		Boolean retVal = interactWithShell("nmsadm", "hostname");
		if(!retVal)
		 {
			 logger.info("exception happened in interactive script, expected value dint occur");
			 return false;
		 }
		putDelay(2000);
		String output = expect("hostname");
		putDelay(2000);		
		
		logger.info(" output of passwordless connection : "+output);
		if ( !(output == null || output.isEmpty()))
		{
			if(output.contains(peerhostname))
			{
				logger.info(" passwordless connection successful");
				return true;
			}
			else
			{
				logger.info(" passwordless connection has failed");
				return false;
			}
		}
		
		return false;
	}	
	
	/**
	 ** Creates MC instance
	 ** @param peerhostname
	 ** @return - true if fth instance is created
	 **/
	
    public Boolean createMCinstance(String peerhostname){
		
		User root = getOssHost().getUsers(UserType.ADMIN).get(0);
		initializeHelper(getOssHost() , root);
	    runInteractiveScript("/opt/ericsson/sck/peer_management/manage_peer");
	    putDelay(10000);
	    Boolean retVal = interactvShell("Please enter selection:", "1");
	    if(!retVal)
		 {
			 logger.info("exception happened in interactive script");
			 closeShell();
			 return retVal;
		 }
	    putDelay(10000); //1 min(1*60*1000)
	    //Boolean retVal ;//= interactWithShell("correct:", "y");
			    
		for (int i=0 ; 5 > i  ; i++)
		{
			retVal = interactvShell("--", peerhostname);
			putDelay(10000);
			if(!retVal)
			 {
				 logger.info("exception happened in interactive script");
				 closeShell();
				 return retVal;
			 }
		}
				
		return true;
	}	
	
	private Boolean interactvShell(String qes, String answer) {
        try {
        	
        	String output2 = expect(qes);
        	logger.info("printing output value"+output2);
        	if(output2.contains("Is this correct"))
        		{        			   
        			runInteractiveScript("y");
            		return true;
            		 
        		}else if(output2.contains("creation successful") || output2.contains("move successful")){
        			runInteractiveScript("s");
        		}else if (output2.contains("SSR Adjust successful")){
        			logger.info("inside adjust successfull");
        			runInteractiveScript("q");
        		}  else {
        			runInteractiveScript(answer);
        		}
        		
             } catch(Exception e)
             {
            	 logger.error( e.getMessage()); 
            	 return false;
             }
        return true;
     }		
}
