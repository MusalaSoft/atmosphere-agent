package com.musala.atmosphere.agent.command;

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

public class UptimeCommandTest extends AgentCommandTestBase {

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
    public void testExecuteUptimeCommand() {
        agentState = new ConnectedAgent(mockedAgent, mockedAgentManager, mockedConsole, "", 0);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_UPTIME, new ArrayList<String>());
        agentState.executeCommand(command);

        verify(mockedAgent, times(1)).getStartDate();
    }

    @Test
    public void testExecuteUptimeCommandWrongParameters() {
        agentState = new DisconnectedAgent(mockedAgent, mockedAgentManager, mockedConsole);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_UPTIME, Arrays.asList("param1", "param2"));
        agentState.executeCommand(command);

        verifyZeroInteractions(mockedAgent);
    }
}
