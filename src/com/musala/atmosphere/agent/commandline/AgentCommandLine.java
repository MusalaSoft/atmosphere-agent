package com.musala.atmosphere.agent.commandline;

import java.net.InetAddress;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.commandline.cli.AgentCliOptionsBuilder;
import com.musala.atmosphere.commons.commandline.cli.CliCommandLine;
import com.musala.atmosphere.commons.exceptions.ArgumentParseException;
import com.musala.atmosphere.commons.exceptions.OptionNotPresentException;

/**
 * Command line that handles parsing and obtaining command line arguments for the ATMOSPHERE Agent component.
 * 
 * @author yordan.petrov
 * 
 */
public class AgentCommandLine extends CliCommandLine {
    protected static final Logger LOGGER = Logger.getLogger(AgentCommandLine.class);

    protected static final String AGENT_CL_SYNTAX = "java -jar agent.jar";

    /**
     * Constructs a new {@link AgentCommandLine} object that handles parsing and obtaining command line arguments for
     * the Agent component.
     */
    public AgentCommandLine() {
        super(new AgentCliOptionsBuilder(), new AgentArgumentParser());
    }

    public void printHelp() {
        printHelp(AGENT_CL_SYNTAX);
    }

    /**
     * Checks whether a host name option is passed to the command line.
     * 
     * @return <code>true</code> if a host name option is passed to the command line; <code>false</code> otherwise
     */
    public boolean hasHostname() {
        return hasOption(AgentOption.HOSTNAME);
    }

    /**
     * Checks whether a port number option is passed to the command line.
     * 
     * @return <code>true</code> if a port number option is passed to the command line; <code>false</code> otherwise
     */
    public boolean hasPort() {
        return hasOption(AgentOption.PORT);
    }

    /**
     * Checks whether if any of the host name or port number options is passed to the command line.
     * 
     * @return <code>true</code> if any of the host name an/or port number option is passed to the command line;
     *         <code>false</code> otherwise
     */
    public boolean hasServerConnectionOptions() {
        return hasHostname() || hasPort();
    }

    /**
     * Gets the {@link InetAddress} value of the argument passed with the host name option.
     * 
     * @return the {@link InetAddress} value of the argument passed with the host name option
     * @throws ArgumentParseException
     *         when parsing of the host name fails
     * @throws OptionNotPresentException
     *         when a host name option is not passed to the command line
     */
    public InetAddress getHostname() throws ArgumentParseException, OptionNotPresentException {
        return (InetAddress) getParsedOptionValue(AgentOption.HOSTNAME);
    }

    /**
     * Gets the {@link Integer} value of the argument passed with the port number option.
     * 
     * @return the {@link Integer} value of the argument passed with the port number option
     * @throws ArgumentParseException
     *         when parsing of the port number fails
     * @throws OptionNotPresentException
     *         when a port number option is not passed to the command line
     */
    public Integer getPort() throws ArgumentParseException, OptionNotPresentException {
        return (Integer) getParsedOptionValue(AgentOption.PORT);
    }
}
