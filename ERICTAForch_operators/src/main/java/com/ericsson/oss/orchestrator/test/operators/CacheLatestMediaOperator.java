package com.ericsson.oss.orchestrator.test.operators;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.google.inject.Singleton;




@Operator(context = Context.CLI)
@Singleton
public class CacheLatestMediaOperator extends PCJCommonOperator {

	/** Operator for all PCJ **/
	private Logger logger = LoggerFactory.getLogger(CacheLatestMediaOperator.class);
	private final static String WGET = "/usr/sfw/bin/wget";
	private final static String CI_LINK ="https://cifwk-oss.lmera.ericsson.se";
	private final static String CI_PROXY_LINK ="https://arm901-eiffel004.athtem.eei.ericsson.se:8443";
	private TestContext context = TafTestContext.getContext();
	private String mode;
	private String mediaFile;
	private String downloadPath = (String)TafConfigurationProvider.provide().getString("downloadPath", "/JUMP/PSV/");
	

	/**
	 * Returns the mode(KGB/CDB) from the property file
	 * @return
	 */
	public String getMode()
	{
		logger.info("Getting the mode from the job" );
		mode = (String) DataHandler.getAttribute("mode");
		return(mode);
	}

	/**
	 * Gets the psv from propertyfile if mode is CDB or runs rest call for shipment
	 * @param shipment ex:15.2.1
	 * @param mode ex CDB/ KGB
	 * @return Product set version of the given shipment 
	 */
	
	public String getPsv( String shipment, String mode)
	{
		logger.info("Testing");
		logger.info("Getting PSV for the shipment "+ shipment + " for the mode " + mode);
		if (mode.toLowerCase().equals("cdb"))
		{
			logger.info("Reached cdb");
			String psv = (String) DataHandler.getAttribute("psv");
			if (psv.toLowerCase().equals("latest")){
				logger.info("Reached latest cdb");
				String url = CI_LINK+"/getProductSetVersions/?drop=" + shipment + "&productSet=OSS-RC";
				String command = WGET+ " -q -O - --no-check-certificate --ignore-length '"+ url +"'" +" | nawk -F '</*version>' '{print $2}' | head -1";
				if (runSingleCommandOnMwsAsRoot(command) == 0){ return(getLastOutput()); }
				return(null); 
			}
			else{
				logger.info("Reached specific cdb");
				return(psv);
			}
	
		}else
		{
			logger.info("Reached kgb");
			String url = CI_LINK+"/getLastGoodProductSetVersion/?drop=" + shipment + "&productSet=OSS-RC";
			String command = WGET+ " -q -O - --no-check-certificate --ignore-length '"+ url +"'";
			if (runSingleCommandOnMwsAsRoot(command) == 0){ return(getLastOutput()); }
			return(null); 
		}
		
	}
	/**
	 * Runs the rest call to get the json file and greps the media to get the respective version
	 * @param psv
	 * @param iso - ex : OM, COMINF, Security, OSS-RC
	 * @param shipment
	 * @return version of iso in product set
	 * 
	 */
	public String getISOver(String psv, String iso, String shipment)
	{
		logger.info("Getting ISO verison of "+ iso + " for "+ shipment + " of psv " + psv);
		if (iso.equals("OSSRC")){ iso = "OSS-RC"; }
		if (iso.equals("OMSAS")){ iso = "Security"; }
		if (iso.equals("SOLARIS")){ iso = "Solaris"; }
		String command = WGET+ " -q -O - --no-check-certificate --ignore-length '"+ CI_LINK +"/getProductSetVersionContents/?drop=" + shipment + "&productSet=OSS-RC&version=" + psv +"&pretty=true' | grep athloneUrl | awk -F: '{print $4}' | grep ERIC"+ iso +" | awk -F/ '{print $(NF-1)}'";
		if (runSingleCommandOnMwsAsRoot(command) == 0){ return(getLastOutput()); }
		return(null); 
	}
	
