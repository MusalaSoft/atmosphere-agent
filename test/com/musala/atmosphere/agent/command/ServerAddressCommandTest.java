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

public class ServerAddressCommandTest extends AgentCommandBaseTest {

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
