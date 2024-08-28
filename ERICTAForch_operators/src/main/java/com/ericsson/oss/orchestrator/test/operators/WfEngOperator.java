package com.ericsson.oss.orchestrator.test.operators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.google.inject.Singleton;

/** Operator for doing commands to install Workflow Engine **/
@Operator(context = Context.CLI)
@Singleton
public class WfEngOperator extends PCJCommonOperator {    

    public final static String INI_TEMPLATE = "wfeng.oss_ini_template";
    private final static String INSTALL_LOC = "/tmp/orchengine_install";
    private final static String WFENG_BASE = "/ericsson/orchestrator";
    private final static String ORCHPKG = "ERICorch";

    
    // Attributes used by test
    private final static String DEPOT_DIR = "testware.orch_upgrade_dir";
    private final static String MWS_BKIP = "testware.mws_bkip";
    private final static String MASHOST = "testware.mashost";
    
    //Statuses that indicate success
    // Include skipped as need this for when want to skip servers
    private final static String successStatuses[] = { "SUCCESS", "PARAM_NOTMATCH", "SKIPPED", "REACHED_VERSION" };
    
    // Data source name
    public final static String ENGINE_STATUS_DATA_SOURCE = "engine_parsed_status_data";
    // Records within data source
    public final static String TASK_ID = "taskid";
    public final static String STATUS = "status";
    public final static String DURATION = "duration";
    public final static String ESTIMATED_DUR = "estimated";
    public final static String ERROR_MSGS = "error_msgs";

    private Logger logger = LoggerFactory.getLogger(WfEngOperator.class);
    
    /**
     * Untars workflow engine tar ball, assumes already called initializeHelper
     * @param engTarBall
     * @return
     */
    public int unTarWfEngTar(final String engTarBall) {         
        if (isShellClosed()) {
            logger.error("Attempt to untar when shell not opened via initializeShell");
            return 255;
        }
        execute("cd " + INSTALL_LOC);
        execute("tar xf " + engTarBall);
        return getCommandExitValue();

    }
    
    /**
     * Installs workflow engine, assumes already called initializeHelper
     * @return
     */
    public int installWfEngTar(String eng_cmd) {
        if (isShellClosed()) {
            logger.error("Attempt to install when shell not opened via initializeShell");
            return 255;
        }
        execute("cd " + INSTALL_LOC);
        execute("AWE_HOME=" + getWfEngBase());
        execute("export AWE_HOME");
        execute("AWE_USER=orcha");
        execute("export AWE_USER");
        execute("./"+eng_cmd);
        return getCommandExitValue();
    }
    
    /**
     * Returns the location of where the workflow engine is installed
     * @return Base location of workflow engine
     */
    public String getWfEngBase() {
        return WFENG_BASE;
    }
    
    /**
     * Returns the workflow engine's config directory
     * @return Base location of workflow engine cfg dir
     */
    public String getWfEngCfg() {
        return getWfEngBase() + "/cfg";
    }
    
    /**
     * Returns the workflow engine's etc directory
     * @return Base location of workflow engine etc dir
     */
    public String getWfEngEtc() {
        return getWfEngBase() + "/etc";
    }
    
    /**
     * Returns the workflow engine's log directory
     * @return Base location of workflow engine log dir
     */
    public String getWfEngLog() {
        return getWfEngBase() + "/log";
    }
    
    /**
     * Executes workflow engine with specified arguments. Assumes already called
     * initialize. Use readUntilClosed to get output, and getShellExitValue to get return
     * code
     * @param args
     * @return
     */
    public Shell executeWfEng(final String args) {
        String cmdLine = "python " + getWfEngBase() + "/lib/workfloweng.py " + args;
        logger.info("Invoking workflow engine with cmd: " + cmdLine, true);
        return executeCommand(cmdLine);
    }
    
    /**
     * Return name of default hosts file
     * @return
     */
    public String getDefaultHostsFile() {
        String hostfile = getWfEngEtc() + "/hosts_" + getAdminHost1() + ".xml";
        return hostfile;
    }
    
    /**
     * Get log prefix of log for this workflow file with default hosts
     * @param workFile
     * @return
     */
    public String getDefaultLogPrefix(String workFile) {
        
        String logFile = workFile.substring(workFile.lastIndexOf("/") + 1, workFile.lastIndexOf("."));
        logFile = logFile + "_hosts_" + getAdminHost1() + "_";
        return logFile;
    }

