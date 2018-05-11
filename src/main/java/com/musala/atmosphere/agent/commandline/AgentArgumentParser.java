// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

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
