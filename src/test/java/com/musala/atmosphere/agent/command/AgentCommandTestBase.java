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

import static org.mockito.Mockito.mock;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.DeviceManager;
import com.musala.atmosphere.agent.state.AgentState;
import com.musala.atmosphere.commons.sa.ConsoleControl;

/**
 * Base for all tests that are used to verify the correct behavior of the agent, agent state and agent manager, when
 * executing shell commands.
 * 
 * @author vladimir.vladimirov
 * 
 */
public abstract class AgentCommandTestBase {

    protected AgentState agentState;

    protected Agent mockedAgent;

    protected AgentManager mockedAgentManager;

    protected DeviceManager mockedDeviceManager;

    protected ConsoleControl mockedConsole;

    protected void setUp() {
        mockedAgent = mock(Agent.class);
        mockedAgentManager = mock(AgentManager.class);
        mockedConsole = mock(ConsoleControl.class);
        mockedDeviceManager = mock(DeviceManager.class);
    }

    public void tearDown() {
    }
}
