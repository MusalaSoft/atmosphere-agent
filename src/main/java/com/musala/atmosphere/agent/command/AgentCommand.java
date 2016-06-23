package com.musala.atmosphere.agent.command;

import java.util.List;

/**
 * Common class used to represent commands which are executed by the agent.
 * 
 * @author nikola.taushanov
 * 
 */
public class AgentCommand {
    private List<String> commandParameters;

    private AgentConsoleCommands commandType;

    public AgentCommand(AgentConsoleCommands aCommandType, List<String> aParameters) {
        commandType = aCommandType;
        commandParameters = aParameters;
    }

    public AgentConsoleCommands getCommandType() {
        return commandType;
    }

    public List<String> getCommandParameters() {
        return commandParameters;
    }
}
