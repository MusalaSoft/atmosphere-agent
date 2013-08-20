package com.musala.atmosphere.agent.command;

import java.util.List;

import com.musala.atmosphere.agent.Agent;

/**
 * Help command.
 * 
 * @author nikola.taushanov
 * 
 */
public class HelpCommand extends NoParamsAgentCommand
{
	public HelpCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected void executeCommand(String[] params)
	{
		List<String> listOfCommands = AgentConsoleCommands.getListOfCommands();
		if (listOfCommands != null)
		{
			for (String agentConsoleCommand : listOfCommands)
			{
				agent.writeLineToConsole(agentConsoleCommand);
			}
		}
	}
}
