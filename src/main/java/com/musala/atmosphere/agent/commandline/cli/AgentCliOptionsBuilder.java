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