	/**
	 *Runs lofiadm mediaPath command on mws  
	 * @param mediaFile
	 * @param mediaVer 
	 * @return lofidevice ex- /dev/lofi/1
	 */
	public String getLofiadmDevice(String mediaFile , String mediaVer)
	{
		logger.info("Getting lofiadm device of "+ mediaFile);
		String command = "lofiadm " + downloadPath + mediaFile;
		if (runSingleCommandOnMwsAsRoot(command) == 0){ return(getLastOutput()); } 
		command = "lofiadm -a " + downloadPath + mediaFile;
		if (runSingleCommandOnMwsAsRoot(command) == 0){ return(getLastOutput()); }
		return(null);
	}

	/**
	 * Runs mount -F command on MWS 
	 * @param lofiDev
	 * @param mountPath
	 * @return exit value of the command
	 */
	
	public int mountMedia(String lofiDev, String mountPath)
	{
		if ( runSingleCommandOnMwsAsRoot("mount | grep " + lofiDev ) == 0)
		{
			runSingleCommandOnMwsAsRoot("mount | grep " + lofiDev + " | awk '{print $1}'");
			umountMedia(getLastOutput().trim());
		}else if ( runSingleCommandOnMwsAsRoot("mount | grep " + mountPath ) == 0){
			umountMedia(mountPath);
		}		
		makeDirOnMws(mountPath);
		logger.info("Mounting device "+ lofiDev + " to " + mountPath);
		String command = "mount -F hsfs " + lofiDev +" " + mountPath; 
		return (runSingleCommandOnMwsAsRoot(command));
		
	}
	/**
	 * Greps the media in download path on MWS
	 * @param media
	 * @param isoVer
	 * @return exit value of the command
	 */
	
	public int existenceOfMedia(String media, String isoVer)
	{
		//There is no single convention for media in names in all the Data bases(like nexus and media info files, depending upon the DB naming conventions are changing as below.
		if ( media.toUpperCase().equals("OSSRC")){ media = "OSS-RC";	}
		if ( media.toUpperCase().equals("OMSAS")){ media = "Security"; }
		if ( media.toUpperCase().equals("SOLARIS")){ media = "solaris"; }
		logger.debug("Checking the Existence of " + media + " media of version " + isoVer + " in /JUMP/ISO"  );
		String command = "ls " + downloadPath + " | grep ERIC" + media + " | grep "+ isoVer + ".iso$";
		int exitCode = runSingleCommandOnMwsAsRoot(command);
		mediaFile = getLastOutput();
		context.setAttribute("mediaFile", mediaFile);
		return(exitCode);
	}
	