    /**
     * Returns the command to run the orchestrator engine with specified args
     * @param args
     * @return
     */
    public String getWfEngCmd(final String args) {
        String cmdLine = "python " + getWfEngBase() + "/lib/workfloweng.py " + args;
        return cmdLine;
    }
    
    /**
     * Copies the workflow engine tar to specified host
     * @param host
     * @param user
     * @param engTarBall
     * @return
     */
    public boolean copyWfEngTar(final Host host, final User user, 
                                final String engTarBall) {
        return sendFileRemotely(host, user, engTarBall, INSTALL_LOC);
    }
    
    /**
     * Copies a local file to the workflow engine config directory
     * @param fileToCopy
     * @return true if successfully copied
     */    
    public boolean copyWfEngCfgFileToMWS(String fileToCopy) {
        // Copy file to workflow engine's config directory
        User orchaUser = getMsHost().getUsers(UserType.OPER).get(0);
        String remoteFileLocation = getWfEngCfg();    
        return sendFileRemotely(getMsHost(), orchaUser, fileToCopy, remoteFileLocation);
    }
    
    /**
     * Copies a local file to the workflow engine etc directory
     * @param fileToCopy
     * @return true if successfully copied
     */    
    public boolean copyWfEngEtcFileToMWS(String fileToCopy) {
        // Copy file to workflow engine's config directory
        User orchaUser = getMsHost().getUsers(UserType.OPER).get(0);
        String remoteFileLocation = getWfEngEtc();    
        return sendFileRemotely(getMsHost(), orchaUser, fileToCopy, remoteFileLocation);
    }
    

    /**
     * Returns the command to create a temporary installation directory
     * @return the command to create the temporary install dir
     */
    public String getCreateInstallDirCommand() {
        return "rm -rf " + INSTALL_LOC + "; mkdir -p " + INSTALL_LOC;
    }
    
    /**
     * Returns the command to remove the temporary installation directory
     * @return the command to remove the temporary install dir
     */
    public String getRemoveInstallDirCmd() {
        return ("rm -rf " + INSTALL_LOC);
    }
    
    /**
     * Returns a list of workflow filenames located in the testware area
     * @return
     */
    public List<String> getWorkflowFilenames() {
        List<String> workFiles = new ArrayList<String>();
        List<String> files = FileFinder.findFile(".xml");
        logger.debug("Get workflow found: " + files.size());
        for (String file : files) {
            if (file.contains("/orchengine/workflow/")) {
                logger.debug("Adding " + file);
                workFiles.add(file);
            }
        }
        return workFiles;
    }
    
    /**
     * Adds strings to end of specified file (if they are not already present)
     * @param host   remote host
     * @param user   remote user
     * @param filename  file to append to
     * @param toAdd  array of strings to add
     * @return 0 if successful, else errorcode
     */
    public int addToFile(final Host host, final User user, 
                         final String filename, final String toAdd[]) {
        initializeHelper(host, user);
        int retVal = 0;
        for (String line : toAdd) {
            execute("grep \"" + line + "\" " + filename);
            int grepVal = getCommandExitValue();
            if (grepVal == 0) {
                logger.info("No need to append line as already present in " + filename);
            } else {
                logger.info("Appending \"" + line + "\" to " + filename);
                execute("echo " + line + " >> " + filename);
                retVal = this.getCommandExitValue();
                if (retVal !=0 ) {
                    return retVal;
                }
            }
        }
        return retVal;
    }    
    
    /**
     * Gets name of host file. Assumes analyseAdminHosts already called
     * @return The name of the hosts file for this admin server
     */
    public String getHostsFile() {
        String localFile = "hosts_" + getAdminHost1() + ".xml";
        return localFile;
    }
    
    /**
     * Creates the hosts xml file. Assumes analyseAdminHosts already called
     * @return boolean - whether successful
     */
    public boolean createHostsFile() {
        // create Hosts file
        String localFile = getHostsFile();
        File lFile = new File(localFile);
        if (lFile.exists()) {
            logger.warn("Deleting old file " + localFile, true);
            lFile.delete();
        }
        try {
             PrintWriter writer = new PrintWriter(localFile);
             writer.println("<hosts>");
             List<Host> hosts = DataHandler.getHosts();
             for (Host hostentry : hosts) {
                 // Write an entry for each host interested 
                 if (hostsInterested.contains(hostentry.getType())) {
                     writeHostLine(hostentry, writer);
                 }
             }
             writer.println("</hosts>");
             writer.close();
        } catch (FileNotFoundException e) {
            logger.error("Failed to create hosts file: " + e.getMessage(), true);
            return false;
        }
        return true;
    }
    
