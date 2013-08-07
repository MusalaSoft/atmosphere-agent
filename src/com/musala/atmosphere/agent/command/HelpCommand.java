package com.musala.atmosphere.agent.command;

import java.util.List;

import com.musala.atmosphere.agent.Agent;

public class HelpCommand extends AgentCommand
{
	public HelpCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected boolean verifyParams(String[] params)
	{
		if (params.length != 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	@Override
	protected void executeCommand(String[] params)
	{
		List<String> listOfCommands = AgentConsoleCommands.getListOfCommands();
		for (String agentConsoleCommand : listOfCommands)
		{
			agent.writeLineToConsole(agentConsoleCommand);
		}
	}
}
