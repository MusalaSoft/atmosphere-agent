package com.musala.atmosphere.agent.command;

import com.musala.atmosphere.agent.Agent;

/**
 * Runs the server.
 * 
 * @author nikola.taushanov
 * 
 */
public class RunCommand extends NoParamsAgentCommand
{
	public RunCommand(Agent agent)
	{
		super(agent);
	}

	@Override
	public void executeCommand(String[] params)
	{
		agent.run();
	}
}
