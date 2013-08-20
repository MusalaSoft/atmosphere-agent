package com.musala.atmosphere.agent.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Test;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

public class StoppedAgentTest
{
	private AgentState stoppedAgentState;

	private Agent innerAgent;

	@Before
	public void setUp()
	{
		innerAgent = mock(Agent.class);
		stoppedAgentState = new StoppedAgent(innerAgent);
	}

	@Test
	public void testGetStartDate()
	{
		assertNull(stoppedAgentState.getStartDate());
	}

	@Test
	public void testGetServerIp()
	{
		assertNull(stoppedAgentState.getServerIp());
	}

	@Test
	public void testGetServerRmiPort()
	{
		assertEquals(-1, stoppedAgentState.getServerRmiPort());
	}

	@Test
	public void testConnectToServer() throws Exception
	{
		stoppedAgentState.connectToServer("", -1);
		verify(innerAgent, never()).setState(any(AgentState.class));
	}

	@Test
	public void testStop() throws Exception
	{
		stoppedAgentState.stop();
		verify(innerAgent, never()).setState(any(AgentState.class));
	}

	@Test
	public void testGetAgentRmiPort()
	{
		assertEquals(-1, stoppedAgentState.getAgentRmiPort());
	}

	@Test
	public void testGetAllAttachedDevices()
	{
		assertNull(stoppedAgentState.getAllAttachedDevices());
	}

	@Test
	public void testCreateAndStartEmulator() throws IOException
	{
		// This method should do nothing. Throwing an exception would be an error.
		stoppedAgentState.createAndStartEmulator(new DeviceParameters());
	}

	@Test
	public void testCloseEmulatorBySerialNumber()
		throws RemoteException,
			NotPossibleForDeviceException,
			DeviceNotFoundException
	{
		// This method should do nothing. Throwing an exception would be an error.
		stoppedAgentState.closeEmulatorBySerialNumber("");
	}

	@Test
	public void testRemoveEmulatorBySerialNumber()
		throws RemoteException,
			IOException,
			DeviceNotFoundException,
			NotPossibleForDeviceException
	{
		// This method should do nothing. Throwing an exception would be an error.
		stoppedAgentState.removeEmulatorBySerialNumber("");
	}
}
