package com.musala.atmosphere.agent.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.Agent;

public class StopCommandTest
{
	private Agent innerAgentMock;

	private AgentCommand stopCommand;

	private String[] emptyArray;

	@Before
	public void setUp()
	{
		innerAgentMock = mock(Agent.class);
		stopCommand = new StopCommand(innerAgentMock);
		emptyArray = new String[] {};
	}

	@Test
	public void testExecuteCommand()
	{
		stopCommand.execute(emptyArray);

		verify(innerAgentMock, times(1)).stop();
	}
}
