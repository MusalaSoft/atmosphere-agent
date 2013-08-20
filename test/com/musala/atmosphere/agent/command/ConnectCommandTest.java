package com.musala.atmosphere.agent.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.Agent;

public class ConnectCommandTest
{
	private Agent innerAgentMock;

	private AgentCommand connectCommand;

	@Before
	public void setUp()
	{
		innerAgentMock = mock(Agent.class);
		connectCommand = spy(new ConnectCommand(innerAgentMock));
	}

	@Test
	public void testExecuteCommandWithEmptyParams() throws Exception
	{
		String[] emptyParams = new String[] {"", ""};
		connectCommand.execute(emptyParams);

		verify(innerAgentMock, never()).connectToServer(anyString(), anyInt());
	}

	@Test
	public void testExecuteCommandWithPortOnly() throws Exception
	{
		String[] portOnlyInParams = new String[] {"1980"};
		connectCommand.execute(portOnlyInParams);

		verify(innerAgentMock, times(1)).connectToServer(eq("localhost"), eq(1980));
	}

	@Test
	public void testExecuteCommand() throws Exception
	{
		String[] portOnlyInParams = new String[] {"localhost", "1980"};
		connectCommand.execute(portOnlyInParams);

		verify(innerAgentMock, times(1)).connectToServer(eq("localhost"), eq(1980));
	}

	@Test
	public void testVerifyParams()
	{
		String[] portOnlyInParams = new String[] {};
		connectCommand.execute(portOnlyInParams);

		verify(connectCommand, never()).executeCommand(any(String[].class));
	}
}
