package com.musala.atmosphere.agent.state;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.IllegalPortException;
import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

/**
 * State used by the agent when it is stopped.
 * 
 * @author nikola.taushanov
 * 
 */
public class StoppedAgent extends AgentState
{
	private static final Logger LOGGER = Logger.getLogger(StoppedAgent.class.getCanonicalName());

	private final String STOPPED_AGENT_ERROR_MESSAGE = "The agent is stopped.";

	public StoppedAgent(Agent agent)
	{
		this(agent, new ConsoleControl());
	}

	public StoppedAgent(Agent agent, ConsoleControl agentConsole)
	{
		super(agent, null, agentConsole);
	}

	@Override
	public Date getStartDate()
	{
		return null;
	}

	@Override
	public String getServerIp()
	{
		LOGGER.error(STOPPED_AGENT_ERROR_MESSAGE);
		return null;
	}

	@Override
	public int getServerRmiPort()
	{
		LOGGER.error(STOPPED_AGENT_ERROR_MESSAGE);
		return -1;
	}

	@Override
	public void connectToServer(String ipAddress, int port)
		throws AccessException,
			RemoteException,
			NotBoundException,
			IllegalPortException
	{
		LOGGER.error(STOPPED_AGENT_ERROR_MESSAGE);
	}

	@Override
	public void run()
	{
		agent.setState(new RunningAgent(agent, agentConsole));
	}

	@Override
	public void stop()
	{
		LOGGER.warn(STOPPED_AGENT_ERROR_MESSAGE);
	}

	@Override
	public int getAgentRmiPort()
	{
		LOGGER.error(STOPPED_AGENT_ERROR_MESSAGE);
		return -1;
	}

	@Override
	public List<String> getAllDevicesSerialNumbers() throws RemoteException
	{
		LOGGER.error(STOPPED_AGENT_ERROR_MESSAGE);
		return null;
	}

	@Override
	public List<IDevice> getAllAttachedDevices()
	{
		LOGGER.error(STOPPED_AGENT_ERROR_MESSAGE);
		return null;
	}

	@Override
	public void createAndStartEmulator(DeviceParameters parameters) throws IOException
	{
		LOGGER.error(STOPPED_AGENT_ERROR_MESSAGE);
	}

	@Override
	public void closeEmulatorBySerialNumber(String deviceSN)
		throws RemoteException,
			NotPossibleForDeviceException,
			DeviceNotFoundException
	{
		LOGGER.error(STOPPED_AGENT_ERROR_MESSAGE);
	}

	@Override
	public void removeEmulatorBySerialNumber(String deviceSN)
		throws RemoteException,
			IOException,
			DeviceNotFoundException,
			NotPossibleForDeviceException
	{
		LOGGER.error(STOPPED_AGENT_ERROR_MESSAGE);
	}
}
