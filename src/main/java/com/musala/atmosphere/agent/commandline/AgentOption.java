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

import com.musala.atmosphere.commons.commandline.CommandLineArgument;
import com.musala.atmosphere.commons.commandline.CommandLineOption;
import com.musala.atmosphere.commons.commandline.IOption;

/**
 * Contains all {@link CommandLineOption Command Line Options} that can be passed to the ATMOSPHERE Agent as well as
 * their {@link CommandLineArgument Command Line Arguments}.
 * 
 * @author yordan.petrov
 * 
 */
public enum AgentOption implements IOption {
    HOSTNAME(new CommandLineOption("h",
                                   "hostname",
                                   "the hostname or the IP address of the remote server to connect to."), new CommandLineArgument("hostname | ip")),
    PORT(new CommandLineOption("p", "port", "specifies the port number of the remote server."), new CommandLineArgument("port number")),
    HELP(new CommandLineOption("?", "help", "displays this help."));

    private CommandLineOption option;

    private CommandLineArgument argument;

    private AgentOption(CommandLineOption option) {
        this.option = option;
        this.argument = null;
    }

    private AgentOption(CommandLineOption option, CommandLineArgument clArgument) {
        this.option = option;
        this.argument = clArgument;
    }

    @Override
    public CommandLineOption getOption() {
        return option;
    }

    @Override
    public CommandLineArgument getArgument() {
        return argument;
    }

    @Override
    public boolean hasArgument() {
        return argument != null;
    }

    /**
     * Gets an {@link AgentOption} instance by its {@link CommandLineOption Command Line Option} short name.
     * 
     * @param shortName
     *        - the short name of the {@link CommandLineOption Command Line Option}
     * @return an {@link AgentOption} instance by the given short name of the {@link CommandLineOption Command Line
     *         Option} or <b>null</b> if such does not exist
     */
    public static AgentOption getOptionByShortName(String shortName) {
        AgentOption[] agentOptions = AgentOption.values();

        for (AgentOption currentAgentOption : agentOptions) {
            CommandLineOption clOption = currentAgentOption.getOption();
            if (shortName.equals(clOption.getShortName())) {
                return currentAgentOption;
            }
        }

        return null;
    }

    /**
     * Gets an {@link AgentOption} instance by its {@link CommandLineOption Command Line Option} long name.
     * 
     * @param longName
     *        - the long name of the {@link CommandLineOption Command Line Option}
     * @return an {@link AgentOption} instance by the given long name of the {@link CommandLineOption Command Line
     *         Option} or <b>null</b> if such does not exist
     */
    public static AgentOption getOptionByLongName(String longName) {
        AgentOption[] agentOptions = AgentOption.values();

        for (AgentOption currentAgentOption : agentOptions) {
            CommandLineOption clOption = currentAgentOption.getOption();
            if (longName.equals(clOption.getLongName())) {
                return currentAgentOption;
            }
        }

        return null;
    }
}
