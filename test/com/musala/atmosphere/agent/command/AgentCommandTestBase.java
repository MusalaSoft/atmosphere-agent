package com.musala.atmosphere.agent.command;

import static org.mockito.Mockito.mock;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentManager;
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

    protected ConsoleControl mockedConsole;

    protected void setUp() {
        mockedAgent = mock(Agent.class);
        mockedAgentManager = mock(AgentManager.class);
        mockedConsole = mock(ConsoleControl.class);
    }

    public void tearDown() {
    }
}
