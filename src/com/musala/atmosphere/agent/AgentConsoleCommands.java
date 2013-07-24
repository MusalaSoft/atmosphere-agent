package com.musala.atmosphere.agent;

import java.util.ArrayList; 
import java.util.List;

/**
 * Enumerates all possible first arguments of shell commands, available for Agent.
 * 
 * @author vladimir.vladimirov
 * 
 */
public enum AgentConsoleCommands
{
	AGENT_RUN("run", "Runs the created Agent on localhost."),
	AGENT_CONNECT("connect","Connects the agent to given ATMOSPHERE Server.\r\n " +
			"	CONNECT [IP:port]" + "	" + "- Tries to connect the agent to the ATMOSPHERE Server, specified by the given IP and port."),
	AGENT_HELP("help","prints all usable commands for manipulating the server"), 
	AGENT_STOP("stop","stops the running agent on localhost");

	private static final String DESCRIPTION_PREFFIX = "	 	- ";

	private String value;

	private String description;

	private AgentConsoleCommands(String firsgArgOfShellCommand, String descriptionOfShellCommand)
	{
		this.value = firsgArgOfShellCommand;
		this.description = descriptionOfShellCommand;
	}

	/**
	 * Gets the command of given AgentConsoleCommand element.
	 * 
	 * @return
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Gets the description of given AgentConsoleCommand.
	 * 
	 * @return - description of the given AgentConsoleCommand.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Gets list with all the commands someone can pass to the console to manage the Agent. For every available command
	 * there is a String which is in the following format: "<b>Command format</b> - <b>Command Description</b>" where
	 * <b>"Command format"</b> is pattern how to write the given command and what arguments it can be passed, while the
	 * <b>"Command description"</b> says what the command does.
	 * 
	 */
	public static List<String> getListOfCommands()
	{
		List<String> allCommandsFullInformation = new ArrayList<String>();
		for (AgentConsoleCommands currentState : AgentConsoleCommands.values())
		{
			String currentCommandInfo = currentState.getValue() + DESCRIPTION_PREFFIX + currentState.getDescription();
			allCommandsFullInformation.add(currentCommandInfo);
		}
		return allCommandsFullInformation;
	}
}
