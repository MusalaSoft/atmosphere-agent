package com.musala.atmosphere.agent.command;

import com.musala.atmosphere.agent.Agent;

/**
 * Common class for commands executed by the agent for a given console input.
 * 
 * @author nikola.taushanov
 * 
 */
public abstract class AgentCommand
{
	private static String ILLEGAL_ARGUMENTS_MESSAGE = "Illegal arguments for command. "
			+ "Use command 'help' for more information.";

	protected Agent agent;

	public AgentCommand(Agent agent)
	{
		this.agent = agent;
	}

	public void execute(String[] params)
	{
		if (!verifyParams(params))
		{
			agent.writeLineToConsole(ILLEGAL_ARGUMENTS_MESSAGE);
		}
		else
		{
			executeCommand(params);
		}
	}

	protected abstract boolean verifyParams(String[] params);

	protected abstract void executeCommand(String[] params);
}
