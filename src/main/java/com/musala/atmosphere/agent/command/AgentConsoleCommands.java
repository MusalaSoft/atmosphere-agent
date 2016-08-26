package com.musala.atmosphere.agent.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enumerates all possible first arguments of shell commands, available for Agent.
 * 
 * @author nikola.taushanov
 * 
 */
public enum AgentConsoleCommands {

    AGENT_CONNECT("connect", "connect [IP] <port>", "Connects the agent to given ATMOSPHERE Server.\n IP - ATMOSPHERE Server IP address"
            + " (optional, if ommited, localhost is assumed)\n port - Server component port.", Arrays.asList(1, 2)),
    AGENT_HELP("help", "help", "Prints all available commands.", Arrays.asList(0)),
    AGENT_DEVICES("devices", "devices", "Lists all attached devices.", Arrays.asList(0)),
    AGENT_SERVER_ADDRESS("server-address", "server-address", "Print the address of the Server that this component is connected to.", Arrays.asList(0)),
    AGENT_EXIT("exit", "exit", "Stops the Agent component and exits.", Arrays.asList(0)),
    AGENT_UPTIME("uptime", "uptime", "Prints the time when Agent was ran and the uptime.", Arrays.asList(0)),
    AGENT_PERFORMANCE("performance", "performance", "Returns performance score and detailed information for this Agent.", Arrays.asList(0));

    private String command;

    private String syntax;

    private String description;

    private List<Integer> possibleNumberOfParameters;

    private AgentConsoleCommands(String command,
            String commmandSyntax,
            String commandDescription,
            List<Integer> possibleNumberOfParameters) {
        this.command = command;
        this.syntax = commmandSyntax;
        this.description = commandDescription;
        this.possibleNumberOfParameters = possibleNumberOfParameters;
    }

    /**
     * Gets the command of given AgentConsoleCommand element.
     * 
     * @return
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the description of given AgentConsoleCommand.
     * 
     * @return - description of the given AgentConsoleCommand.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the format for a command of given type.
     * 
     * @return - string, representing the way a command with its parameters should be written.
     */
    public String getSyntax() {
        return syntax;
    }

    /**
     * 
     * @return - a list with all acceptable values for the number of parameters for the corresponding command.
     */
    public List<Integer> getPossibleNumberOfParameters() {
        return possibleNumberOfParameters;
    }

    /**
     * Gets list with all the commands that can be passed to the console to manage the Agent. For every available
     * command there is a String which is in the following format: "<b>Command format</b> - <b>Command Description</b>"
     * where <b>"Command format"</b> is pattern how to write the given command and what arguments it can be passed,
     * while the <b>"Command description"</b> says what the command does.
     * 
     */
    public static List<String> getListOfCommandDescriptions() {
        List<String> allCommandsFullInformation = new ArrayList<String>();
        for (AgentConsoleCommands currentCommand : AgentConsoleCommands.values()) {
            String description = currentCommand.getDescription();
            String syntax = currentCommand.getSyntax();
            String currentCommandInfo = String.format("%-25s %s", syntax, description);
            allCommandsFullInformation.add(currentCommandInfo);
        }
        return allCommandsFullInformation;
    }

    /**
     * Searches for command by given command name. Returns null if no corresponding command is found.
     * 
     * @param commandName
     * @return an {@link AgentConsoleCommands AgentConsoleCommands} instance.
     */
    public static AgentConsoleCommands findCommand(String commandName) {
        AgentConsoleCommands resultCommand = null;

        for (AgentConsoleCommands possibleCommand : AgentConsoleCommands.values()) {
            String possibleCommandString = possibleCommand.getCommand();
            if (possibleCommandString.equalsIgnoreCase(commandName)) {
                resultCommand = possibleCommand;
                break;
            }
        }

        return resultCommand;
    }
}
