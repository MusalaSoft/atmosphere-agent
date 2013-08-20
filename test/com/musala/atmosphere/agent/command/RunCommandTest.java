package com.musala.atmosphere.agent.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.Agent;

public class RunCommandTest
{
	private Agent innerAgentMock;

	private AgentCommand runCommand;

	private String[] emptyArray;

	@Before
	public void setUp()
	{
		innerAgentMock = mock(Agent.class);
		runCommand = new RunCommand(innerAgentMock);
		emptyArray = new String[] {};
	}

	@Test
	public void testExecuteCommand()
	{
		runCommand.execute(emptyArray);

		verify(innerAgentMock, times(1)).run();
	}
}
