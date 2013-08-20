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
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

/**
 * This class creates Agent objects which can be manipulated from the user.
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
	 * Creates an Agent object on the default agent rmi port ( whose value can be seen in the agent.properties file
	 * under the name ).
	 */
	public Agent()
	{
		this(AgentPropertiesLoader.getAgentRmiPort());
	}

	/**
	 * Creates Agent on given RMI port.
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
	 * Gets the date and time in which the Agent was run on.
	 * 
	 * @return - the specified date
	 */
	public Date getStartDate()
	{
		return currentAgentState.getStartDate();
	}

	/**
	 * Writes string to the agent's console output.
	 * 
	 * @param message
	 *        - the message that will be written
	 */
	public void writeToConsole(String message)
	{
		currentAgentState.writeToConsole(message);
	}

	/**
	 * Writes line to the agent's console output.
	 * 
	 * @param message
	 *        - the message that will be written
	 */
	public void writeLineToConsole(String message)
	{
		currentAgentState.writeLineToConsole(message);
	}

	/**
	 * Connects this Agent to a Server.
	 * 
	 * @param ipAddress
	 *        server's IP address.
	 * @param port
	 *        server's RMI port.
	 * @throws NotBoundException
	 * @throws RemoteException
	 * @throws AccessException
	 * @throws IllegalPortException
	 */
	public void connectToServer(String ipAddress, int port)
		throws AccessException,
			RemoteException,
			NotBoundException,
			IllegalPortException
	{
		currentAgentState.connectToServer(ipAddress, port);
	}

	/**
	 * Gets the IP of the server which the agent is connected to.
	 * 
	 * @return - the specified IP address of the server
	 */
	public String getServerIp()
	{
		String serverIpAddress = currentAgentState.getServerIp();
		return serverIpAddress;
	}

	/**
	 * Gets the RMI Port which the server uses to connect to the agent.
	 * 
	 * @return - the specified RMI Port
	 */
	public int getServerRmiPort()
	{
		int serverRmiPort = currentAgentState.getServerRmiPort();
		return serverRmiPort;
	}

	/**
	 * Runs the agent.
	 */
	public void run()
	{
		currentAgentState.run();
	}

	/**
	 * Stops the agent thread and disconnects the Android Debug Bridge.
	 */
	public void stop()
	{
		currentAgentState.stop();
	}

	/**
	 * Stops the agent and exits.
	 */
	public void close()
	{
		currentAgentState.stop();
		closed = true;
	}

	/**
	 * Gets the RMI port of the Agent
	 * 
	 * @return - number of port, under which the Agent is published in RMI.
	 */
	public int getAgentRmiPort()
	{
		return agentRmiPort;
	}

	/**
	 * Gets list with all serial numbers of devices, attached to this Agent.
	 * 
	 * @return - list of Strings which are the serial numbers of the devices, attached to this Agent
	 * @throws RemoteException
	 *         - when there is some problem with the RMI connection
	 */
	public List<String> getAllDevicesSerialNumbers() throws RemoteException
	{
		List<String> deviceSerialNumbers = currentAgentState.getAllDevicesSerialNumbers();
		return deviceSerialNumbers;
	}

	/**
	 * Gets list with all devices attached to this Agent.
	 * 
	 * @return - list of IDevices containing the attached devices.
	 */
	public List<IDevice> getAllAttachedDevices()
	{
		List<IDevice> attachedDevices = currentAgentState.getAllAttachedDevices();
		return attachedDevices;
	}

	/**
	 * Creates and starts an emulator on this agent with desired properties.
	 * 
	 * @param parameters
	 *        - requred DeviceParameters for the emulator to have
	 * @throws IOException
	 */
	public void createAndStartEmulator(DeviceParameters parameters) throws IOException
	{
		currentAgentState.createAndStartEmulator(parameters);
	}

	/**
	 * Closes given emulator.
	 * 
	 * @param deviceSN
	 *        - the Serial number of the device we want to stop running.
	 * @throws RemoteException
	 * @throws NotPossibleForDeviceException
	 *         - when the device, corresponding to the passed device serial number, is not an emulator and, therefore,
	 *         can not be closed
	 * @throws DeviceNotFoundException
	 *         - when there is no device with the given serial number
	 */
	public void closeEmulatorBySerialNumber(String deviceSN)
		throws RemoteException,
			NotPossibleForDeviceException,
			DeviceNotFoundException
	{
		currentAgentState.closeEmulatorBySerialNumber(deviceSN);
	}

	/**
	 * Closes given emulator and erases the Virtual Device, responsible for creating and starting it.
	 * 
	 * @param deviceSN
	 *        - the Serial Number of the device we want to remove completely from the Agent.
	 * @throws RemoteException
	 * @throws IOException
	 * @throws DeviceNotFoundException
	 *         - when there is no device with the given serial number
	 * @throws NotPossibleForDeviceException
	 *         - when the device, corresponding to the passed device serial number, is not an emulator and, therefore,
	 *         can not be closed
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
	 * {@link com.musala.atmosphere.commons.sa.ConsoleControl#readCommand() AgentConsole.readLine()}
	 * 
	 * @return - the first line in the console buffer as a String.
	 * @throws IOException
	 *         - when an error occurs when trying to read from console
	 */
	private String readFromConsole() throws IOException
	{
		String command = currentAgentState.readCommandFromConsole();
		return command;
	}

	/**
	 * Executes passed shell command from the user into the console of given Agent.
	 * 
	 * @param passedShellCommand
	 *        - the shell command that the managing Agent person wants to execute
	 * @return - boolean which represents if the Agent should be running if after execution of the passed command. It is
	 *         'true' if the Agent should be running, and 'false' otherwise.
	 * @throws IOException
	 */
	private void parseAndExecuteShellCommand(String passedShellCommand) throws IOException
	{
		// parsing the command where character is ' ' OR ':'
		String[] args = passedShellCommand.trim().split("[ :]");
		int numberOfParams = args.length - 1;
		String command = args[0];
		String[] params = new String[numberOfParams];

		// Copy args array in params shifted with one position.
		System.arraycopy(args, 1, params, 0, numberOfParams);

		if (!command.equals(""))
		{
			executeShellCommand(command, params);
		}
	}

	/**
	 * Evaluates passed command and calls appropriate method of the Agent.
	 * 
	 * @param commandName
	 *        - passed command for execution
	 * @param params
	 *        - arguments, passed to the command
	 * @throws RemoteException
	 * @throws AccessException
	 */
	private void executeShellCommand(String commandName, String[] params) throws AccessException, RemoteException
	{
		AgentConsoleCommands command = AgentConsoleCommands.findCommand(commandName);

		// if the command does not match any of the enum commands
		if (command == null)
		{
			currentAgentState.writeLineToConsole("No such command. Type 'help' to retrieve list of available commands.");
			return;
		}

		AgentCommand executableCommand = commandFactory.getCommandInstance(command);
		executableCommand.execute(params);
	}

	/**
	 * Sets the current state of the agent.
	 * 
	 * @param state
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
	 * Starts an Agent on localhost. It can be passed zero parameters or one parameter of type integer, which specifies
	 * on which port the Agent will be created. If passed argument could not be converted to int, or there are more
	 * arguments, the Agent will be created on default port.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		// First we check if we have been passed an argument which specifies RMI port for the Agent to be ran at.
		int portToCreateAgentOn = 0;

		try
		{
			if (args.length == 1)
			{
				String passedRmiPort = args[0];
				portToCreateAgentOn = Short.parseShort(passedRmiPort);
			}
			else
			{
				portToCreateAgentOn = AgentPropertiesLoader.getAgentRmiPort();
			}
		}
		catch (NumberFormatException e)
		{
			String exceptionMessage = "Error while trying to parse given port: argument is not valid port number.";
			LOGGER.error(exceptionMessage, e);
			throw new IllegalPortException(exceptionMessage, e);
		}

		// and then we create instance of the Agent, but without running it; the User should type "run" to run the
		// Agent
		Agent localAgent = new Agent(portToCreateAgentOn);
		localAgent.executeShellCommand(AgentConsoleCommands.AGENT_RUN.getCommand(), null);

		do
		{
			String passedShellCommand = localAgent.readFromConsole();
			localAgent.parseAndExecuteShellCommand(passedShellCommand);
		} while (!localAgent.isClosed());
	}
}
