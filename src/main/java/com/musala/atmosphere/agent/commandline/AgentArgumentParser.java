package com.musala.atmosphere.agent.commandline;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.musala.atmosphere.commons.commandline.IArgumentParser;
import com.musala.atmosphere.commons.commandline.IOption;
import com.musala.atmosphere.commons.exceptions.ArgumentParseException;

/**
 * Parses the different Agent command line arguments.
 *
 * @author yordan.petrov
 *
 */
public class AgentArgumentParser implements IArgumentParser {
    /**
     * Parses and returns a {@link InetAddress} instance from the given string representation of a host name.
     *
     * @param hostname
     *        - the given string representation of a host name
     * @return a parsed {@link InetAddress} instance from the given string representation of a host name.
     * @throws ArgumentParseException
     *         when parsing of the host name fails
     */
    public InetAddress getHostname(String hostname) throws ArgumentParseException {
        InetAddress hostnameAddress = null;
        try {
            hostnameAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            String errorMessage = "Hostname parsing failed.";
            throw new ArgumentParseException(errorMessage, e);
        }

        return hostnameAddress;
    }

    /**
     * Parses and returns a {@link Integer} instance from the given string representation of a port number.
     *
     * @param port
     *        - the given string representation of a port number
     * @return a parsed {@link Integer} instance from the given string representation of a port number
     * @throws ArgumentParseException
     *         when parsing of the port number fails
     */
    public Integer getPort(String port) throws ArgumentParseException {

        Integer portNumber = null;
        try {
            portNumber = Integer.valueOf(port);
        } catch (NumberFormatException e) {
            String errorMessage = "Port parsing failed.";
            throw new ArgumentParseException(errorMessage, e);
        }

        if (portNumber < 1 || portNumber > 65535) {
            String errorMessage = "Port parsing failed. Port number must be in the range [1, 65535].";
            throw new ArgumentParseException(errorMessage);
        }

        return portNumber;
    }

    @Override
    public Object getOption(IOption option, String value) throws ArgumentParseException {
        AgentOption agentOption = (AgentOption) option;

        Object agentOptionValue = null;
        switch (agentOption) {
            case HOSTNAME:
                agentOptionValue = getHostname(value);
                break;

            case PORT:
                agentOptionValue = getPort(value);
                break;

            default:
                break;
        }

        return agentOptionValue;
    }
}