    /**
     * Returns the location of the Media Location. For CDB then this should come from the attribute that is set by a preceding test case.
     * To allow this test to run by itself or be part of the upgrade flow done by orchestrator team then it can also come from a property set on the 
     * command line
     * @param configuration
     * @return
     */
    public String getMediaLocation(final TafConfiguration configuration, final String media) {
        String cachePath = (String)DataHandler.getAttribute(media + CACHE_SUFFIX);
        if (cachePath == null || cachePath.length() == 0) {
            cachePath = (String) configuration.getProperty("testware.orch_" + media + "_media");
        } else if (media.equals("om")) {
            // For om we need trailing /om at end of media
            cachePath = cachePath + "/" + media;
        }
        logger.debug("Will use " + media + "cachePath of:" + cachePath);
        return cachePath;
    }
    
    /**
     * Gets the location of the depot dir. If the CreateDepotScenario has run then we generate a reference, else it comes in as a parameter through Jenkins
     * @param configuration
     * @return
     */
    public String getDepotDir(final TafConfiguration configuration) {
        // If depot directory specified in Maven then use it, else use the value set by
        // CreateDepotScenario.
        // This order allows for the fact that for the engine to use the depot created by
        // CreateDepoScenario then the Liveupgrade has to be downloaded to it. So in a suite
        // with just CreateDepot and no download of Liveupgrade we will want to point to
        // a different location
        // then get from DEPOT_DIR property passed through maven
        String upgradeDir = (String) configuration.getProperty(DEPOT_DIR);
        if (upgradeDir == null) {
            upgradeDir = (String)DataHandler.getAttribute(ORCHA_DEPOT);
        }
        logger.debug("Will use depot dir of:" + upgradeDir);
        return upgradeDir;
    }
    
    /**
     * Upgrades engine to version at install Path
     * @param installPath
     * @return Whether install was successful
     */   
    public boolean upgradeEngine(String installPath) {
        boolean removeEngine = false;
        if (isPkgInstalled(ORCHPKG)) {
            String installed = getPkgInfoCmdVersion(ORCHPKG);
            String latest = getPkgVersion(installPath + "/" + ORCHPKG  + ".pkg");
            if (installed.equals(latest)) {
                logger.info("Engine and latest version of orchestrator engine patch match, no install necessary");
                return true;
            } else {
                logger.info("Engine needs to be installed as version mismatch, installed: " + installed + ", latest:" + latest);
                removeEngine = true;
            }
        } else {
            logger.info("Install engine as no version installed");
            if (!removeOldCode()) {
                logger.warn("Failed to remove old wfeng code, will continue...");
            }
        }
        if (removeEngine) {
            boolean removed = removeEngine();
            if (!removed) {
                logger.info("Failed to remove engine");
                return false;
            }
        }
        return installEngine(installPath);
    }
    
    private boolean removeEngine() {
        int exit = runSingleCommandOnMwsAsRoot("pkgrm -n -a /tmp/default.adm " + ORCHPKG);
        logger.info("Result from removing of Orchestrator engine package: " + exit);
        if (exit == 0) {
            return true; 
        } else {
            return false;
        }
    }
    
