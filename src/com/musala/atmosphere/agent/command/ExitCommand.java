package com.musala.atmosphere.agent.command;

import com.musala.atmosphere.agent.Agent;

/**
 * Command which is executed when the user wants to close and exit the agent.
 * 
 * @author nikola.taushanov
 * 
 */
public class ExitCommand extends NoParamsAgentCommand
{

	public ExitCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected void executeCommand(String[] params)
	{
		agent.exit();
	}
}
