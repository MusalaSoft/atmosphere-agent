package com.musala.atmosphere.agent;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.command.AgentCommand;
import com.musala.atmosphere.agent.command.AgentCommandFactory;
import com.musala.atmosphere.agent.command.AgentConsoleCommands;
import com.musala.atmosphere.agent.state.AgentState;
import com.musala.atmosphere.agent.state.StoppedAgent;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

/**
 * Class that instantiates the Agent ATMOSPHERE component.
 * 
 * @author vladimir.vladimirov, nikola.taushanov
 * 
 */
public class Agent
{
	private static final Logger LOGGER = Logger.getLogger(Agent.class.getCanonicalName());

	private int agentRmiPort;

	private AgentCommandFactory commandFactory;

	private AgentState currentAgentState;

	private boolean closed;

	/**
	 * Creates an Agent component bound on the specified in <i>agent.properties</i> file port.
	 */
	public Agent()
	{
		this(AgentPropertiesLoader.getAgentRmiPort());
	}

	/**
	 * Creates an Agent component bound on given port.
	 * 
	 * @param agentRmiPort
	 *        - RMI port of the Agent.
	 */
	public Agent(int agentRmiPort)
	{
		closed = false;
		this.agentRmiPort = agentRmiPort;
		LOGGER.info("Agent created on port: " + agentRmiPort);
		commandFactory = new AgentCommandFactory(this);
		currentAgentState = new StoppedAgent(this);
	}

	/**
	 * Gets the date and time on which the Agent was started.
	 * 
	 * @return the specified date.
	 */
	public Date getStartDate()
	{
		Date result = currentAgentState.getStartDate();
		return result;
	}

	/**
	 * Prints a string to the agent's console output.
	 * 
	 * @param message
	 *        - the message to be printed.
	 */
	public void writeToConsole(String message)
	{
		currentAgentState.writeToConsole(message);
	}

	/**
	 * Prints a line to the agent's console output.
	 * 
	 * @param message
	 *        - the message to be printed.
	 */
	public void writeLineToConsole(String message)
	{
		currentAgentState.writeLineToConsole(message);
	}

	/**
	 * Connects this Agent to a Server.
	 * 
	 * @param ipAddress
	 *        - server's IP address.
	 * @param port
	 *        - server's RMI port.
	 * @throws NotBoundException
	 * @throws RemoteException
	 * @throws AccessException
	 */
	public void connectToServer(String ipAddress, int port) throws AccessException, RemoteException, NotBoundException
	{
		currentAgentState.connectToServer(ipAddress, port);
	}

	/**
	 * Gets the IP of the server to which the agent is currently connected to.
	 * 
	 * @return the current server's IP address.
	 */
	public String getServerIp()
	{
		String serverIpAddress = currentAgentState.getServerIp();
		return serverIpAddress;
	}

	/**
	 * Gets the port of the server to which the agent is currently connected to.
	 * 
	 * @return - the current server's port.
	 */
	public int getServerRmiPort()
	{
		int serverRmiPort = currentAgentState.getServerRmiPort();
		return serverRmiPort;
	}

	/**
	 * Runs the Agent.
	 */
	public void run()
	{
		currentAgentState.run();
	}

	/**
	 * Stops the Agent thread and releases the Android Debug Bridge.
	 */
	public void stop()
	{
		currentAgentState.stop();
	}

	/**
	 * Stops the Agent and effectively closes the current process.
	 */
	public void close()
	{
		stop();
		closed = true;
	}

	/**
	 * Gets the Agent's port.
	 * 
	 * @return Agent's RMI port.
	 */
	public int getAgentRmiPort()
	{
		return agentRmiPort;
	}

	/**
	 * Gets a list of all attached devices' serial numbers.
	 * 
	 * @return list of Strings that represent device serial numbers.
	 */
	public List<String> getAllDevicesSerialNumbers()
	{
		List<String> deviceSerialNumbers = null;
		deviceSerialNumbers = currentAgentState.getAllDevicesSerialNumbers();
		return deviceSerialNumbers;
	}

	/**
	 * Gets list of all devices attached to this Agent.
	 * 
	 * @return list of {@link IDevice IDevice} objects.
	 */
	public List<IDevice> getAllAttachedDevices()
	{
		List<IDevice> attachedDevices = currentAgentState.getAllAttachedDevices();
		return attachedDevices;
	}

