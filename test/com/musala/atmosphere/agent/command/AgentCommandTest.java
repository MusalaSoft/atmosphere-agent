package com.musala.atmosphere.agent.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.Agent;

public class AgentCommandTest
{
	private AgentCommand mockedAgentCommand;

	private String[] randomParams;

	@Before
	public void setUp()
	{
		Agent innerAgentMock = mock(Agent.class);
		mockedAgentCommand = spy(new AgentCommand(innerAgentMock)
		{
			@Override
			protected boolean verifyParams(String[] params)
			{
				return false;
			}

			@Override
			protected void executeCommand(String[] params)
			{
			}
		});
		randomParams = new String[] {"1", "2"};
	}

	@Test
	public void testExecuteCommandWithValidParams()
	{
		when(mockedAgentCommand.verifyParams(any(String[].class))).thenReturn(true);
		mockedAgentCommand.execute(randomParams);

		verify(mockedAgentCommand, times(1)).executeCommand(eq(randomParams));
	}

	@Test
	public void testExecuteCommandWithInvalidParams()
	{
		when(mockedAgentCommand.verifyParams(any(String[].class))).thenReturn(false);
		mockedAgentCommand.execute(randomParams);

		verify(mockedAgentCommand, never()).executeCommand(any(String[].class));
	}
}
