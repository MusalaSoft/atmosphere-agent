package com.musala.atmosphere.agent.command;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentState;
import com.musala.atmosphere.agent.IllegalPortException;

public class ConnectCommand extends AgentCommand
{
	private static final Logger LOGGER = Logger.getLogger(Agent.class.getCanonicalName());

	public ConnectCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected boolean verifyParams(String[] params)
	{
		if (params.length == 1 || params.length == 2)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void executeCommand(String[] params)
	{
		if (agent.getAgentState() == AgentState.AGENT_CREATED)
		{
			agent.writeLineToConsole("Agent not running to be connected. Run the Agent first.");
			return;
		}

		String serverIp = "localhost";
		String serverPortAsString = null;
		if (params.length == 1)
		{
			serverPortAsString = params[0];
		}
		else if (params.length == 2)
		{
			serverIp = params[0];
			serverPortAsString = params[1];
		}

		try
		{
			int serverPort = Integer.parseInt(serverPortAsString);
			agent.connectToServer(serverIp, serverPort);
		}
		catch (NumberFormatException | UnknownHostException | ConnectException | IllegalPortException e)
		{
			LOGGER.error("Server IP/Port is not valid.", e);
		}
		catch (NotBoundException | RemoteException e)
		{
			LOGGER.error("Error when connecting to server.", e);
		}
	}
}
