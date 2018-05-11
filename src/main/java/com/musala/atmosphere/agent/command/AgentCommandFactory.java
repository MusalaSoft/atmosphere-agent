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
     * @return - instance of {@link com.musala.atmosphere.agent.command.AgentCommand} if the passed command is valid for
     *         execution by the {@link com.musala.atmosphere.agent.Agent agent}, or <b>null</b> if the command is not
     *         listed in {@link com.musala.atmosphere.agent.command.AgentConsoleCommands}.
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
