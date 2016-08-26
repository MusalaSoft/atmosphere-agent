package com.musala.atmosphere.agent.command;

import java.util.List;

/**
 * A factory that for a given AgentConsoleCommand enumerated value returns the proper instance of command which should
 * be used by the agent.
 * 
 * @author nikola.taushanov
 * 
 */
public class AgentCommandFactory {
    /**
     * Creates and returns an {@link AgentCommand} from the passed parameters, or <b>null</b> if no appropriate console
     * command is found.
     * 
     * @param command
     *        - command for execution by the agent.
     * @param params
     *        - additional parameters for the command.
     * @return - instance of {@link AgentCommand} if the passed command is valid for execution by the {@link Agent
     *         agent}, or <b>null</b> if the command is not listed in {@link AgentConsoleCommands}.
     */
    public static AgentCommand getCommandInstance(String command, List<String> params) {
        AgentCommand resultCommand = null;

        for (AgentConsoleCommands currentAgentCommand : AgentConsoleCommands.values()) {
            String underlyingCommand = currentAgentCommand.getCommand();
            if (underlyingCommand.equals(command)) {
                resultCommand = new AgentCommand(currentAgentCommand, params);
                break;
            }
        }

        return resultCommand;
    }
}
