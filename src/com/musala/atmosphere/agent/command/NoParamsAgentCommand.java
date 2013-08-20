package com.musala.atmosphere.agent.command;

import com.musala.atmosphere.agent.Agent;

/**
 * Common class for all agents which require no parameters. The logic for the verification is the same for all of them.
 * 
 * @author nikola.taushanov
 * 
 */
public abstract class NoParamsAgentCommand extends AgentCommand
{
	public NoParamsAgentCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	protected boolean verifyParams(String[] params)
	{
		if (params != null && params.length != 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}
