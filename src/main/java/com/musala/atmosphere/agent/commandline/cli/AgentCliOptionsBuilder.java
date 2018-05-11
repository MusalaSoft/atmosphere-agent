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

package com.musala.atmosphere.agent.commandline.cli;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.musala.atmosphere.agent.commandline.AgentOption;
import com.musala.atmosphere.commons.commandline.cli.AbstractCliOptionsBuilder;

/**
 * Builds the CLI Options that can be used to parse command line arguments with a CommandLineParser. This class is used
 * for building command line arguments that can be used with the <a
 * href="http://commons.apache.org/proper/commons-cli/">Apache Commons CLI</a> library.
 * 
 * @author yordan.petrov
 * 
 */
public class AgentCliOptionsBuilder extends AbstractCliOptionsBuilder {
    /**
     * Builds and returns the list of {@link Option CLI Options} for the remote server host name and port.
     * 
     * @return the list of {@link Option CLI Options} for the remote server host name and port
     */
    private List<Option> buildHostnameOptions() {
        AgentOption hostnameOption = AgentOption.HOSTNAME;
        AgentOption portNumberOption = AgentOption.PORT;

        Option hostnameCliOption = buildOption(hostnameOption);
        Option portNumberCliOption = buildOption(portNumberOption);

        List<Option> hostnameCliOptions = new ArrayList<Option>();
        hostnameCliOptions.add(hostnameCliOption);
        hostnameCliOptions.add(portNumberCliOption);

        return hostnameCliOptions;
    }

    @Override
    public Options buildOptions() {
        List<Option> hostnameOptions = buildHostnameOptions();
        addOptions(hostnameOptions);

        return cliOptions;
    }
}
