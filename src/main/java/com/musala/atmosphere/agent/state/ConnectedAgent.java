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

package com.musala.atmosphere.agent.state;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.command.AgentCommand;
import com.musala.atmosphere.agent.command.AgentConsoleCommands;
import com.musala.atmosphere.commons.sa.ConsoleControl;

/**
 * State of the agent, used when it is connected to a server.
 * 
 * @author nikola.taushanov
 * 
 */
public class ConnectedAgent extends AgentState {
    private static final Logger LOGGER = Logger.getLogger(ConnectedAgent.class.getCanonicalName());

    private String serverIp;

    private int serverPort;

    public ConnectedAgent(Agent agent,
            AgentManager agentManager,
            ConsoleControl agentConsole,
            String serverIp,
            int serverPort) {
        super(agent, agentManager, agentConsole);
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    @Override
    public void executeServerAddressCommand(AgentCommand commandForExecution) {
        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_SERVER_ADDRESS, commandForExecution);

            String consoleMessage = String.format("Server address - \"%s:%d\"", serverIp, serverPort);
            agentConsole.writeLine(consoleMessage);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        }
    }

    @Override
    public void executeConnectCommand(AgentCommand commandForExecution) {
        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_CONNECT, commandForExecution);

            String serverAdress = String.format("\"%s:%d\"", serverIp, serverPort);
            agentConsole.writeLine("Agent already connected to " + serverAdress);

            LOGGER.info("Error connecting agent to server: agent is already connected to " + serverAdress);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        }
    }

}
