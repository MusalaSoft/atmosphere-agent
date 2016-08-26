package com.musala.atmosphere.agent.command;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.musala.atmosphere.agent.state.ConnectedAgent;
import com.musala.atmosphere.agent.state.DisconnectedAgent;

public class DevicesCommandTest extends AgentCommandTestBase {

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
    public void testExecuteDeviceCommand() throws RemoteException {
        agentState = new DisconnectedAgent(mockedAgent, mockedAgentManager, mockedConsole);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_DEVICES, new ArrayList<String>());
        agentState.executeCommand(command);

        verify(mockedConsole, atLeast(1)).writeLine(Matchers.anyString());
    }

    @Test
    public void testExecuteDeviceCommandWrongParameters() {
        agentState = new ConnectedAgent(mockedAgent, mockedAgentManager, mockedConsole, "", 123);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_DEVICES, Arrays.asList("param1", "param2"));
        agentState.executeCommand(command);

        verifyZeroInteractions(mockedAgent, mockedAgentManager);
    }
}
