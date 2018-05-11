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
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.state.ConnectedAgent;
import com.musala.atmosphere.agent.state.DisconnectedAgent;

public class ConnectCommandTest extends AgentCommandTestBase {

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
    public void testExecuteConnectCommand() {
        agentState = new ConnectedAgent(mockedAgent, mockedAgentManager, mockedConsole, "", 0);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_CONNECT, new ArrayList<String>());
        agentState.executeCommand(command);

        verifyZeroInteractions(mockedAgentManager);
    }

    // Testing on Disconnected Agent
    @Test
    public void testExecuteConnectCommandOnDisconnectedAgent() throws Exception {
        final String serverIp = "serverip";
        final String serverPortAsString = "12";
        final Integer serverPortAsNumber = Integer.parseInt(serverPortAsString);

        agentState = new DisconnectedAgent(mockedAgent, mockedAgentManager, mockedConsole);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_CONNECT, Arrays.asList(serverIp,
                                                                                                  serverPortAsString));
        agentState.executeCommand(command);

        verify(mockedAgentManager, times(1)).connectToServer(eq(serverIp), eq(serverPortAsNumber));
        verify(mockedAgent, times(1)).setState(any(ConnectedAgent.class));
    }
}
