package com.musala.atmosphere.agent.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.commons.sa.ConsoleControl;

public class RunningAgentTest
{
	private AgentState runningAgentState;

	private Agent innerAgent;

	@Before
	public void setUp()
	{
		innerAgent = spy(new Agent());
		runningAgentState = new RunningAgent(innerAgent);
	}

	@After
	public void tearDown()
	{
		runningAgentState.stop();
	}

	@Test
	public void testGetServerIp()
	{
		assertNull(runningAgentState.getServerIp());
	}

	@Test
	public void testGetServerRmiPort()
	{
		assertEquals(-1, runningAgentState.getServerRmiPort());
	}

	@Test
	public void testConnectToServer() throws Exception
	{
		AgentManager innerAgentManagerMock = mock(AgentManager.class);
		ConsoleControl consoleMock = mock(ConsoleControl.class);
		AgentState runningAgentState = new RunningAgent(innerAgent, innerAgentManagerMock, consoleMock);
		String serverAddress = "localhost";
		int serverPort = 1980;

		runningAgentState.connectToServer(serverAddress, serverPort);

		verify(innerAgent, times(1)).setState(any(ConnectedAgent.class));
	}

	@Test
	public void testRun()
	{
		// Nothing should happen. This tests only if some error occurs when run is called on already running agent.
		runningAgentState.run();
	}

	@Test
	public void testStop() throws Exception
	{
		runningAgentState.stop();

		verify(innerAgent, times(1)).setState(any(StoppedAgent.class));
	}
}