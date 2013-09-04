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
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

/**
 * State used by the agent when it is running.
 * 
 * @author nikola.taushanov
 * 
 */
public class RunningAgent extends AgentState
{
	private static final Logger LOGGER = Logger.getLogger(RunningAgent.class.getCanonicalName());

	private Date startDate;

	private Thread agentThread;

	private boolean isRunning;

	public RunningAgent(Agent agent)
	{
		this(agent, new ConsoleControl());
	}

	public RunningAgent(Agent agent, AgentManager agentManager, ConsoleControl agentConsole)
	{
		super(agent, agentManager, agentConsole);
	}

	public RunningAgent(Agent agent, ConsoleControl agentConsole)
	{
		super(agent, null, agentConsole);
		isRunning = true;

		InnerRunThread innerThread = new InnerRunThread();
		agentThread = new Thread(innerThread, "AgentRunningWaitThread");
		agentThread.start();

		try
		{
			agentManager = new AgentManager(AgentPropertiesLoader.getADBPath(), agent.getAgentRmiPort());
		}
		catch (RemoteException e)
		{
			stopAgent();

			LOGGER.fatal("Creating an AgentManager instance resulted in RMI exception.", e);
			throw new RuntimeException("Agent could not be started.", e);
		}
		catch (ADBridgeFailException e)
		{
			stopAgent();

			LOGGER.fatal("Creating an AgentManager instance resulted in ADB related exception.", e);
			throw new RuntimeException("Agent could not be started.", e);
		}
	}

	@Override
	public Date getStartDate()
	{
		return startDate;
	}

	@Override
	public String getServerIp()
	{
		LOGGER.warn("Agent is not connected to a server.");
		return null;
	}

	@Override
	public int getServerRmiPort()
	{
		LOGGER.warn("Agent is not connected to a server.");
		return -1;
	}

	@Override
	public void connectToServer(String ipAddress, int port) throws AccessException, RemoteException, NotBoundException
	{
		agentManager.connectToServer(ipAddress, port);
		agent.setState(new ConnectedAgent(agent, agentManager, agentConsole));
	}

	@Override
	public void run()
	{
		LOGGER.warn("Agent is already running.");
	}

	@Override
	public void stop()
	{
		if (agentThread != null)
		{
			stopAgentThread();
		}
		else
		{
			stopAgent();
		}
	}

	private void stopAgentThread()
	{
		isRunning = false;

		try
		{
			agentThread.join();
		}
		catch (InterruptedException e)
		{
			LOGGER.fatal("Agent waiting thread was interrupted.", e);
		}
	}

	private void stopAgent()
	{
		isRunning = false;
		if (agentManager != null)
		{
			agentManager.close();
		}
		agent.setState(new StoppedAgent(agent, agentConsole));
	}

	@Override
	public int getAgentRmiPort()
	{
		LOGGER.warn("Agent is not connected to a server.");
		return -1;
	}

	@Override
	public List<String> getAllDevicesSerialNumbers()
	{
		List<String> deviceSerialNumbers = null;
		try
		{
			deviceSerialNumbers = agentManager.getAllDeviceWrappers();
		}
		catch (RemoteException e)
		{
			// Impossible as the method is invoked locally.
		}
		return deviceSerialNumbers;
	}

	@Override
	public List<IDevice> getAllAttachedDevices()
	{
		List<IDevice> attachedDevices = agentManager.getDevicesList();
		return attachedDevices;
	}

	@Override
	public void createAndStartEmulator(DeviceParameters parameters) throws IOException
	{
		agentManager.createAndStartEmulator(parameters);
	}

	@Override
	public void removeEmulatorBySerialNumber(String deviceSN)
		throws RemoteException,
			IOException,
			DeviceNotFoundException,
			NotPossibleForDeviceException
	{
		agentManager.eraseEmulator(deviceSN);
	}

	private class InnerRunThread implements Runnable
	{
		/**
		 * Runs the Agent on localhost and port <i>agentRmiPort</i>.
		 */
		@Override
		public void run()
		{
			LOGGER.info("Running agent...");
			while (isRunning)
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					LOGGER.warn("Agent wait thread was interrupted.", e);
				}
			}
			stopAgent();
		}
	}
}