	/**
	 * Runs upgrade_om.bsh script to upgrde the ERICjump package.
	 * @param directory - mount directory of om media
	 * @return returns exit value of the script
	 */
	public int upgradeERICjump(String directory)
	{
		logger.info("Upgrading ERICjump package on mws");
		String command = directory + "/omtools/upgrade_om.bsh  -p " + directory + " -a mws -x ERICjump";
		return(runSingleCommandOnMwsAsRoot(command,false));
	}
	/**
	 * Runs wget command of the web page to grep the built media
	 * @param psv
	 * @param shipment
	 * @param media
	 * @return Built media 
	 */
	public String getShipmentOfPsvMedia(String psv , String shipment , String media )
	{
		logger.info("Getting the built shipment of the media");
		if ( media.toUpperCase().equals("OSSRC")){ media = "OSS-RC"; }
		if ( media.toUpperCase().equals("OMSAS")){ media = "Security"; }
		if ( media.toUpperCase().equals("SOLARIS")){ media = "Solaris"; }
		
		//String command = WGET+ " -q -O - --no-check-certificate --ignore-length  '"+ CI_LINK + "/OSS-RC/content/" + shipment + "/" + psv + "' |  grep '<td>' | nawk -F '</*td>' '{print $2}' | grep '/" + media + "' | awk -F/ '{print $3}' | head -1";
		String command = WGET+ " -q -O - --no-check-certificate --ignore-length  '"+ CI_LINK + "/OSS-RC/content/" + shipment + "/" + psv + "' |  grep '/" + media + "/show' |  nawk -F '</*td>' '{print $13}' |head -1";
        
		if (runSingleCommandOnMwsAsRoot(command) == 0){ return(getLastOutput()); }
		return(null);
	}
	/**
	 * Gets the expected path from the template files present in /ericsson/jumpstart/etc/nfs_media_config/
	 * @param media -Ex OM,OSSRC,OMSAS,COMINF
	 * @param shipment
	 * @return
	 */
	public String getMediaExpectedCachePath (String media, String shipment)
	{
		if (media.toUpperCase().equals("COMINF")){ media ="COMINF_INSTALL"; }
		if (media.toUpperCase().equals("SOLARIS")){	return(getSolCachePath(shipment));	}
		String configFile = "/ericsson/jumpstart/etc/nfs_media_config/" +media.toLowerCase();
		if (verifyPathAvailabilityOnMws(configFile) != 0){
			configFile = "/ericsson/jumpstart/template/media_config_template/" +media.toLowerCase() +"_template";
		}
		logger.info("Getting the cached media path w.r.t "+ media + " media for " + shipment);
		String mediaAreaCommand = "grep MEDIA_AREA= " + configFile + " | awk -F= '{print $2}'";
		runSingleCommandOnMwsAsRoot(mediaAreaCommand);
		String mediaArea = getLastOutput();
		if ( getLastOutput() == null || getLastOutput().isEmpty()){
			return(null);
		}
		String mediaDirCommand = "grep MEDIA_DIRECTORY= " + configFile + " | awk -F= '{print $2}'";
		runSingleCommandOnMwsAsRoot(mediaDirCommand);
		String mediaDir = getLastOutput();
		if ( getLastOutput() == null || getLastOutput().isEmpty()){
			return(null);
		}
		String command = "echo " + shipment + " | awk -F. '{print $1,$2}' | sed 's/ /\\_/g'";
		runSingleCommandOnMwsAsRoot(command);
		String track = getLastOutput();
		if ( getLastOutput() == null || getLastOutput().isEmpty()){
			return(null);
		}
		
		if ( media.toUpperCase().equals("OM") ){
			media = "OSSRC"; 
		}
		if (media.toUpperCase().equals("COMINF_INSTALL")) {
			media ="COMINF";
		}
		
		String mediaCachedPath = mediaArea + "/" + mediaDir + "/" + media + "_O" + track + "/" + shipment;
		return(mediaCachedPath);
	}
	
	/**
	 * Returns Solaris expected Cache Path
	 * @param shipment
	 * @return Returns Solaris expected Cache Path
	 */
	public String getSolCachePath(String shipment)
	{
		logger.info("Getting the cached media path w.r.t SOLARIS media for " + shipment);
		runSingleCommandOnMwsAsRoot("grep media_label /ericsson/jumpstart/release/i386/media_identity/* | awk -F: '{print $1}'", false);
		String[] solMediaInfoFileList = getLastOutput().split("\\n");
		String solMediaInfoFile = null;
		int solVer=0,solUpdate=0;
	
		for (int i=0; i<solMediaInfoFileList.length; i++) {
			runSingleCommandOnMwsAsRoot("grep media_label " + solMediaInfoFileList[i].replace("\n", "").replace("\r", "") + " | awk -F= '{print $2}' | awk -F_ '{print $3}'");
			int solVerTemp=Integer.parseInt(getLastOutput());
			if (solVerTemp > solVer ){ solVer=solVerTemp; }
			runSingleCommandOnMwsAsRoot("grep ERIC_SOL_" + solVer + " " + solMediaInfoFileList[i].replace("\n", "").replace("\r", "") + " | awk -F= '{print $2}' | awk -F_ '{print $4}' | sed 's/U//g''");
			int solUpdateTemp=0;
			if (getLastExitCode() == 0){ solUpdateTemp=Integer.parseInt(getLastOutput());}
			if (solUpdateTemp > solUpdate){
				solUpdate=solUpdateTemp;
				solMediaInfoFile= solMediaInfoFileList[i];
			}
		}
		String solMediaNum=null ,solMediaRev=null,solMediaPrefix=null;
		
		if ( runSingleCommandOnMwsAsRoot("grep media_number " + solMediaInfoFile.replace("\n", "").replace("\r", "") + " | awk -F= '{print $2}'") == 0){ solMediaNum=getLastOutput();}
	    if ( runSingleCommandOnMwsAsRoot("grep media_rev " + solMediaInfoFile.replace("\n", "").replace("\r", "") + " | awk -F= '{print $2}'") == 0){ solMediaRev=getLastOutput();}
	    if ( runSingleCommandOnMwsAsRoot("grep media_prefix " + solMediaInfoFile.replace("\n", "").replace("\r", "") + " | awk -F= '{print $2}'") == 0){solMediaPrefix=getLastOutput();}
	    if (solMediaNum.equals(null) && solMediaRev.equals(null) && solMediaPrefix.endsWith(null)){ return null; }
	    runSingleCommandOnMwsAsRoot("/ericsson/jumpstart/bin/manage_jumpstart.bsh -a list -j " + solMediaPrefix + "-"+ solMediaNum +"-"+ solMediaRev);
	    if (getLastOutput().contains("Path :")){
	    	String output=getLastOutput();
	    	return(output.substring(output.indexOf("Path :")+15,output.indexOf("Architecture :")).trim());
	    }else {
	    		runSingleCommandOnMwsAsRoot("cd /JUMP/SOL_MEDIA; ls -d *");
	    		String[] directoryList = getLastOutput().split("");
	    		int newDir=1;
	    		if ( directoryList.length != 0){
	    			for (int i=0; i<directoryList.length; i++) {
		    			if (directoryList[i].contains(Integer.toString(newDir))){ newDir++; }
		    		}
	    		}
	    		return ("/JUMP/SOL_MEDIA/" +newDir);
	    }
	}
	
