package com.musala.atmosphere.agent.command;

import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.state.ConnectedAgent;
import com.musala.atmosphere.agent.state.DisconnectedAgent;

public class PerformanceCommandTest extends AgentCommandBaseTest {

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
    public void testExecutePerformanceCommand() {
        agentState = new DisconnectedAgent(mockedAgent, mockedAgentManager, mockedConsole);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_PERFORMANCE, new ArrayList<String>());
        agentState.executeCommand(command);

        verifyZeroInteractions(mockedAgent, mockedAgentManager);
    }

    @Test
    public void testExecutePerformanceCommandWrongParameters() {
        agentState = new ConnectedAgent(mockedAgent, mockedAgentManager, mockedConsole, "", 123);

        AgentCommand command = new AgentCommand(AgentConsoleCommands.AGENT_PERFORMANCE, Arrays.asList("param1",
                                                                                                      "param2"));
        agentState.executeCommand(command);

        verifyZeroInteractions(mockedAgent, mockedAgentManager);
    }
}
