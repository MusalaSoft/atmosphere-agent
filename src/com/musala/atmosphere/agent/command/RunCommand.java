package com.musala.atmosphere.agent.command;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentState;

public class RunCommand extends AgentCommand
{
	public RunCommand(Agent agent)
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
	public void executeCommand(String[] params)
	{
		if (agent.getAgentState() == AgentState.AGENT_RUNNING)
		{
			agent.writeLineToConsole("Cannot run agent: agent already running.");
		}
		else
		{
			agent.startAgentThread();
		}
	}
}