	/**
	 * Gets the r-state of the cache path from the hidden file present in it
	 * @param cachePath
	 * @param media
	 * @return version of Cached ISO
	 */
	public String getVerOfCachePath (String cachePath, String media)
	{
		String configFile;
		logger.debug("Getting the version of path" + cachePath);
		if (media.toUpperCase().equals("COMINF")){ media ="COMINF_INSTALL";}
		if (media.toUpperCase().equals("SOLARIS")){
			configFile = "/ericsson/jumpstart/etc/ericjump_config";
			String mediaInfoFileCommand = "grep SOL_MEDIA_INFO_FILE= " + configFile + " | awk -F= '{print $2}' ";
			runSingleCommandOnMwsAsRoot(mediaInfoFileCommand);
			if ( getLastOutput() == null || getLastOutput().isEmpty()){
				return(null);
			}
			String mediaInfoFile = getLastOutput();
			//Now checking revision as the r-state is not present, in future this might change to media rstate.
			String revision = "grep media_rev= " + cachePath + "/" + mediaInfoFile;
			return(convertRstateToVersion("R1"+ revision + "01"));
		}
	    configFile = "/ericsson/jumpstart/etc/nfs_media_config/" +media.toLowerCase();
		if (verifyPathAvailabilityOnMws(configFile) != 0){
			configFile = "/ericsson/jumpstart/template/media_config_template/" +media.toLowerCase() +"_template";
		}
		String mediaFileCommand= null;
		if (media.toUpperCase().equals("OSSRC")){
			mediaFileCommand = "grep MEDIA_FILE= "+ configFile + " | awk -F= '{print $2}' | awk -F\\| '{print $2}'";
		}else {
			mediaFileCommand = "grep MEDIA_FILE= "+ configFile + " | awk -F= '{print $2}' ";
		}
		runSingleCommandOnMwsAsRoot(mediaFileCommand);
		if ( getLastOutput() == null || getLastOutput().isEmpty()){
			return(null);
		}
		mediaFile = getLastOutput();
		
		String mediaLabelCommand = "grep MEDIA_LABEL= " + configFile + " |  awk -F= '{print $2}'";
		runSingleCommandOnMwsAsRoot(mediaLabelCommand);
		if ( getLastOutput() == null || getLastOutput().isEmpty()){
			return(null);
		}
		String mediaLabel = getLastOutput();
		
		String rstateCommand = "grep media_rstate= " + cachePath + "/" + mediaLabel + "/"+ mediaFile + " | awk -F= '{print $2}'";
		runSingleCommandOnMwsAsRoot(rstateCommand);
		if ( getLastOutput() == null || getLastOutput().isEmpty()){
			return(null);
		}

		return (convertRstateToVersion(getLastOutput()));
	}
	/**
	 * Runs a rest call to convert r-state to a version
	 * @param rState
	 * @return version of rState
	 */
	
