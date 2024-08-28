package com.ericsson.oss.orchestrator.test.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.oss.orchestrator.test.operators.MigratePeerMCOperator;
import com.google.inject.Inject;

public class MigratePeerMCSteps extends TorTestCaseHelper {

    final Logger logger = LoggerFactory.getLogger(MigratePeerMCSteps.class);
            
    @Inject
    private MigratePeerMCOperator migratepeermc;
    
    /**
     * Verify all fth instances against peer server and create if needed 
     */
        
    @TestStep(id="verifyFthInstanceTowardsPeer")
    public void verifyFthInstanceTowardsPeer(){
    	{    		
    		 
    		if(migratepeermc.getPeerServer("peer1")!=null)
    		{
    			Boolean retVal = migratepeermc.nmsadmpasswordless("peer1");
    			assertTrue(" nmsadm user passsword less connection not happened against peer1", retVal);
    			retVal = migratepeermc.verifyandMigrateBootstrapMC();
    			assertTrue(" Failed to verify Bootstrap MC ", retVal);
    			retVal = migratepeermc.verifyandCreateFthInstance("1","peer1");
    			assertTrue(" Failed to verify FTH_1 MC instance ", retVal);
    			retVal = migratepeermc.verifyandCreateFthInstance("2","peer1");
    			assertTrue(" Failed to verify FTH_2 MC instance ", retVal);    			
    		}
    		
    		if(migratepeermc.getPeerServer("peer2")!=null)
    		{
    			Boolean retVal = migratepeermc.nmsadmpasswordless("peer2");
    			assertTrue(" nmsadm user passsword less connection not happened against peer2", retVal);
    			retVal = migratepeermc.verifyandCreateFthInstance("3","peer2");
    			assertTrue(" Failed to verify FTH_3 MC instance ", retVal);
    			retVal = migratepeermc.verifyandCreateFthInstance("4","peer2");
    			assertTrue(" Failed to verify FTH_4 MC instance ", retVal);    			
    		}
    		
    		if(migratepeermc.getPeerServer("peer3")!=null)
    		{
    			Boolean retVal = migratepeermc.nmsadmpasswordless("peer3");
    			assertTrue(" nmsadm user passsword less connection not happened against peer3", retVal);
    			retVal = migratepeermc.verifyandCreateFthInstance("5","peer3");
    			assertTrue(" Failed to verify FTH_5 MC instance ", retVal);
    			retVal = migratepeermc.verifyandCreateFthInstance("6","peer3");
    			assertTrue(" Failed to verify FTH_6 MC instance ", retVal);    			
    		}
    		
    		if(migratepeermc.getPeerServer("peer4")!=null)
    		{
    			Boolean retVal = migratepeermc.nmsadmpasswordless("peer4");
    			assertTrue(" nmsadm user passsword less connection not happened against peer4", retVal);
    			retVal = migratepeermc.verifyandCreateFthInstance("7","peer4");
    			assertTrue(" Failed to verify FTH_7 MC instance ", retVal);
    			retVal = migratepeermc.verifyandCreateFthInstance("8","peer4");
    			assertTrue(" Failed to verify FTH_8 MC instance ", retVal);    			
    		}
    		
    		if(migratepeermc.getPeerServer("peer5")!=null)
    		{
    			Boolean retVal = migratepeermc.nmsadmpasswordless("peer5");
    			assertTrue(" nmsadm user passsword less connection not happened against peer5", retVal);
    			retVal = migratepeermc.verifyandCreateFthInstance("9","peer5");
    			assertTrue(" Failed to verify FTH_9 MC instance ", retVal);
    			retVal = migratepeermc.verifyandCreateFthInstance("10","peer5");
    			assertTrue(" Failed to verify FTH_10 MC instance ", retVal);    			
    		} 
    	} 
    		
    	}  

}
