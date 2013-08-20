package com.musala.atmosphere.agent.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.Agent;

public class ExitCommandTest
{
	private Agent innerAgentMock;

	private AgentCommand exitCommand;

	private String[] emptyArray;

	@Before
	public void setUp()
	{
		innerAgentMock = mock(Agent.class);
		exitCommand = new ExitCommand(innerAgentMock);
		emptyArray = new String[] {};
	}

	@Test
	public void testExecuteCommand()
	{
		exitCommand.execute(emptyArray);

		verify(innerAgentMock, times(1)).close();
		verifyNoMoreInteractions(innerAgentMock);
	}
}
