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
