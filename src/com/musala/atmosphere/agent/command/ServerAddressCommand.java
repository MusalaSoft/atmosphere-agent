package com.musala.atmosphere.agent.command;

import com.musala.atmosphere.agent.Agent;

/**
 * Prints the address of the server which the agent is connected to, if any.
 * 
 * @author nikola.taushanov
 * 
 */
public class ServerAddressCommand extends NoParamsAgentCommand
{

	public ServerAddressCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected void executeCommand(String[] params)
	{
		String serverIp = agent.getServerIp();
		int serverRmiPort = agent.getServerRmiPort();

		String consoleMessage = null;
		if (serverIp != null)
		{
			consoleMessage = String.format("Server address - %s:%d", serverIp, serverRmiPort);
		}
		else
		{
			consoleMessage = "Not connected to a Server.";
		}
		agent.writeLineToConsole(consoleMessage);
	}

}