	public String convertRstateToVersion(String rState)
	{
		logger.debug("Converting R-state"+ rState + " to version ");
		String url = CI_LINK+"/getVersionFromRstate/?version=" + rState;
		String command = WGET+ " -q -O - --no-check-certificate --ignore-length '"+ url +"'";
		if (runSingleCommandOnMwsAsRoot(command) == 0){ return(getLastOutput()); }
		return(null);
	}
	
	

	/**
	 * Checks the r-satate of cache path and latest version of media
	 * @param cachePath
	 * @param media
	 * @param isoVer
	 * @return boolean to download media or not
	 */
	public boolean checkMediaToDownload(String cachePath, String media, String isoVer)
	{
		String alwaysCacheOssrc = (String)DataHandler.getAttribute("always_cache_ossrc"); 
    	if ( verifyPathAvailabilityOnMws(cachePath) == 0 ){
    		if (media.toUpperCase().equals("SOLARIS")){
    			logger.info("$$$*** SKIPPED Downloading of " + media + " MEDIA  as it is already cached in mws with LATEST ERICJUMP Package ***$$$" );
    			return(true);
    		}
    		String rState = getVerOfCachePath(cachePath, media);
    		if ( rState == null){
    			logger.error("Unable to get rState of Cache path " + cachePath);
    			return(false);
    		}
    		int rStateVer= convertVerToNum(rState);
    		int latestVer = convertVerToNum(isoVer);
    		if ( rStateVer == latestVer ) {
    			if (media.toUpperCase().equals("OSSRC") && alwaysCacheOssrc != null && ! alwaysCacheOssrc.isEmpty() && alwaysCacheOssrc.toLowerCase().equals("true") ){ 
                    return(false); 
    			} 
    			logger.info("$$$*** SKIPPED Downloading of " + media + " MEDIA since LATEST Verison " + isoVer + " is already cached in mws ***$$$" );
    			return (true);
    		}else if (rStateVer > latestVer){
    			logger.info("$$$*** SKIPPED Downloading of  " + media + " MEDIA since CACHED R-STATE " + rState + " is  HIGHER than  PSV  R-STATE " + isoVer + " in mws ***$$$" );
    			return (true);
    		}else {
    			logger.info("LATEST Cache path of " + media + " MEDIA exists but DOESNOT contain latest media Version " + isoVer + " it has " + rState );
    			return(false);
    		}
    	}else {
    		logger.info("LATEST Cache path of " + media  + " does not exists");
			return (false);
		}
	}      
	                    
	                     
	                    
	            
	/**
	 * Eliminates the dots from a version
	 * @param ver
	 * @return integer by eliminating dots(.)
	 */
	public int convertVerToNum(String ver)
	{
		if ( ! ver.equals("None") ){
			String command = "echo " + ver + " | sed 's/\\.//g'";
			runSingleCommandOnMwsAsRoot(command);
			return (Integer.parseInt(getLastOutput()));
		}
		return(0);
	}
	
	/**
	 * Removes the latest cache path if the r-state is lesser than the latest
	 * @param cachePath
	 * @param media
	 * @param isoVer
	 * @return 
	 */
	
	public int checkAndRemoveCachePathOnMws(String  cachePath, String media, String isoVer)
	{
		String alwaysCacheOssrc = (String)DataHandler.getAttribute("always_cache_ossrc"); 
		// If not set , setting to false to prevent exception later. 
		if (alwaysCacheOssrc == null) alwaysCacheOssrc = "false";
		
		if (media.toUpperCase().equals("SOLARIS")){ return(0); }
		if ( verifyPathAvailabilityOnMws(cachePath) == 0 ){
    		String rState = getVerOfCachePath(cachePath, media);
    		if ( rState == null){
    			logger.error("Unable to get rState of Cache path " + cachePath);
    			return(1);
    		}
			int rStateVer= convertVerToNum(rState);
			int latestVer = convertVerToNum(isoVer);
			logger.info("***  rStateVer is " +rStateVer +" ,   media id is " +media );
			
			
			// In some cases we want to force a re-cache of the OSSRC MEDIA			
			if (media.toUpperCase().equals("OSSRC") && alwaysCacheOssrc.toLowerCase().equals("true") ){
				rStateVer=100;
				logger.info("Always Cache OSSRC Media is set to TRUE");
			}
			
			if (rStateVer < latestVer){
				logger.info("Removing the Cache path " + cachePath + " from mws");
				return(removePathOnMws(cachePath));
			}
		}
		logger.debug("Nothing to remove as expected Cache path is not present");
		return(0);
	}
	/**
	 * @return boolean value
	 */
	public boolean storageCheckOnMws()
	{
		if ( makeDirOnMws(downloadPath) == 0){
			String command ="df -h " + downloadPath + " | awk '{print $4}' | tail -1 | sed 's/G//g'";
			runSingleCommandOnMwsAsRoot(command);
			try{ 
				if ( Double.parseDouble(getLastOutput()) >= 5.0 ){ return true;}
			}
			catch(Exception e){	return false; }
			return false;
		}
		return false;
	}
	
