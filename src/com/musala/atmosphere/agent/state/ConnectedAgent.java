package com.musala.atmosphere.agent.state;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.IllegalPortException;
import com.musala.atmosphere.commons.sa.ConsoleControl;

/**
 * State of the agent used when it is connected to a server.
 * 
 * @author nikola.taushanov
 * 
 */
public class ConnectedAgent extends RunningAgent
{
	private static final Logger LOGGER = Logger.getLogger(ConnectedAgent.class.getCanonicalName());

	public ConnectedAgent(Agent agent, AgentManager agentManager, ConsoleControl agentConsole)
	{
		super(agent, agentManager, agentConsole);
	}

	@Override
	public int getAgentRmiPort()
	{
		int serverRmiPort = agentManager.getServerRmiPort();
		return serverRmiPort;
	}

	@Override
	public String getServerIp()
	{
		String serverIpAddress = agentManager.getServerIPAddress();
		return serverIpAddress;
	}

	@Override
	public void connectToServer(String ipAddress, int port)
		throws AccessException,
			RemoteException,
			NotBoundException,
			IllegalPortException
	{
		LOGGER.warn("The agent is already connected to a server.");
	}
}
