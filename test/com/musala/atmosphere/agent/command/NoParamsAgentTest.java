package com.musala.atmosphere.agent.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.Agent;

public class NoParamsAgentTest
{
	private AgentCommand noParamsCommand;

	@Before
	public void setUp()
	{
		Agent innerAgentMock = mock(Agent.class);
		noParamsCommand = new NoParamsAgentCommand(innerAgentMock)
		{
			@Override
			protected void executeCommand(String[] params)
			{
			}
		};
	}

	@Test
	public void testVerifyParamsWithValidParams()
	{
		assertTrue(noParamsCommand.verifyParams(new String[] {}));
		assertTrue(noParamsCommand.verifyParams(null));
	}

	@Test
	public void testVerifyParamsWithInvalidParams()
	{
		assertFalse(noParamsCommand.verifyParams(new String[] {""}));
		assertFalse(noParamsCommand.verifyParams(new String[] {"1", "2"}));
	}
}