	/**
	 * Creates and starts an emulator with specified properties on the current Agent.
	 * 
	 * @param parameters
	 *        - parameters to be passed to the emulator creation procedure.
	 * @throws IOException
	 */
	public void createAndStartEmulator(DeviceParameters parameters) throws IOException
	{
		currentAgentState.createAndStartEmulator(parameters);
	}

	/**
	 * Closes and erases an emulator by it's serial number.
	 * 
	 * @param deviceSN
	 *        - the serial number of the emulator to be closed.
	 * @throws RemoteException
	 * @throws NotPossibleForDeviceException
	 *         - when the specified serial number is of a real device and, therefore, cannot be closed.
	 * @throws DeviceNotFoundException
	 *         - when there is no device with the specified serial number.
	 * @throws IOException
	 */
	public void removeEmulatorBySerialNumber(String deviceSN)
		throws RemoteException,
			IOException,
			DeviceNotFoundException,
			NotPossibleForDeviceException
	{
		currentAgentState.removeEmulatorBySerialNumber(deviceSN);
	}

	/**
	 * Reads one line from the agent's console. For more information see
	 * {@link com.musala.atmosphere.commons.sa.ConsoleControl#readCommand() AgentConsole.readLine()}.
	 * 
	 * @return the first line in the console buffer as a String.
	 * @throws IOException
	 *         - when a console reading error occurs.
	 */
	private String readFromConsole() throws IOException
	{
		String command = currentAgentState.readCommandFromConsole();
		return command;
	}

	/**
	 * Executes a passed shell command from the console.
	 * 
	 * @param passedShellCommand
	 *        - the passed shell command.
	 * @throws IOException
	 */
	private void parseAndExecuteShellCommand(String passedShellCommand) throws IOException
	{
		if (passedShellCommand == null)
		{
			throw new IllegalArgumentException("Shell command passed for execution can not be 'null'.");
		}

		Pair<String, String[]> parsedCommand = ConsoleControl.parseShellCommand(passedShellCommand);
		String command = parsedCommand.getKey();
		String[] params = parsedCommand.getValue();

		if (!command.isEmpty())
		{
			executeShellCommand(command, params);
		}
	}

	/**
	 * Evaluates s passed command and calls the appropriate Agent method.
	 * 
	 * @param commandName
	 *        - command for execution.
	 * @param params
	 *        - passed command arguments.
	 */
	private void executeShellCommand(String commandName, String[] params)
	{
		AgentConsoleCommands command = AgentConsoleCommands.findCommand(commandName);

		// if the command does not match any of the enum commands
		if (command == null)
		{
			currentAgentState.writeLineToConsole("Unknown command. Use 'help' to retrieve list of available commands.");
			return;
		}

		AgentCommand executableCommand = commandFactory.getCommandInstance(command);
		executableCommand.execute(params);
	}

	/**
	 * Sets the current Agent state.
	 * 
	 * @param state
	 *        - the new {@link AgentState AgentState}.
	 */
	public void setState(AgentState state)
	{
		currentAgentState = state;
	}

	private boolean isClosed()
	{
		return closed;
	}

	/**
	 * Starts an Agent. Either no parameters or one that specifies on which port the Agent will be started can be
	 * passed. If the passed argument could not be parsed as integer an exception is thrown. If no parameters or more
	 * than one parameter was passed, the Agent will be created on the port specified in the properties file.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		// Check if an argument which specifies a port for the Agent was passed.
		int portToCreateAgentOn = 0;
		try
		{
			if (args.length == 1)
			{
				String passedRmiPort = args[0];
				portToCreateAgentOn = Integer.parseInt(passedRmiPort);
			}
			else
			{
				portToCreateAgentOn = AgentPropertiesLoader.getAgentRmiPort();
			}
		}
		catch (NumberFormatException e)
		{
			String exceptionMessage = "Parsing passed port resulted in an exception.";
			LOGGER.fatal(exceptionMessage, e);
			throw new IllegalPortException(exceptionMessage, e);
		}

		Agent localAgent = new Agent(portToCreateAgentOn);
		localAgent.executeShellCommand(AgentConsoleCommands.AGENT_RUN.getCommand(), null);

		do
		{
			String passedShellCommand = localAgent.readFromConsole();
			localAgent.parseAndExecuteShellCommand(passedShellCommand);
		} while (!localAgent.isClosed());
	}
}