    private boolean installEngine(final String installPath) {
        // When we have an ERICorch package then will install from ERICorch but if the media is not there, then install from the tar
        if (verifyPathAvailabilityOnMws(installPath + "/" + ORCHPKG  + ".pkg") == 0 ) {
            logger.info("INSTALLING ENGINE from ERICorch.pkg");
            int exit = runSingleCommandOnMwsAsRoot("pkgadd -G -n -a /tmp/default.adm -d " + installPath + "/" + ORCHPKG + ".pkg all");
            logger.info("Result from installing of Orchestrator engine package: " + exit);
            if (exit == 0) {
                return true; 
            } else {
                return false;
            }
        } else {
            logger.info("NO ERICorch.pkg available - INSTALLING FROM TAR");
            // Determine whether to delete old code
            if (!removeOldCode()) {
                logger.warn("Failed to remove old wfeng code, will continue...");
            }
            User rootUser = getMsHost().getUsers(UserType.ADMIN).get(0);
            if (runSingleCommandOnMwsAsRoot(getCreateInstallDirCommand(), false) != 0) {
                logger.error("Failed to create install directory");
                return false;           
            }
            String wfengTar = (String) TafConfigurationProvider.provide().getProperty("testware.orch_engine_tar");
            if (!copyWfEngTar(getMsHost(), rootUser, wfengTar)) {
                logger.error("Failed to copy wfeng tar");
                return false;
            }
            String wfprereqTar = (String) TafConfigurationProvider.provide().getProperty("testware.orch_engine_prereq_tar");

            initializeHelper(getMsHost(), rootUser);
            // For both single and multiple tars we need to untar the wfeng tar ball
            if (unTarWfEngTar(wfengTar) != 0) {
                logger.error("Failed to unpack wfeng tar");
                return false;
            }
            String eng_cmd = "full_install.bsh";
            if (wfprereqTar != null) {
                logger.info("Installing from prereq and engine tar balls");
                if (!copyWfEngTar(getMsHost(), rootUser, wfprereqTar)) {
                    logger.error("Failed to copy wfeng tar");
                    return false;
                }
                eng_cmd = "install_prereq.sh";
                if (unTarWfEngTar(wfprereqTar) != 0) {
                    logger.error("Failed to unpack wfeng prereq tar");
                    return false;
                }
                if (installWfEngTar(eng_cmd) != 0) {
                    logger.error("Failed to install wfeng prereq tar");
                    return false;
                }
                eng_cmd = "install.sh";
            } else {
                logger.info("Installing from single engine tar ball");
            }
            
            if (installWfEngTar(eng_cmd) != 0) {
                logger.error("Failed to install wfeng tar");
                return false;
            }
            closeShell();
            runSingleCommandOnMwsAsRoot(getRemoveInstallDirCmd(), false);
            return true;
        }

    }
    
