package com.ericsson.oss.orchestrator.test.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.oss.orchestrator.test.operators.CacheLatestMediaOperator;
import com.google.inject.Inject;



public class CacheLatestMediaOnMwsTestSteps extends TorTestCaseHelper{
	
	final static Logger logger = LoggerFactory.getLogger(CacheLatestMediaOnMwsTestSteps.class);
	TestContext context = TafTestContext.getContext();
    @Inject
    private CacheLatestMediaOperator cacheOperator;
    
    /**
	 * Check if the latest media on Mws
	 */
    @TestStep(id="checkCachedMediaOnMws")
    public void checkCachedMediaOnMws(@Input("media") String media)
    {
    	setTestStep("checkCachedMediaOnMws");
    	//Get the shipment details from the jenkins job
    	String shipment = cacheOperator.getShipment();
    	assertNotNull("Unable to find Shipment details", shipment);
    	
    	//Get the mode of the run
    	String mode = cacheOperator.getMode();
    	assertNotNull("Unable to find the mode of the RUN ", mode);
    	
    	// Get the product set version for given shipment
    	String psv = cacheOperator.getPsv(shipment,mode);
    	assertNotNull("Unable to get Product set version", psv);
    	DataHandler.setAttribute("psv", psv);
    	
    	// Get the shipment of the media with which they are built
    	String builtShipment = cacheOperator.getShipmentOfPsvMedia(psv, shipment, media);
    	assertNotNull("Unable to get the built shipment of Latest Media",builtShipment);
    	
    	//Get expected cache path w.r.t shipment and media
    	String cachePath = cacheOperator.getMediaExpectedCachePath(media,builtShipment);
    	assertNotNull("Unable to get the expected Cache Path", cachePath);
    	DataHandler.setAttribute(media.toLowerCase() + CacheLatestMediaOperator.CACHE_SUFFIX, cachePath);
  
    	// Get ISO version of media
    	String isoVer = cacheOperator.getISOver(psv, media, shipment);
    	assertEquals("Unable to get ISO version of "+ media + " media from Nexus",cacheOperator.getLastExitCode(),0);
    	DataHandler.setAttribute(media.toLowerCase() +"IsoVer", isoVer);
    	
    	//Check cache path available with latest media on mws
    	boolean already_cached = cacheOperator.checkMediaToDownload(cachePath, media, isoVer);
    	assertNotNull("Unable to Check media to Download or not",already_cached);
    	context.setAttribute(media.toLowerCase() + "Already_cached", already_cached);
    	
    	// Remove the existing cache path if it has lower r-state
    	int exitcode = cacheOperator.checkAndRemoveCachePathOnMws(cachePath, media, isoVer);
		assertEquals("Unable to remove the path " + cachePath + " from mws",exitcode,0);

    }
    /**
     * Download Latest Media from NEXUS
     */
    @TestStep(id="downloadLatestMedia")
    public void downloadLatestMedia(@Input("media") String media)
    {
    	setTestStep("downloadLatestMedia");
		boolean already_cached = context.getAttribute( media.toLowerCase() + "Already_cached");
		String psv = (String)DataHandler.getAttribute("psv");
		String isoVer = (String)DataHandler.getAttribute(media.toLowerCase()+ "IsoVer");
		String shipment = cacheOperator.getShipment();
		
		
		if (! already_cached)
		{
			//Check the media already existed in download Path
			boolean already_downloaded = cacheOperator.checkMediaAlreadyExists(media, isoVer, shipment, psv);
			if (! already_downloaded){
				assertTrue("Unable to Download " + media + " media to MWS as downloadPath has low storage", cacheOperator.storageCheckOnMws());
				//Downloading the latest media from nexus	
				assertEquals("Unable to Downloaded media in download path of mws", cacheOperator.downloadMediaFromNexus(shipment, psv, media),0);
				assertEquals("Unable to find the Downloaded media in download path of mws", cacheOperator.existenceOfMedia(media, isoVer),0);
				String mediaFile = context.getAttribute("mediaFile");	
				//Sha1 checksum of download media			
				assertEquals("Unable to Download " + media + " media", cacheOperator.getMediaSha1OnMws(mediaFile), cacheOperator.getMediaSha1FromNexus(shipment, psv, media));
			}
				
		}
		
    }
	/**
	 * Mount the Downloaded media
	 */
	@TestStep(id="mountMedia")
	public void mountMedia(@Input("media") String media)
	{	
		setTestStep("mountMedia");
		String mountPath="/tmp/temp_"+ media.toLowerCase() +"_iso";
		DataHandler.setAttribute("mountPath",mountPath);
		boolean already_cached = context.getAttribute( media.toLowerCase() + "Already_cached");
		if (! already_cached ) {	
			String mediaFile = context.getAttribute("mediaFile");
			assertNotNull(mediaFile);
			String isoVer = (String)DataHandler.getAttribute(media.toLowerCase()+ "IsoVer");
			assertNotNull(isoVer);
			
			//Getting lofi device
			String lofiDevice = cacheOperator.getLofiadmDevice(mediaFile, isoVer);
			DataHandler.setAttribute("lofiDevice", lofiDevice);
			assertEquals("Unable to get lofi device",cacheOperator.getLastExitCode(),0);
			
			//Mounting OM media to /tmp/temp_om_iso directory
			assertEquals("Unable to mount the media",cacheOperator.mountMedia(lofiDevice, mountPath),0);
		} else {
			logger.info("SKIPPED mounting of " + media + "MEDIA");
		}

	}
	/*
	 * Upgrading ERICjump Package from the latest mount media
	 */
	@TestStep(id="upgradeERICjump")
	public void upgradeERICjump(@Input("media") String media)
	{
		setTestStep("upgradeERICjump");
		String mountPath=(String)DataHandler.getAttribute("mountPath");
		if (media.equals("OM"))
		{
			boolean already_cached = context.getAttribute( media.toLowerCase() + "Already_cached");
			String actualVer;
			if (! already_cached ) {
				//Upgrade the package from mount path
				assertEquals("Unable to upgrade ERICjump package",cacheOperator.upgradeERICjump(mountPath),0);
				actualVer = cacheOperator.getERICjumpVer(mountPath);

			}else {
				//Upgrade the package from cached path
				String cachePath= (String)DataHandler.getAttribute(CacheLatestMediaOperator.OM_CACHE);
				assertEquals("Unable to upgrade ERICjump package",cacheOperator.upgradeERICjump(cachePath + "/om"),0);
				actualVer = cacheOperator.getERICjumpVer(cachePath + "/om");
			}
			int cacheNum =Integer.parseInt(cacheOperator.convertRstateToVersion(actualVer).replace(".", ""));
			int installNum =Integer.parseInt(cacheOperator.convertRstateToVersion(cacheOperator.getERICjumpVerOnMws()).replace(".", ""));
			if (installNum > cacheNum ){
				logger.info("Upgrade of ERICJump Skipped as mws has latest Version");
			}else{
				assertEquals("Version Mismatch of ERICjump package",cacheOperator.getERICjumpVerOnMws(),actualVer);	
			}
			
		}	
	}
	/**
	 * Cache the media on MWS
	 */
	@TestStep(id="cacheMediaOnMws")
	public void cacheMediaOnMws(@Input("media") String media)
	{
		setTestStep("cacheMediaOnMws");
		String mountPath=(String)DataHandler.getAttribute("mountPath");
		//Caching media on Mws
		boolean already_cached = context.getAttribute( media.toLowerCase() + "Already_cached");
		if (! already_cached ) {
			assertEquals("Unable to cache " + media + " Media on MWs", cacheOperator.cacheMedia(media, mountPath),0);
			
			//Checking the cache path on mws
			String cachePath = (String)DataHandler.getAttribute(media.toLowerCase() + CacheLatestMediaOperator.CACHE_SUFFIX);
			assertEquals("Unable to cache " + media + " Media on MWs", cacheOperator.verifyPathAvailabilityOnMws(cachePath),0);
		}else{
			logger.info("SKIPPED caching of " + media + "MEDIA");
		}
		
	}
	/**
	 * Umount the media on MWS
	 * 
	 */
	@TestStep(id="umountMediaOnMws")
	public void umountMediaOnMws(@Input("media") String media)
	{
		setTestStep("umountMediaOnMws");
		String mountPath=(String)DataHandler.getAttribute("mountPath");
		//unmounting the media on mws
		boolean already_cached = context.getAttribute( media.toLowerCase() + "Already_cached");
		if (! already_cached ) {
			//assertEquals("Unable to unmount the media path " + mountPath + "on mws", cacheOperator.umountMedia(mountPath),0);
			if(cacheOperator.umountMedia(mountPath) == 0 ){
				//Removing the path on mws
				assertEquals("Unable to remove the media path " + mountPath + "on mws", cacheOperator.removePathOnMws(mountPath),0);
				
				//Removing lofiadm from mws
				String lofiDevice = (String)DataHandler.getAttribute("lofiDevice");
				assertEquals("Unable to delete the lofidevice on mws",cacheOperator.deleteLofiDevice(lofiDevice),0);
				//Removing media which is downloaded 
				assertEquals("Unable to remove download Path from MWS", cacheOperator.removeDownloadPath(),0);
			}	
		}
	}
	
	
}
