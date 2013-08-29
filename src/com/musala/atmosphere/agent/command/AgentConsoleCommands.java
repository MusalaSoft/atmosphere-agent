package com.musala.atmosphere.agent.command;

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
	AGENT_RUN("run", "run", "Runs the Agent component."), AGENT_CONNECT(
			"connect",
			"connect [IP] <port>",
			"Connects the agent to given ATMOSPHERE Server.\n"
					+ " IP - ATMOSPHERE Server IP address (optional, if ommited, localhost is assumed)\n port - Server component port."), AGENT_HELP(
			"help", "help", "Prints all available commands."), AGENT_STOP("stop", "stop", "Stops the running Agent."), AGENT_DEVICES(
			"devices", "devices", "Lists all attached devices."), AGENT_SERVER_ADDRESS("server-address",
			"server-address", "Print the address of the Server that this component is connected to."), AGENT_EXIT(
			"exit", "exit", "Stops the Agent component and exits."), AGENT_UPTIME("uptime", "uptime",
			"Prints the time when Agent was ran and the uptime."), AGENT_PERFORMANCE("performance", "performance",
			"Returns performance score and detailed information for this Agent.");

	private String command;

	private String syntax;

	private String description;

	private AgentConsoleCommands(String command, String commmandSyntax, String commandDescription)
	{
		this.command = command;
		this.syntax = commmandSyntax;
		this.description = commandDescription;
	}

	/**
	 * Gets the command of given AgentConsoleCommand element.
	 * 
	 * @return
	 */
	public String getCommand()

	{
		return command;
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

	public String getSyntax()
	{
		return syntax;
	}

	/**
	 * Gets list with all the commands that can be passed to the console to manage the Agent. For every available
	 * command there is a String which is in the following format: "<b>Command format</b> - <b>Command Description</b>"
	 * where <b>"Command format"</b> is pattern how to write the given command and what arguments it can be passed,
	 * while the <b>"Command description"</b> says what the command does.
	 * 
	 */
	public static List<String> getListOfCommands()
	{
		List<String> allCommandsFullInformation = new ArrayList<String>();
		for (AgentConsoleCommands currentCommand : AgentConsoleCommands.values())
		{
			String description = currentCommand.getDescription();
			String syntax = currentCommand.getSyntax();
			String currentCommandInfo = String.format("%-25s %s", syntax, description);
			allCommandsFullInformation.add(currentCommandInfo);
		}
		return allCommandsFullInformation;
	}

	/**
	 * Searches for command by given command name. Returns null if no corresponding command is found.
	 * 
	 * @param commandName
	 * @return an {@link AgentConsoleCommands AgentConsoleCommands} instance.
	 */
	public static AgentConsoleCommands findCommand(String commandName)
	{
		AgentConsoleCommands resultCommand = null;

		for (AgentConsoleCommands possibleCommand : AgentConsoleCommands.values())
		{
			String possibleCommandString = possibleCommand.getCommand();
			if (possibleCommandString.equalsIgnoreCase(commandName))
			{
				resultCommand = possibleCommand;
				break;
			}
		}

		return resultCommand;
	}
}
