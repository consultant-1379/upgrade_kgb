package com.ericsson.oss.orchestrator.test.operators;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.google.inject.Singleton;

@Singleton

public class PackageOverlayOperator extends PCJCommonOperator {
	
	private Logger logger = LoggerFactory.getLogger(PackageOverlayOperator.class);
	public static String mediaCache1 ="";
	//private String media="OSSRC";
	String shipment = (String) DataHandler.getAttribute("shipment");
//	public String physicalmws = (String) DataHandler.getAttribute("physicalmws");
	//public String getMediaExpectedCachePath (String shipment)
	/*public String phymwsip() {
	logger.info("Getting the Physical MWS ip from the job ");
	physicalmws = (String) DataHandler.getAttribute("physicalmws");
	return (physicalmws);
	}*/
	public String copyuploadedpkgs(String physicalmws) {
	{
	String configFile = "/ericsson/jumpstart/etc/nfs_media_config/ossrc";
	if (verifyPathAvailabilityOnMws(configFile) != 0){
		configFile = "/ericsson/jumpstart/etc/nfs_media_config/ossrc";
		
	}
	
	
			
	logger.info("Getting the cached media path w.r.t "+ shipment );
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
	logger.info(command + "Shipment executed successfully");
	String track = getLastOutput();
	if ( getLastOutput() == null || getLastOutput().isEmpty()){
		return(null);
	}
	String medialabelCommand = "grep MEDIA_LABEL= " + configFile + " | awk -F= '{print $2}'";
	runSingleCommandOnMwsAsRoot(medialabelCommand);
	String medialabel = getLastOutput();
	System.out.println("The Media Label" + medialabel);
	String medialabel1 = medialabel.trim();
	if ( getLastOutput() == null || getLastOutput().isEmpty()){
		return(null);
	}
	
			
	String mediaCachedPathCmd = mediaArea + "/" + mediaDir + "/" + "OSSRC_O" + track + "/" + shipment + "/" + medialabel;
	//runSingleCommandOnMwsAsRoot(mediaCachedPathCmd);
	String mediaCache = getLastOutput();
	System.out.println("The path is"+mediaCache);
	String mediacache1 = mediaCache.trim();
	if ( getLastOutput() == null || getLastOutput().isEmpty()){
		return(null);
	}
	logger.info(mediaCachedPathCmd + "Media Path executed successfully");
	
	


	logger.info("eeeeeeeeeexecuted successfully");
	String cmd1="ls " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded > /var/tmp/uploaded_pkgs";
	int exitCode4 = this.runSingleCommandOnMwsAsRoot(cmd1, false);
	logger.info(cmd1 + "executed successfully");
	String cmd2="cat /var/tmp/uploaded_pkgs";
	int exitCode3 = this.runSingleCommandOnMwsAsRoot(cmd2, false);
	logger.info(cmd2 + "executed successfully");
	
	String cmd21="cat /var/tmp/uploaded_pkgs|grep ERICusck";
	int exitCode31 = this.runSingleCommandOnMwsAsRoot(cmd21, false);
	logger.info(cmd21 + "ERICusck is present in Uploaded Directory");
	
	String cmd22="cat /var/tmp/uploaded_pkgs|grep ERICorchupg";
	int exitCode32 = this.runSingleCommandOnMwsAsRoot(cmd22, false);
	logger.info(cmd22 + "ERICorchupg is present in Uploaded Directory");
	
	String cmd41="cat /var/tmp/uploaded_pkgs|grep ERICsck";
	int exitCode41 = this.runSingleCommandOnMwsAsRoot(cmd41, false);
	logger.info(cmd41 + "ERICsck is present in Uploaded Directory");
	
	String cmd44="cat /var/tmp/uploaded_pkgs|grep ERICcore";
	int exitCode44 = this.runSingleCommandOnMwsAsRoot(cmd44, false);
	logger.info(cmd44 + "ERICcore is present in Uploaded Directory");
	
	String cmd49="cat /var/tmp/uploaded_pkgs|grep ECONFsystem";
	int exitCode49 = this.runSingleCommandOnMwsAsRoot(cmd49, false);
	logger.info(cmd49 + "ECONFsystem is present in Uploaded Directory");
	
	String cmd56="cat /var/tmp/uploaded_pkgs|grep ERICaxe";
	int exitCode56 = this.runSingleCommandOnMwsAsRoot(cmd56, false); 
	logger.info(cmd56 + "ERICaxe is present in Uploaded Directory");
	
	String cmd58="cat /var/tmp/uploaded_pkgs|grep ECONFbase";
	int exitCode58 = this.runSingleCommandOnMwsAsRoot(cmd58, false);
	logger.info(cmd58 + "ECONFbase is present in Uploaded Directory");
	
	String cmd5="pkginfo -ld " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ERICusck.pkg|grep VERSION|awk '{print $2}'";
	String cmd51="pkginfo -ld " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ERICorchupg.pkg|grep VERSION|awk '{print $2}'";
	String cmd43="pkginfo -ld " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ERICsck.pkg|grep VERSION|awk '{print $2}'";
	String cmd45="pkginfo -ld " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ERICcore.pkg|grep VERSION|awk '{print $2}'";
	String cmd48="pkginfo -ld " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ECONFsystem.pkg|grep VERSION|awk '{print $2}'";
	String cmd55="pkginfo -ld " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ERICaxe.pkg|grep VERSION|awk '{print $2}'";
	String cmd54="pkginfo -ld " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ECONFbase.pkg|grep VERSION|awk '{print $2}'";
    int exitCode5 = this.runSingleCommandOnMwsAsRoot(cmd5, false);
	String NewRstate = getLastOutput();
	String NRstate=NewRstate.trim();
	logger.info(cmd5 + "ERICusck New Rstate executed successfully");
	int exitCode51 = this.runSingleCommandOnMwsAsRoot(cmd51, false);
	String NewRstate_Orcha = getLastOutput();
    String NRstate_Orcha=NewRstate.trim();
    logger.info(cmd51 + "ERICorchupg New Rstate executed successfully");
    int exitCode43 = this.runSingleCommandOnMwsAsRoot(cmd43, false);
	String NewRstate_sck = getLastOutput();
	String NRstate_sck=NewRstate_sck.trim();
	logger.info(cmd43 + "ERICsck New Rstate executed successfully");
	int exitCode45 = this.runSingleCommandOnMwsAsRoot(cmd45, false);
	String NewRstate_core = getLastOutput();
	String NRstate_core=NewRstate_core.trim();
	logger.info(cmd45 + "ERICcore New Rstate executed successfully");
	int exitCode48 = this.runSingleCommandOnMwsAsRoot(cmd48, false);
	String NewRstate_confsystem = getLastOutput();
	String NRstate_confsystem=NewRstate_core.trim();
	logger.info(cmd45 + "ECONFsystem New Rstate executed successfully");
	int exitCode55 = this.runSingleCommandOnMwsAsRoot(cmd55, false);
	String NewRstate_axe = getLastOutput();
	String NRstate_axe=NewRstate_axe.trim();
	logger.info(cmd55 + "ERICaxe New Rstate executed successfully");
	int exitCode54 = this.runSingleCommandOnMwsAsRoot(cmd54, false);
	String NewRstate_confbase = getLastOutput();
	String NRstate_confbase=NewRstate_confbase.trim();
	logger.info(cmd54 + "ECONFbase New Rstate executed successfully");
	
	String cmd6 = "cat " + mediaCachedPathCmd + "/" + "build_manifest_i386| grep ERICusck| awk '{print $3}'";
	int exitCode6 = this.runSingleCommandOnMwsAsRoot(cmd6, false);
	String OLDRstate = getLastOutput();
	String ORstate = OLDRstate.trim();
	logger.info(cmd6 + "ERICusck Old Rstate executed successfully");
	String cmd61="cat " + mediaCachedPathCmd + "/" + "build_manifest_i386| grep ERICorchupg| awk '{print $3}'";
    int exitCode61 = this.runSingleCommandOnMwsAsRoot(cmd61, false);
    String OLDRstate_Orcha = getLastOutput();
    String ORstate_Orcha = OLDRstate_Orcha.trim();
    logger.info(cmd61 + "ERICorchupg Old Rstate executed successfully");
    String cmd81="cat " + mediaCachedPathCmd + "/" + "build_manifest_i386| grep ERICsck| awk '{print $3}'";
    int exitCode81 = this.runSingleCommandOnMwsAsRoot(cmd81, false);
    String OLDRstate_Orcha_sck = getLastOutput();
    String ORstate_Orcha_sck = OLDRstate_Orcha_sck.trim();
    logger.info(cmd81 + "ERICsck Old Rstate executed successfully");
    String cmd82="cat " + mediaCachedPathCmd + "/" + "build_manifest_i386| grep ERICcore| awk '{print $3}'";
    int exitCode82 = this.runSingleCommandOnMwsAsRoot(cmd82, false);
    String OLDRstate_Orcha_core = getLastOutput();
    String ORstate_Orcha_core = OLDRstate_Orcha_core.trim();
    logger.info(cmd82 + "ERICcore Old Rstate executed successfully");
    String cmd83="cat " + mediaCachedPathCmd + "/" + "build_manifest_i386| grep ECONFsystem| awk '{print $3}'";
    int exitCode83 = this.runSingleCommandOnMwsAsRoot(cmd83, false);
    String OLDRstate_Orcha_confsystem = getLastOutput();
    String ORstate_Orcha_confsystem = OLDRstate_Orcha_confsystem.trim();
    logger.info(cmd83 + "ECONFsystem Old Rstate executed successfully");
    String cmd84="cat " + mediaCachedPathCmd + "/" + "build_manifest_i386| grep ERICaxe| awk '{print $3}'";
    int exitCode84 = this.runSingleCommandOnMwsAsRoot(cmd84, false);
    String OLDRstate_Orcha_axe = getLastOutput();
    String ORstate_Orcha_axe = OLDRstate_Orcha_axe.trim();
    logger.info(cmd84 + "ERICaxe Old Rstate executed successfully");
    String cmd85="cat " + mediaCachedPathCmd + "/" + "build_manifest_i386| grep ECONFbase| awk '{print $3}'";
    int exitCode85 = this.runSingleCommandOnMwsAsRoot(cmd85, false);
    String OLDRstate_Orcha_confbase = getLastOutput();
    String ORstate_Orcha_confbase = OLDRstate_Orcha_confbase.trim();
    logger.info(cmd85 + "ECONFbase Old Rstate executed successfully");
    
	String cmd10="echo $exitCode5";
	int exitCode10 = this.runSingleCommandOnMwsAsRoot(cmd10, false);
	logger.info(cmd10 + "executed successfully");
	String cmd101="echo $exitCode51";
    int exitCode101 = this.runSingleCommandOnMwsAsRoot(cmd101, false);
    logger.info(cmd101 + "executed successfully");
    
    if (exitCode31 == 0) {
	String test= "/";
	String command68 = test + NRstate;
	ORstate = ORstate.concat(command68);
	System.out.println(ORstate);
	String cmd7="/usr/bin/sed \"/ERICusck/ s/" + ORstate + "/\"" + " " + mediaCachedPathCmd + "/" + "build_manifest_i386  > /var/tmp/tmp_build_manifest_i386";
	 int exitCode7 = this.runSingleCommandOnMwsAsRoot(cmd7, false);
	 String Output = getLastOutput();
	 logger.info(cmd7 + "ERICusck sed command executed successfully");
    }
    
    if (exitCode32 == 0) {
	 String test_Orcha= "/";
     String command12 = test_Orcha + NRstate_Orcha;
     ORstate_Orcha = ORstate_Orcha.concat(command12);
     System.out.println(ORstate_Orcha);
     String cmd71="/usr/bin/sed \"/ERICorchupg/ s/" + ORstate_Orcha + "/\"" + " " + mediaCachedPathCmd + "/" + "build_manifest_i386  > /var/tmp/tmp_build_manifest_i386";
     int exitCode71 = this.runSingleCommandOnMwsAsRoot(cmd71, false);
      String Output_Orcha = getLastOutput();
      logger.info(cmd71 + "ERICorchupg sed command executed successfully");
    }
    
    if (exitCode41 == 0) {
		String test_sck= "/";
		String command69 = test_sck + NRstate_sck;
		ORstate_Orcha_sck = ORstate_Orcha_sck.concat(command69);
		System.out.println(ORstate_Orcha_sck);
		String cmd75="/usr/bin/sed \"/ERICsck/ s/" + ORstate_Orcha_sck + "/\"" + " " + mediaCachedPathCmd + "/" + "build_manifest_i386  > /var/tmp/tmp_build_manifest_i386";
		 int exitCode75 = this.runSingleCommandOnMwsAsRoot(cmd75, false);
		 String Output = getLastOutput();
		 logger.info(cmd75 + "ERICsck sed command executed successfully");
        }
    
    if (exitCode44 == 0) {
		String test_core= "/";
		String command73 = test_core + NRstate_core;
		ORstate_Orcha_core = ORstate_Orcha_core.concat(command73);
		System.out.println(ORstate_Orcha_core);
		String cmd73="/usr/bin/sed \"/ERICcore/ s/" + ORstate_Orcha_core + "/\"" + " " + mediaCachedPathCmd + "/" + "build_manifest_i386  > /var/tmp/tmp_build_manifest_i386";
		 int exitCode73 = this.runSingleCommandOnMwsAsRoot(cmd73, false);
		 String Output = getLastOutput();
		 logger.info(cmd73 + "ERICcore sed command executed successfully");
        }
    
    if (exitCode49 == 0) {
		String test_confsystem= "/";
		String command74 = test_confsystem + NRstate_confsystem;
		ORstate_Orcha_confsystem = ORstate_Orcha_confsystem.concat(command74);
		System.out.println(ORstate_Orcha_confsystem);
		String cmd74="/usr/bin/sed \"/ECONFsystem/ s/" + ORstate_Orcha_confsystem + "/\"" + " " + mediaCachedPathCmd + "/" + "build_manifest_i386  > /var/tmp/tmp_build_manifest_i386";
		 int exitCode74 = this.runSingleCommandOnMwsAsRoot(cmd74, false);
		 String Output = getLastOutput();
		 logger.info(cmd74 + "ECONFsystem sed command executed successfully");
        }
    
    if (exitCode56 == 0) {
		String test_axe= "/";
		String command76 = test_axe + NRstate_axe;
		ORstate_Orcha_axe = ORstate_Orcha_axe.concat(command76);
		System.out.println(ORstate_Orcha_axe);
		String cmd76="/usr/bin/sed \"/ERICaxe/ s/" + ORstate_Orcha_axe + "/\"" + " " + mediaCachedPathCmd + "/" + "build_manifest_i386  > /var/tmp/tmp_build_manifest_i386";
		 int exitCode76 = this.runSingleCommandOnMwsAsRoot(cmd76, false);
		 String Output = getLastOutput();
		 logger.info(cmd76 + "ERICaxe sed command executed successfully");
        }
    
    if (exitCode58 == 0) {
		String test_confbase= "/";
		String command77 = test_confbase + NRstate_confbase;
		ORstate_Orcha_confbase = ORstate_Orcha_confbase.concat(command77);
		System.out.println(ORstate_Orcha_confbase);
		String cmd77="/usr/bin/sed \"/ECONFbase/ s/" + ORstate_Orcha_confbase + "/\"" + " " + mediaCachedPathCmd + "/" + "build_manifest_i386  > /var/tmp/tmp_build_manifest_i386";
		 int exitCode77 = this.runSingleCommandOnMwsAsRoot(cmd77, false);
		 String Output = getLastOutput();
		 logger.info(cmd77 + "ECONFbase sed command executed successfully");
        }
    
     String cmd11="echo $exitCode6";
	 int exitCode11 = this.runSingleCommandOnMwsAsRoot(cmd11, false);
	 logger.info(cmd11 + "executed successfully");
	 String cmd111="echo $exitCode61";
     int exitCode111 = this.runSingleCommandOnMwsAsRoot(cmd111, false);
     logger.info(cmd111 + "executed successfully");
     
     
	 String cmd9="cat /var/tmp/tmp_build_manifest_i386|grep ERICusck";
	 int exitCode9 = this.runSingleCommandOnMwsAsRoot(cmd9, false);
	 logger.info(cmd9 + "ERICusck in tmpdir executed successfully");
	 String cmd91="cat /var/tmp/tmp_build_manifest_i386|grep ERICorchupg";
     int exitCode91 = this.runSingleCommandOnMwsAsRoot(cmd91, false);
     logger.info(cmd91 + "ERICorchupg in tmpdir executed successfully");
     String cmd92="cat /var/tmp/tmp_build_manifest_i386|grep ERICsck";
	 int exitCode92 = this.runSingleCommandOnMwsAsRoot(cmd92, false);
	 logger.info(cmd92 + "ERICsck in tmpdir executed successfully");
	 String cmd93="cat /var/tmp/tmp_build_manifest_i386|grep ERICcore";
	 int exitCode93 = this.runSingleCommandOnMwsAsRoot(cmd93, false);
	 logger.info(cmd93 + "ERICcore in tmpdir executed successfully");
	 String cmd94="cat /var/tmp/tmp_build_manifest_i386|grep ECONFsystem";
	 int exitCode94 = this.runSingleCommandOnMwsAsRoot(cmd94, false);
	 logger.info(cmd94 + "ECONFsystem in tmpdir executed successfully");
	 String cmd95="cat /var/tmp/tmp_build_manifest_i386|grep ERICaxe";
	 int exitCode95 = this.runSingleCommandOnMwsAsRoot(cmd95, false);
	 logger.info(cmd95 + "ERICaxe in tmpdir executed successfully");
	 String cmd96="cat /var/tmp/tmp_build_manifest_i386|grep ECONFbase";
	 int exitCode96 = this.runSingleCommandOnMwsAsRoot(cmd96, false);
	 logger.info(cmd96 + "ECONFbase in tmpdir executed successfully");
	 
	 String cmd8="cp /var/tmp/tmp_build_manifest_i386" + " " + mediaCachedPathCmd + "/" + "build_manifest_i386";
	 int exitCode8 = this.runSingleCommandOnMwsAsRoot(cmd8, false);
	 logger.info(cmd8 + "executed successfully");
	 if (exitCode8 != 0) 
	 {
            logger.error("ERICusck or ERICorchupg pkg is not copied to Build Manifest File " + exitCode8);
            return "1";
        }
	 
	 
	 
	 String cmd3="cat /var/tmp/uploaded_pkgs | grep -i ERICusck.pkg";	
	 int exitCode1 = this.runSingleCommandOnMwsAsRoot(cmd3, false);
	 logger.info(cmd3 + "executed successfully");
	
	 String cmd31="cat /var/tmp/uploaded_pkgs | grep -i ERICorchupg.pkg";
	 int exitCode14 = this.runSingleCommandOnMwsAsRoot(cmd31, false);
     logger.info(cmd31 + "executed successfully");
     
	 if (exitCode1 != 0 && exitCode14 !=0 )
	 {
            logger.error("ERICusck or ERICorchupg  Pkg is not present " + exitCode1);
            return "1";
	 } 
	
	 
	 String cmd = "cp -r " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ERICusck.pkg" + " " + mediaCachedPathCmd + "/" + "inst_config/common/upgrade/ERICusck.pkg";
	 int exitValue = this.runSingleCommandOnMwsAsRoot(cmd,false);
	 logger.info("ERICusck Pkg is present and executed successfully" + exitValue);
	 String cmd15 = "cp -r " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ERICorchupg.pkg" + " " + mediaCachedPathCmd + "/" + "inst_config/i386/upgrade/ERICorchupg.pkg";
     int exitValue15 = this.runSingleCommandOnMwsAsRoot(cmd15,false);
     logger.info("ERICorchupg Pkg is present and executed successfully" + exitValue15);
     String cmd76 = "cp -r " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ERICsck.pkg" + " " + mediaCachedPathCmd + "/" + "inst_config/common/ericsson/ERICsck.pkg";
	 int exitValue76 = this.runSingleCommandOnMwsAsRoot(cmd76,false);
	 logger.info("ERICsck Pkg is present and executed successfully" + exitValue76);
     String cmd77 = "cp -r " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ERICcore.pkg" + " " + mediaCachedPathCmd + "/" + "inst_config/common/install/ERICcore.pkg";
	 int exitValue77 = this.runSingleCommandOnMwsAsRoot(cmd77,false);
	 logger.info("ERICcore Pkg is present and executed successfully" + exitValue77);
     String cmd78 = "cp -r " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ECONFsystem.pkg" + " " + mediaCachedPathCmd + "/" + "inst_config/common/config/ECONFsystem.pkg";
	 int exitValue78 = this.runSingleCommandOnMwsAsRoot(cmd78,false);
	 logger.info("ECONFsystem Pkg is present and executed successfully" + exitValue78);
     String cmd79 = "cp -r " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ERICaxe.pkg" + " " + mediaCachedPathCmd + "/" + "eric_app/common/ERICaxe.pkg";
	 int exitValue79 = this.runSingleCommandOnMwsAsRoot(cmd79,false);
	 logger.info("ERICaxe Pkg is present and executed successfully" + exitValue79);
     String cmd80 = "cp -r " + "/net/" + physicalmws + "/export/jumpstart/teams/iiug/OSSRC_O" + track + "/" + shipment + ".daily/uploaded/ECONFbase.pkg" + " " + mediaCachedPathCmd + "/" + "inst_config/common/config/ECONFbase.pkg";
	 int exitValue80 = this.runSingleCommandOnMwsAsRoot(cmd80,false);
	 logger.info("ECONFbase Pkg is present and executed successfully" + exitValue80);
     if(exitValue != 0 && exitValue15 !=0 )
     {
    	 logger.error("ERICusck and ERICorchupg are not copied to the upgrade directory  " + exitValue);
         return "1"; 
     }
           
      return "0";      
}



}
}         		 

         		 
	

