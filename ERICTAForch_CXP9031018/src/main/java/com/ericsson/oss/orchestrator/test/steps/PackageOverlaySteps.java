package com.ericsson.oss.orchestrator.test.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.oss.orchestrator.test.operators.PackageOverlayOperator;
import com.google.inject.Inject;



public class PackageOverlaySteps extends TorTestCaseHelper {
	
	final Logger logger = LoggerFactory.getLogger(PackageOverlaySteps.class);
	
	  
    private static String adminHostname = "";
    String shipment = "";
    private static String rstate = "";

	
	@Inject
    private PackageOverlayOperator copypkgs;
	
	private static final String COPY_PKGS = "pkgs";
	public static String physicalmws="";

	
	
	@TestStep(id="getShipmentandMediaPath")
    public void getShipmentandMediaPath(@Input("shipment") String shipment) {
      
       setTestStep("getShipmentandMediaPath");
      
       shipment = (String) DataHandler.getAttribute("shipment");
	
       shipment = copypkgs.getShipment();
       System.out.println("Shipment is"+shipment);
   	   assertNotNull("Unable to find Shipment details", shipment);
   	   
  // 	physicalmws = (String) DataHandler.getAttribute("physicalmws");
   	
   // System.out.println("Physical MWS ip"+physicalmws);
	  // assertNotNull("Unable to find MWS details", physicalmws);
   	  // String builtShipment = copypkgs.getMediaExpectedCachePath(shipment);
	   // assertNotNull("Unable to get the built shipment of Latest Media",builtShipment);
	
        String ossrcMediaPath = (String)DataHandler.getAttribute(copypkgs.OSSRC_CACHE);
    
                   
    }
	@TestStep(id="CopyUploadedPackagestoMedia")
    public void CopyUploadedPackagestoMedia(@Input("physicalmws") String physicalmws){
		TafConfiguration configuration = TafConfigurationProvider.provide();
	   	physicalmws=(String) configuration.getProperty("testware.physicalmws");
		//physicalmws = (String) DataHandler.getAttribute("physicalmws");
		System.out.println("Physical MWS ip"+physicalmws);
    	setTestStep("CopyUploadedPackagestoMedia");
    	System.out.println("Physical MWS ip"+physicalmws);
    	String retVal1 = copypkgs.copyuploadedpkgs(physicalmws);
    	assertEquals(" Packages are copied to MWS", retVal1, "0");
	}

}