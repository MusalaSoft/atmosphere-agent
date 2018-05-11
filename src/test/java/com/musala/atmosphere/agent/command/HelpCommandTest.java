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

import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.state.ConnectedAgent;
import com.musala.atmosphere.agent.state.DisconnectedAgent;

public class HelpCommandTest extends AgentCommandTestBase {

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

    @Test
    public void testExecuteHelpCommand() {
        agentState = new DisconnectedAgent(mockedAgent, mockedAgentManager, mockedConsole);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_HELP, new ArrayList<String>());
        agentState.executeCommand(command);

        verifyZeroInteractions(mockedAgent, mockedAgentManager);
    }

    @Test
    public void testExecuteHelpCommandWrongParameters() {
        agentState = new ConnectedAgent(mockedAgent, mockedAgentManager, mockedConsole, "", 123);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_HELP, Arrays.asList("param1", "param2"));
        agentState.executeCommand(command);

        verifyZeroInteractions(mockedAgent, mockedAgentManager);
    }
}
