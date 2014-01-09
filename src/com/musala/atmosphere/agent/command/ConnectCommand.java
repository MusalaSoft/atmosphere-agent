package com.musala.atmosphere.agent.command;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.IllegalPortException;

/**
 * Command which is executed when the user request a connection to a server.
 * 
 * @author nikola.taushanov
 * 
 */
public class ConnectCommand extends AgentCommand
{
	private static final Logger LOGGER = Logger.getLogger(ConnectCommand.class.getCanonicalName());

	public ConnectCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected boolean verifyParams(String[] params)
	{
		if (params != null && (params.length == 1 || params.length == 2))
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
			agent.connect(serverIp, serverPort);
		}
		catch (NumberFormatException | IllegalPortException e)
		{
			LOGGER.error("Server IP/Port is not valid.", e);
		}
		catch (NotBoundException | RemoteException e)
		{
			LOGGER.error("Connecting to Server resulted in exception.", e);
		}
	}
}
