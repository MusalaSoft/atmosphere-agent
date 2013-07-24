package com.musala.atmosphere.agent;

/**
 * Enumerates all agent states - instantiated but not running, running and stopped.
 * 
 * @author vladimir.vladimirov
 * 
 */
public enum AgentState
{
	AGENT_CREATED("agent_created"), AGENT_RUNNING("agent_running"), AGENT_STOPPED("agent_stopped");

	private String value;

	private AgentState(String stateOfAgent)
	{
		this.value = stateOfAgent;
	}

	public String toString()
	{
		return value;
	}
}