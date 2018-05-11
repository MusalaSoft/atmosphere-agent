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