	/**
	 * Run a rest call to download the media from proxy nexus.
	 * @param shipment
	 * @param psv
	 * @param iso
	 * @return exit value of wget command
	 */
	public int downloadMediaFromNexus(String shipment, String psv, String iso)
	{
		String url = getMediaUrl(shipment, psv, iso);
		if(iso.toUpperCase().equals("SOLARIS")){ url = url.replaceAll("Solaris", "solaris");}
		logger.info("Dowloading the " + iso + " media from Nexus to "+ downloadPath + " of mws");
		String command = WGET+ " -q -P " + downloadPath + " --no-check-certificate --ignore-length " + url;
		return(runSingleCommandOnMwsAsRoot(command));
	}
	/**
	 * Greps the link from the json file which we get from a latest psv restcall
	 * @param shipment
	 * @param psv
	 * @param iso
	 * @return URL of the media    
	 */
	public String getMediaUrl( String shipment, String psv, String iso)
	{
		logger.debug("Getting Media URL ");
		if (iso.toUpperCase().equals("OSSRC")){ iso = "OSS-RC | sed 's/oss/ossrc/'";	}
		if (iso.toUpperCase().equals("OMSAS")){iso = "Security";	}
		if (iso.toUpperCase().equals("SOLARIS")){iso = "Solaris"; }
		
		String command = WGET+ " -q -O - --no-check-certificate --ignore-length  '"+ CI_LINK +"/getProductSetVersionContents/?drop=" + shipment + "&productSet=OSS-RC&version=" + psv +"&pretty=true' | grep athloneUrl | grep ERIC"+ iso +" | cut -d\\\" -f4";
		runSingleCommandOnMwsAsRoot(command);
		if (runSingleCommandOnMwsAsRoot(command) == 0){ 
			String commandOutput = getLastOutput();
			return(commandOutput.trim());
		}
		return(null);
		
	}
	/**
	 * Downloads the sha1 checksum of the respective media and gets the content
	 * @param shipment
	 * @param psv
	 * @param iso
	 * @return returns the content of sha1 checksum
	 */
	public String getMediaSha1FromNexus( String shipment, String psv, String iso)
	{
		logger.debug("Getting sha1 key from the nexus");
		String url = getMediaUrl(shipment, psv, iso) + ".sha1";
		if(iso.toUpperCase().equals("SOLARIS")){ url = url.replaceAll("Solaris", "solaris");}
		String command = WGET+ " -q -O - --no-check-certificate --ignore-length " + url;
		if (runSingleCommandOnMwsAsRoot(command) != 0){
			logger.error("Unable to download SHA1 checksum from NEXUS");
			return(""); 
		}
		return(getLastOutput());
	}
	/**
	 * Runs digest command to get the checksum of media on mws.
	 * @param mediaFile
	 * @return returns checksum generated
	 */
	
	public String getMediaSha1OnMws (String mediaFile)
	{
		logger.debug("Getting sha1 digest from the downloaded media under " + mediaFile );
		String command = "/usr/bin/digest -a sha1 " + downloadPath + mediaFile;
		if (runSingleCommandOnMwsAsRoot(command) != 0){
			logger.error("Unable to digest sha1 checksum of " + mediaFile);
			return("");  
		}
		return(getLastOutput());
	}
	/**
	 * Checks the media already exists on Downloadpath given
	 * @param media
	 * @param isoVer
	 * @param shipment
	 * @param psv
	 * @return returns boolean wheather to download media or not
	 */
	
