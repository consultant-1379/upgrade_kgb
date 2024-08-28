package com.ericsson.oss.orchestrator.test.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.google.inject.Singleton;

/**
 * Simple operator class around CLIHelper
 *
 */
@Operator(context = Context.CLI)
@Singleton
public class CLIHelperOperator {    

    Logger logger = LoggerFactory.getLogger(CLIHelperOperator.class);
    
    private CLICommandHelper cliCommandHelper;

    /**
     * Initialises cliCommandHelper and opens shell
     * @param host
     * @param user
     */
    public void initializeHelper(final Host host, final User user) {
        logger.debug("Opening shell to " + host.getHostname() + " with user " + user.getUsername());
        cliCommandHelper = new CLICommandHelper(host, user);
        cliCommandHelper.openShell();
    }   
    
    /**
     * Returns whether the shell is closed
     * @return if shell is closed
     */
    public boolean isShellClosed() {
        return cliCommandHelper.isClosed();
    }
    
    /**
     * Executes command on CLI. Do not use for long-running commands
     * @param command
     * @return stdOut from command.
     */
    public String execute(final String command) {
        logger.debug("Executing command: " + command);
        return cliCommandHelper.execute(command);
    }
    
    /**
     * Runs an interactive script
     * @param command - script to run
     */
    public void runInteractiveScript(final String command) {
        logger.debug("Executing interactive command: " + command);
        cliCommandHelper.runInteractiveScript(command);
    }
    
    /**
     * Returns the stdout of last command
     * @return String 
     */
    public String getStdOut() {
        return cliCommandHelper.getStdOut();
    }
    
    /**
     * When running interactive command, match the expected string
     * @param expect
     * @return String
     */
    public String expect(final String expect) {
        logger.debug("Expecting: " + expect);
        String received = cliCommandHelper.expect(expect);
        logger.debug("Matched: " + received);
        return received;
    }
    
    /**
     * When running interactive command, match the expected string
     * @param expect
     * @param timeout - timeout in seconds
     * @return
     */
    public String expect(final String expect, long timeout) {
        logger.debug("Expecting: " + expect);
        String received = cliCommandHelper.expect(expect, timeout);
        logger.debug("Matched: " + received);
        return received;
    }
    
    /**
     * Interact with the shell
     * @param input
     */
    public void interactWithShell(final String input) {
        logger.debug("Interacting with: " + input);
        cliCommandHelper.interactWithShell(input);

    }
    
    /**
     * Interact with shell, waiting for question, and responding with answer
     * @param question - String to match on
     * @param answer - Input to give to shell when matched question
     * @return true/false
     */
    public Boolean interactWithShell(final String question, final String answer) {
        logger.debug("Expecting: " + question + " and answer " + answer);
        try 
        {
        	expect(question);
            interactWithShell(answer);
            return true;        	
        } catch(Exception e){
        
        	logger.debug("Expected question dint occur,verify your question :"+question);
            return false;

        }
        
    }
    
    /**
     * Expect shell to close within default timeout
     */
    public void expectShellClosure() {
        logger.debug("Expecting shell closure");
        cliCommandHelper.expectShellClosure();
    }
    
    /**
     * Expect shell to close within specified timeout
     * @param timeoutInSeconds
     */
    public void expectShellClosure(final long timeoutInSeconds) {
        logger.debug("Expecting shell closure");
        cliCommandHelper.expectShellClosure(timeoutInSeconds);
    }

    /**
     * Return the exit value from last command
     * @return exitCode from running command (via execute)
     */
    public int getCommandExitValue() {
        return cliCommandHelper.getCommandExitValue();
    }
    
    /**
     * Closes shell
     */
    public void closeShell() {
        logger.debug("Close and validate shell");
        cliCommandHelper.closeAndValidateShell();
    }
    
    /**
     * Return the exit code from the shell
     * @return int
     */
    public int getShellExitValue() {
        return cliCommandHelper.getShellExitValue();
    }

}

