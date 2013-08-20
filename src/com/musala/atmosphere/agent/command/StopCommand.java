package com.musala.atmosphere.agent.command;

import com.musala.atmosphere.agent.Agent;

/**
 * Stops the agent.
 * 
 * @author nikola.taushanov
 * 
 */
public class StopCommand extends NoParamsAgentCommand
{
	public StopCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected void executeCommand(String[] params)
	{
		agent.stop();
	}
}