    /** Looks to see if old code exists from install from tar, and if so deletes it
     * 
     * @return true if successful, false if failed
     */
    private boolean removeOldCode() {
        // Remove old wfeng package - might be in lib or lib/python - cope with either
        int exit = runSingleCommandOnMwsAsRoot("find /ericsson/orchestrator/lib -name wfeng | xargs rm -rf");
        logger.info("Result from removing of Orchestrator engine wfeng code: " + exit);
        if (exit == 0) {
            // Might be in bin or lib - so remove either
            exit = runSingleCommandOnMwsAsRoot("find /ericsson/orchestrator/ -name workfloweng.py | xargs rm -f");
            logger.info("Result from removing of Orchestrator engine workfloweng.py code: " + exit);
            if (exit == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    public boolean createAdmFile() {
        User rootUser = getMsHost().getUsers(UserType.ADMIN).get(0);
        return sendFileRemotely(getMsHost(), rootUser, "default.adm", "/tmp");
    }
    
    public boolean removeAdmFile() {
        User rootUser = getMsHost().getUsers(UserType.ADMIN).get(0);
        return deleteRemoteFile(getMsHost(), rootUser, "/tmp/default.adm");
    }

    
    
    
    
    
    /**
     * Customises ini file template given TafConfiguration, assumes analyseAdminHosts already run
     * @param configuration
     * @return
     */
    public int customiseIniFile(final TafConfiguration configuration) {
        String upgradeDir = getDepotDir(configuration);
        String mwsBkip = (String) configuration.getProperty(MWS_BKIP);
        String masHost = (String) configuration.getProperty(MASHOST);
        
        String ossrcMedia = getMediaLocation(configuration, "ossrc");
        String omsasMedia = getMediaLocation(configuration, "omsas");
        String omMedia = getMediaLocation(configuration, "om");
        String cominfMedia = getMediaLocation(configuration, "cominf");
        String solMedia = getMediaLocation(configuration, "solaris");
        
        if (upgradeDir == null || ossrcMedia == null || cominfMedia == null || omMedia == null || solMedia == null || omsasMedia == null) {
            // Error - should have media locations
            return -1;
        }
        String iniFile = getWfEngCfg()  + "/wfeng_" + getAdminHost1() + ".ini";
        String patchMedia = omMedia + "/Patches";
        
        
        StringBuilder sedCommand = new StringBuilder();
        sedCommand.append("sed -e \"s?BOOT_MEDIA_VAL?");
        sedCommand.append(solMedia);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?OM_MEDIA_VAL?");
        sedCommand.append(omMedia);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?PATCH_MEDIA_VAL?");
        sedCommand.append(patchMedia);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?OSSRC_MEDIA_VAL?");
        sedCommand.append(ossrcMedia);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?OMSAS_MEDIA_VAL?");
        sedCommand.append(omsasMedia);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?COMINF_MEDIA_VAL?");
        sedCommand.append(cominfMedia);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ORCH_DEPOT_VAL?");
        sedCommand.append(upgradeDir);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?UAS_IP_VAL?");
        Host uasHost = DataHandler.getHostByType(HostType.UAS);
        if (uasHost != null) {
            sedCommand.append(uasHost.getIp());
        }
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?MWS_IP_VAL?");
        sedCommand.append(getMsHost().getIp());
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_HOST1_VAL?");
        sedCommand.append(getAdminHost1());
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_HOST2_VAL?");
        String adminHost2 = getAdminHost2();
        if (adminHost2 == null) {
            adminHost2 = "";
        }
        sedCommand.append(adminHost2);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?MWS_BIP_VAL?");
        if (mwsBkip == null || mwsBkip.length() == 0) {
            sedCommand.append(getMsHost().getIp());
        } else {
            sedCommand.append(mwsBkip);
        }
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?MASHOST_VAL?");
        if (masHost == null) {
            masHost = "";	
        }
        sedCommand.append(masHost);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_ILO1_VAL?");
        String iloIp= getILOIp1();
        if (iloIp == null){
        	iloIp = "";
        }
        sedCommand.append(iloIp);
        sedCommand.append("?g\" ");
        sedCommand.append("-e \"s?ADMIN_ILO2_VAL?");
        String iloIp2 = getILOIp2();
        if (iloIp2 == null){
        	iloIp2 = "";
        }
        sedCommand.append(iloIp2);
        sedCommand.append("?g\" " + getWfEngCfg() + "/" + INI_TEMPLATE + " > ");
        sedCommand.append(iniFile);

        Shell shell = executeCommand(sedCommand.toString());
        readUntilClosed(shell, true);
        return getShellExitValue(shell);
    }

    /**
     * Displays the status file, assumes that have already called initialize to
     * get connection to mws
     * @param workfile
     * @param hostfile
     */
    public void displayStatusFile(final String workfile, final String hostfile) {
        // Locate and output the status file
        String statusFilename = getStatusFilename(workfile, hostfile);
                 
        Shell shell = executeCommand("cat " + statusFilename);
        String statusFile = readUntilClosed(shell,  false);
        logger.info("Status file shows:\n" + statusFile, true);
    }
    
    /**
     * Parses local status file adding records to ENGINE_STATUS_DATA_SOURCE data source with
     * specified run_marker
     * @param filename
     * @return true if successfully parsed
     */
    public boolean parseStatusFile(final String filename) {
        
        TestContext context = TafTestContext.getContext();
        
        File file = new File(filename);
        if (!file.exists()) {
            logger.error("Status file " + filename + " does not exist");
            return false;
        }       

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            Document dom = db.parse(file);
            Element docEle = dom.getDocumentElement();
            NodeList phases = docEle.getChildNodes();
            
            if (phases != null && phases.getLength() > 0) {
                for (int i = 0; i < phases.getLength(); i++) {
                    if (phases.item(i).hasChildNodes()) {
                        Element phase = (Element)phases.item(i);
                        NodeList tasks = phase.getElementsByTagName("taskstatus");
                        if (tasks != null && tasks.getLength() > 0) {
                            for (int j = 0; j < tasks.getLength(); j++) {
                                Element task = (Element)tasks.item(j);
                                String id = task.getAttribute("id");
                                String host = task.getAttribute("host");
                                String status = task.getAttribute("status");
                                String duration = task.getAttribute("actualDur");
                                String estimated = task.getAttribute("estimatedDur");
                                EngineTaskStatusDataSource.addRecord(id + "_" + host, status, duration, estimated);
                            }
                        }
                    }
                }
            } 
        } catch (ParserConfigurationException pce) {
            logger.error("Failed to parse status file", pce);
            return false;
        } catch (SAXException se) {
            logger.error("Failed to parse status file", se);
            return false;
        } catch (IOException ioe) {
            logger.error("Failed to parse status file", ioe);
            return false;
        }
        return true;
    }
    
    /**
     * Parse the log file and extract error lines so can report when analyse status
     * @param filename
     * @return
     */
    public boolean parseLogFile(final String filename) {
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.matches(".*ERROR.*")) {
                    // Find task id on ERROR tags
                    Pattern p = Pattern.compile("^.*-->(.*):.*ERROR.*");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        String taskId = m.group(1);
                        // If the error is on a sequential line then between --> and the : is the taskId, but
                        // if its run in a parallel task then the taskIds have colour codes, so string the [34m at front
                        // and the [0m at end. NB start colour differs but will always be 2 digits
                        if (taskId.endsWith("[0m")) {
                            // Then we have colours in the task Id
                            taskId = taskId.substring(4, taskId.length()-3);
                        }
                        logger.debug("Adding error for task: " + taskId + " of " + line);
                        EngineTaskStatusDataSource.addErrorMsg(taskId, line);
                    }
                }
                if (line.matches(".*RESULT.*FAILED.*")) {
                    // Find task id on FAILED RESULT lines
                    Pattern p = Pattern.compile("^.* RESULT (.*): .*FAILED.*");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        logger.debug("Adding result for task: " + m.group(1) + " of " + line);
                        EngineTaskStatusDataSource.addErrorMsg(m.group(1), line);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            logger.error("Failed to parse log file: " + filename, e);
            return false;
        }
        return true;
    }

    
    /**
     * Returns name of status file for workfile and host
     * @param workfile  - full path to workfile
     * @param hostfile - full or absolute path to hostfile
     * @return
     */
    public String getStatusFilename(final String workfile, final String hostfile) {
        // Status file will be in same directory as workfile and will have format:
        // <workfilename>_<hostsfilename>_status.xml
        String statusFilename = workfile.substring(0, workfile.indexOf("."));
        // Hostprefix 
        String hostPrefix = hostfile.substring(0,hostfile.indexOf("."));
        String hostName = hostPrefix.substring(hostPrefix.lastIndexOf("/")+1);
        statusFilename = statusFilename + "_" + hostName + "_status.xml";
        return statusFilename;
    }
    
