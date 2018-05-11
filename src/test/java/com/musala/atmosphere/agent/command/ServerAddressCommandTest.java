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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.state.ConnectedAgent;
import com.musala.atmosphere.agent.state.DisconnectedAgent;

public class ServerAddressCommandTest extends AgentCommandTestBase {

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // Testing on Connected Agent
    @Test
    public void testExecuteServerAdressCommand() {
        agentState = new ConnectedAgent(mockedAgent, mockedAgentManager, mockedConsole, "", 0);
        mockedAgent.setState(agentState);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_SERVER_ADDRESS, new ArrayList<String>());
        agentState.executeCommand(command);

        verify(mockedConsole, times(1)).writeLine(any(String.class));
    }

    // Testing on Disconnected Agent
    @Test
    public void testExecuteServerAdressCommandOnDisconnectedAgent() {
        agentState = new DisconnectedAgent(mockedAgent, mockedAgentManager, mockedConsole);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_SERVER_ADDRESS, new ArrayList<String>());
        agentState.executeCommand(command);

        verify(mockedConsole, times(1)).writeLine(eq("Not connected to a Server."));
    }

}