	public boolean checkMediaAlreadyExists (String media, String isoVer, String shipment, String psv)
	{
		logger.info("Checking Latest " + media + " Media existance in download path before Downloading ");
		if ( existenceOfMedia(media, isoVer) == 0 ){
			logger.info("Found Latest " + media + " Media under download path");
			mediaFile = context.getAttribute("mediaFile");
			logger.info("Checking checksum of existing media");
			if (getMediaSha1FromNexus(shipment, psv, media).equals(getMediaSha1OnMws(mediaFile ))) {
				logger.info("SKIPPING DOWNLOAD of " + media + " MEDIA as it is Already Dowloaded in download path " + downloadPath);
				return (true);
			}else{
				logger.info("Removing the " + media + " media as checksum didnot match");
				removePathOnMws(downloadPath + mediaFile);
				return(false);
			}
		}
		logger.info("Media does not exists in download path " + downloadPath);
		return (false);
	}
	/**
	 * Gets the ERICjump version on latest mount path
	 * @param directory
	 * @return version of ERICjump
	 */
	public String getERICjumpVer(String directory){
		logger.info("Getting Version of ERICjump present in " + directory + "path");
		String command = "pkginfo -ld " + directory + "/omtools/eric_jumpstart/ERICjump.pkg | grep VERSION | awk '{print $2}'";
		if (runSingleCommandOnMwsAsRoot(command) == 0){ return(getLastOutput()); }
		return(null);
	}
	
	/**
	 * Gets the ERICjump version on mws
	 * @return
	 */
	
	public String getERICjumpVerOnMws(){
		logger.info("Getting version of deployed ERICjump pkg on MWS");
		return(getPkgInfoCmdVersion("ERICjump"));
	}
	/**
	 * Runs manage_nfs_media.bsh script to cache the media
	 * @param media
	 * @param mountPath
	 * @return returns exit code of the script
	 */
	public int cacheMedia(String media, String mountPath){
		logger.info("Caching " + media + " media on MWs");
		if (media.toUpperCase().equals("SOLARIS")){
			User rootUser = getMsHost().getUsers(UserType.ADMIN).get(0);
			copySysIdFileToMWS(getMsHost(), rootUser);
			String command="/ericsson/jumpstart/bin/manage_jumpstart.bsh -a add -p " + mountPath +" -N -s /tmp/sysid.cfg";
			return(runSingleCommandOnMwsAsRoot(command,false));	
		}
		if (media.toUpperCase().equals("COMINF")){
			media ="COMINF_INSTALL";
		}	
		String command = "/ericsson/jumpstart/bin/manage_nfs_media.bsh -a add -m " + media.toLowerCase() + " -N -p " + mountPath;
		return(runSingleCommandOnMwsAsRoot(command,false));
	}
	/**
	 * 
	 * @param host
	 * @param user
	 * @param engTarBall
	 * @return
	 */
    public boolean copySysIdFileToMWS (final Host host, final User user) {
    	return sendFileRemotely(host, user, "sysid.cfg", "/tmp/");
    }
	/**
	 * Runs umount command to the given path 
	 * @param mountPath
	 * @return exit code of the command
	 */
	public int umountMedia(String mountPath)
	{
		logger.info("Unmounting the path "+ mountPath + " on Mws");
		runSingleCommandOnMwsAsRoot("umount " + mountPath);
		int umountExitCode = getLastExitCode();
		if (getLastExitCode() != 0){
			runSingleCommandOnMwsAsRoot("fuser -c " + mountPath);
			logger.warn("SKIPPING umount of " + mountPath + " :fuser command output is " + getLastOutput());
		}
		return(umountExitCode);
	}
	/**
	 * Deletes the lofi device on MWS
	 * @param lofiDevice
	 * @return exit value of the command
	 */
	public int deleteLofiDevice(String lofiDevice)
	{
		logger.info("Removing lofi device " + lofiDevice + " from MWS");
		return(runSingleCommandOnMwsAsRoot("lofiadm -d " + lofiDevice));
	}
	
	public int removeDownloadPath()
	{
		logger.info("Removing downloaded Path from MWS " );
		return(runSingleCommandOnMwsAsRoot("rm -rf " + downloadPath ));
	}
}
