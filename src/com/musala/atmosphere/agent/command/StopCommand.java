package com.musala.atmosphere.agent.command;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentState;

public class StopCommand extends AgentCommand
{
	public StopCommand(Agent agent)
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
		// our agent can be stopped only once; so if it is not in running state, it must be only created and
		// not started
		if (agent.getAgentState() != AgentState.AGENT_RUNNING)
		{
			agent.writeLineToConsole("Cannot stop agent: agent is not running to be stopped. To run it, type \"run\".");
		}
		else
		{
			agent.stop();
		}
	}
}
