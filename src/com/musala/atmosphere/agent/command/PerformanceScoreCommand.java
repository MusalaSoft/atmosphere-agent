package com.musala.atmosphere.agent.command;

import com.musala.atmosphere.agent.Agent;

/**
 * Print detailed information about the performance of the machine for this agent.
 * 
 * @author nikola.taushanov
 * 
 */
public class PerformanceScoreCommand extends AgentCommand
{

	public PerformanceScoreCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected boolean verifyParams(String[] params)
	{
		return false;
	}

	@Override
	protected void executeCommand(String[] params)
	{
		// TODO Should be implemented when performance score is available.
	}

}
