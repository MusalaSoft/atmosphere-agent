package com.musala.atmosphere.agent.command;

import com.musala.atmosphere.agent.Agent;

/**
 * A factory which is instantiated with an agent associated to it. For a given AgentConsoleCommand enum value it returns
 * the proper instance of command which should be used by this agent.
 * 
 * @author nikola.taushanov
 * 
 */
public class AgentCommandFactory
{
	private final Agent agent;

	public AgentCommandFactory(Agent agent)
	{
		this.agent = agent;
	}

	/**
	 * 
	 * @param consoleCommand
	 * @return instance of {@link AgentCommand AgentCommand} which is associated with the passed consoleCommand.
	 */
	public AgentCommand getCommandInstance(AgentConsoleCommands consoleCommand)
	{
		AgentCommand resultCommand = null;
		switch (consoleCommand)
		{
			case AGENT_RUN:
			{
				resultCommand = new RunCommand(agent);
				break;
			}
			case AGENT_CONNECT:
			{
				resultCommand = new ConnectCommand(agent);
				break;
			}
			case AGENT_HELP:
			{
				resultCommand = new HelpCommand(agent);
				break;
			}
			case AGENT_STOP:
			{
				resultCommand = new StopCommand(agent);
				break;
			}
			case AGENT_DEVICES:
			{
				resultCommand = new ListDevicesCommand(agent);
				break;
			}
			case AGENT_SERVER_ADDRESS:
			{
				resultCommand = new ServerAddressCommand(agent);
				break;
			}
			case AGENT_EXIT:
			{
				resultCommand = new ExitCommand(agent);
				break;
			}
			case AGENT_UPTIME:
			{
				resultCommand = new UptimeCommand(agent);
				break;
			}
			case AGENT_PERFORMANCE:
			{
				resultCommand = new PerformanceScoreCommand(agent);
				break;
			}
		}

		return resultCommand;
	}
}
