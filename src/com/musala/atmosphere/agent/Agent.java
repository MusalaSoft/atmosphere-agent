package com.musala.atmosphere.agent;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

/**
 * This class creates Agent objects which can be manipulated from the user.
 * 
 * @author vladimir.vladimirov
 * 
 */
public class Agent
{
	private static final Logger LOGGER = Logger.getLogger(Agent.class.getCanonicalName());

	private AgentManager agentManager;

	private AgentConsole agentConsole;

	private AgentState agentState;

	private Thread agentThread;

	private int agentRmiPort;

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
		agentState = AgentState.AGENT_CREATED;
		this.agentRmiPort = agentRmiPort;
		LOGGER.info("Agent created on port: " + agentRmiPort);
		agentConsole = new AgentConsole();
	}

	/**
	 * Starts the Agent thread.
	 * 
	 */
	public void startAgentThread()
	{
		InnerRunThread innerThread = new InnerRunThread();
		agentThread = new Thread(innerThread, "AgentRunningWaitThread");
		// agentState = AgentState.AGENT_RUNNING;
		agentThread.start();

		try
		{
			agentManager = new AgentManager(AgentPropertiesLoader.getPathToADB(), agentRmiPort);
		}
		catch (RemoteException e)
		{
			stop();
			LOGGER.fatal("Error in RMI connection", e);
			throw new RuntimeException("Agent could not be created", e);
		}
		catch (ADBridgeFailException e)
		{
			stop();
			LOGGER.fatal("Error while creating AgentManager", e);
			throw new RuntimeException("Agent could not be created", e);
		}
	}

	private class InnerRunThread implements Runnable
	{
		/**
		 * Runs the Agent on localhost and port <i>agentRmiPort</i>.
		 */
		@Override
		public void run()
		{
			agentState = AgentState.AGENT_RUNNING;

			LOGGER.info("Running agent...");

			try
			{
				// waiting for action requests
				while (!isStopped())
				{
					Thread.sleep(1000);
				}
			}
			catch (InterruptedException e)
			{
				LOGGER.fatal("Something has interrupted the current thread.", e);
				// Thread.currentThread().interrupt();
			}
			finally
			{
				if (!isStopped())
				{
					stop();
					LOGGER.warn("Closed agent on address [localhost:" + agentRmiPort + "].");
				}
			}
		}
	}

	/**
	 * Stops the agent thread and disconnects the Android Debug Bridge.
	 * 
	 * @throws InterruptedException
	 *         - this exception occurs when some other thread interrupts the thread in which Agent methods are running.
	 */
	public void stop()
	{
		if (agentState != AgentState.AGENT_STOPPED)
		{
			if (agentManager != null)
			{
				agentManager.close();
			}
			agentState = AgentState.AGENT_STOPPED;
		}
		else
		{
			LOGGER.info("The agent is not running.");
		}
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
		List<String> deviceSerialNumbers = agentManager.getAllDeviceWrappers();
		return deviceSerialNumbers;
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
		agentManager.createAndStartEmulator(parameters);
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
		agentManager.closeEmulator(deviceSN);
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
		agentManager.eraseEmulator(deviceSN);
	}

	/**
	 * Checks whether the Agent has been stopped.
	 * 
	 * @return - true, if agent state is <b><i>AGENT_STOPPED</i></b>, and false if agent state is
	 *         <b><i>AGENT_CREATED</i></b> or <b><i>AGENT_RUNNING</i></b>.
	 */
	public boolean isStopped()
	{
		boolean isAgentStopped = (agentState == AgentState.AGENT_STOPPED);
		return isAgentStopped;
	}

	/**
	 * Reads one line from the agent's console. For more information see
	 * {@link com.musala.atmosphere.agent.AgentConsole#readLine() AgentConsole.readLine()}
	 * 
	 * @return - the first line in the console buffer as a String.
	 * @throws IOException
	 *         - when an error occurs when trying to read from console
	 */
	private String readCommandFromConsole() throws IOException
	{
		String command = agentConsole.readLine();
		return command;
	}

	/**
	 * Executes passed shell command from the user into the console of given Agent.
	 * 
	 * @param passedShellCommand
	 *        - the shell command that the managing Agent person wants to execute
	 * @return - boolean which represents if the Agent should be running if after execution of the passed command. It is
	 *         'true' if the Agent should be running, and 'false' otherwise.
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void parseAndExecuteShellCommand(String passedShellCommand) throws IOException
	{
		// parsing the command where character is ' ' OR ':'
		String[] args = passedShellCommand.trim().split("[ :]");
		int numberOfarguments = args.length - 1;
		if (numberOfarguments < 0)
		{
			LOGGER.error("Empty command or invalid command parsing!");
			return;
		}
		String command = args[0];
		String[] params = new String[numberOfarguments];
		for (int indexOfAdditionalArgument = 0; indexOfAdditionalArgument < numberOfarguments; indexOfAdditionalArgument++)
		{
			// we exclude the first argument of the parsed command, because it is the command itself
			params[indexOfAdditionalArgument] = args[indexOfAdditionalArgument + 1];
		}

		// executing the command
		executeShellCommand(command, params);
	}

	/**
	 * Evaluates passed command and calls appropriate method of the Agent.
	 * 
	 * @param commandString
	 *        - passed command for execution
	 * @param params
	 *        - arguments, passed to the command
	 * @throws InterruptedException
	 * @throws RemoteException
	 * @throws AccessException
	 * @throws IOException
	 */
	private void executeShellCommand(String commandString, String[] params) throws AccessException, RemoteException
	{
		AgentConsoleCommands command = null;
		// Enum.valueOf(String command) doesn't work as expected here
		for (AgentConsoleCommands possibleCommand : AgentConsoleCommands.values())
		{
			String possibleCommandString = possibleCommand.getCommand();
			if (possibleCommandString.equalsIgnoreCase(commandString))
			{
				command = possibleCommand;
				break;
			}
		}

		// if the command does not match any of the enum commands
		if (command == null)
		{
			if (!commandString.isEmpty())
			{
				agentConsole.writeLine("No such command. Type 'help' to retrieve list of available commands.");
			}
			return;
		}

		// evaluate the command
		switch (command)
		{
			case AGENT_RUN:
			{
				executeCommandRun(params);
				break;
			}
			case AGENT_CONNECT:
			{
				executeCommandConnect(params);
				break;
			}
			case AGENT_HELP:
			{
				executeCommandHelp(params);
				break;
			}
			case AGENT_STOP:
			{
				executeCommandStop(params);
				break;
			}
		}

	}

	/**
	 * Executes the command for running agent.
	 * 
	 * @param params
	 */
	private void executeCommandRun(String[] params)
	{
		if (params.length != 0)
		{
			agentConsole.writeLine("Command '" + AgentConsoleCommands.AGENT_RUN.getCommand()
					+ "' requires no arguments. Type '" + AgentConsoleCommands.AGENT_HELP.getCommand()
					+ "' to retrieve list of available commands.");
		}
		else
		{
			// we should start the agent if it's instantiated
			if (agentState == AgentState.AGENT_CREATED)
			{
				startAgentThread();
			}
			else
			{
				// agentState cannot be AGENT_STOPPED and we already checked the case of AGENT_CREATED, so the only
				// available option here is AGENT_RUNNING
				agentConsole.writeLine("Cannot run agent: agent already running.");
			}
		}
	}

	/**
	 * Connects the agent to given Server.
	 * 
	 * @param params
	 *        - should be array of type: { serverIp, serverPort } or {serverPort} in which case the serverIp will be
	 *        assumed to be "localhost"
	 * @throws RemoteException
	 * @throws AccessException
	 */
	private void executeCommandConnect(String[] params) throws AccessException, RemoteException
	{
		if (agentState == AgentState.AGENT_CREATED)
		{
			agentConsole.writeLine("Agent not running to be connected. Run the Agent first.");
			return;
		}
		String serverIp = "localhost";
		String serverPortAsString = null;
		switch (params.length)
		{
			case 2:
			{
				serverIp = params[0];
				serverPortAsString = params[1];
				break;
			}
			case 1:
			{
				serverPortAsString = params[0];
				break;
			}
			default:
			{
				agentConsole.writeLine("Bad arguments for command '" + AgentConsoleCommands.AGENT_CONNECT.getCommand()
						+ ". Type '" + AgentConsoleCommands.AGENT_HELP.getCommand()
						+ "' to retrieve list of available commands.");
				return;
			}
		}

		try
		{
			int serverPort = Integer.parseInt(serverPortAsString);
			if (isPortValueValid(serverPort))
			{
				agentManager.connectToServer(serverIp, serverPort);
			}
			else
			{
				LOGGER.error("Invalid port value: port must be number in the interval ["
						+ AgentPropertiesLoader.getRmiMinimalPortValue() + "; "
						+ AgentPropertiesLoader.getRmiMaximalPortValue() + "].");
			}
		}
		catch (NumberFormatException e)
		{
			LOGGER.error("Passed server port is not a number!");
			return;
		}
		catch (NotBoundException e)
		{
			LOGGER.error("No server is running.", e);
		}
		catch (UnknownHostException e)
		{
			LOGGER.error("Your IP is invalid.", e);
		}
		catch (ConnectException e)
		{
			LOGGER.error("IP is ok, but port is not. Make sure the port value is the same as the Server's port.", e);
		}

	}

	/**
	 * Evaluates command for printing information about console commands.
	 * 
	 * @param params
	 *        - passed arguments to the command
	 */
	private void executeCommandHelp(String[] params)
	{
		if (params.length != 0)
		{
			agentConsole.writeLine("Command '" + AgentConsoleCommands.AGENT_HELP.getCommand()
					+ "' requires no arguments. Type '" + AgentConsoleCommands.AGENT_HELP.getCommand()
					+ "' to retrieve list of available commands.");
		}
		else
		{
			List<String> listOfCommands = AgentConsoleCommands.getListOfCommands();
			for (String agentConsoleCommand : listOfCommands)
			{
				agentConsole.writeLine(agentConsoleCommand);
			}
		}
	}

	/**
	 * Executes stopping command for the Agent with given parameters.
	 * 
	 * @param params
	 *        - passed arguments to the command
	 * @throws IOException
	 */
	private void executeCommandStop(String[] params)
	{
		if (params.length != 0)
		{
			agentConsole.writeLine("Command '" + AgentConsoleCommands.AGENT_STOP.getCommand()
					+ "' requires no arguments. Type '" + AgentConsoleCommands.AGENT_HELP.getCommand()
					+ "' to retrieve list of available commands.");
		}
		else
		{
			// our agent can be stopped only once; so if it is not in running state, it must be only created and
			// not started
			if (agentState != AgentState.AGENT_RUNNING)
			{
				agentConsole.writeLine("Cannot stop agent: agent is not running to be stopped. To run it, type \"run\".");
			}
			else
			{
				this.stop();
			}
		}
	}

	/**
	 * Checks whether given RMi port value is valid.
	 * 
	 * @param rmiPort
	 *        - int that represents the port value
	 * @return - true, if the passed rmi port value is valid, and false otherwise
	 */
	public static boolean isPortValueValid(int rmiPort)
	{
		boolean isPortOk = (rmiPort >= AgentPropertiesLoader.getRmiMinimalPortValue() && rmiPort <= AgentPropertiesLoader.getRmiMaximalPortValue());
		return isPortOk;
	}

	/**
	 * Starts an Agent on localhost. It can be passed zero parameters or one parameter of type integer, which specifies
	 * on which port the Agent will be created. If passed argument could not be converted to int, or there are more
	 * arguments, the Agent will be created on default port.
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		// First we check if we have been passed an argument which specifies RMI port for the Agent to be ran at.
		int portToCreateAgentOn = AgentPropertiesLoader.getAgentRmiPort();
		if (args.length == 1)
		{
			String passedRmiPort = args[0];
			try
			{
				portToCreateAgentOn = Integer.parseInt(passedRmiPort);
			}
			catch (NumberFormatException e)
			{
				LOGGER.warn("Error while trying to parse given port: argument is not a number.", e);
			}
		}

		// check if passed port value is valid ( should be in the range [0;65535] )
		if (isPortValueValid(portToCreateAgentOn) == false)
		{
			portToCreateAgentOn = AgentPropertiesLoader.getAgentRmiPort();
			LOGGER.warn("Invalid port: port must be number in the interval ["
					+ AgentPropertiesLoader.getRmiMinimalPortValue() + "; "
					+ AgentPropertiesLoader.getRmiMaximalPortValue() + "]. Creating agent on default RMI port- "
					+ portToCreateAgentOn);
		}

		// and then we create instance of the Agent, but without running it; the User should type "run" to run the
		// Agent
		Agent localAgent = new Agent(portToCreateAgentOn);
		do
		{
			String passedShellCommand = localAgent.readCommandFromConsole();
			localAgent.parseAndExecuteShellCommand(passedShellCommand);
		} while (localAgent.isStopped() == false);
	}
}