    /**
     * Returns true if orchestrator engine is already running
     * @return
     */
    public boolean isOrchEngineRunning() {
        // If it is running then there will be a lock file
        String lockDir = getWfEngBase() + "/.lock";
        int dirExists = runSingleCommandOnMwsAsRoot("ls " + lockDir, false);
        if (dirExists != 0) {
            logger.info("No engine running as directory " + lockDir + " does not exist");
            return false;
        }
        String lines[] = {};
        String output = getLastOutput();
        if (output != null) {
            lines = output.split("\\s");
        }
        for (String line : lines) {
            // line - should be pid of running process
            if (line.matches("[0-9]+")) {
                logger.info("Found lock file: " + line);
                if (isPidRunningOnMWS(line)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns true if pid is running on MWS
     * @param pid
     * @return
     */
    public boolean isPidRunningOnMWS(String pid) {
        logger.info("Checking if pid exists on MWS: " + pid);
        int pidExists = runSingleCommandOnMwsAsRoot("kill -0 " + pid, false);
        if (pidExists == 0) {
            logger.info("PID running: " + pid);
            return true;
        } else {
            logger.info("PID NOT running: " + pid);
            return false;
        }
    }
    
    public boolean isSuccessStatus(String status) {
        return Arrays.asList(successStatuses).contains(status);
    }
    /**
     * Writes a line to the hosts xml file for specified host
     * @param host    Host to represent
     * @param writer  Writer instance for hosts xml file
     */
    private void writeHostLine(final Host host, final PrintWriter writer) {
        StringBuilder line = new StringBuilder();
        line.append("<host name=\"");
        line.append(host.getHostname());
        line.append("\" server=\"");
        line.append(getOssType(host.getType()));
        line.append("\" ip=\"");
        line.append(host.getIp());
        line.append("\" username=\"orcha\"/>");
        writer.println(line.toString());
    }
    
}

