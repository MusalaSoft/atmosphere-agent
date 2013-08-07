package com.musala.atmosphere.agent;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.command.AgentCommand;
import com.musala.atmosphere.agent.command.AgentCommandFactory;
import com.musala.atmosphere.agent.command.AgentConsoleCommands;
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

	private AgentCommandFactory commandFactory;

	/**
	 * Creates an Agent object on the default agent rmi port ( whose value can be seen in the agent.properties file
	 * under the name ).
	 */
	public Agent()
	{
		this(AgentPropertiesLoader.getAgentRmiPort());
		commandFactory = new AgentCommandFactory(this);
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
		commandFactory = new AgentCommandFactory(this);
	}

	public AgentState getAgentState()
	{
		return agentState;
	}

	public void writeToConsole(String message)
	{
		agentConsole.write(message);
	}

	public void writeLineToConsole(String message)
	{
		agentConsole.writeLine(message);
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
		agentManager.connectToServer(ipAddress, port);
	}

	/**
	 * Starts the Agent thread.
	 * 
	 */
	public void startAgentThread()
	{
		InnerRunThread innerThread = new InnerRunThread();
		agentThread = new Thread(innerThread, "AgentRunningWaitThread");
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
			agentConsole.writeLine("No such command. Type 'help' to retrieve list of available commands.");
			return;
		}

		AgentCommand executableCommand = commandFactory.getCommandInstance(command);
		executableCommand.execute(params);
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

		String passedRmiPort = args[0];
		try
		{
			if (args.length == 1)
			{
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
		do
		{
			String passedShellCommand = localAgent.readCommandFromConsole();
			localAgent.parseAndExecuteShellCommand(passedShellCommand);
		} while (localAgent.isStopped() == false);
	}
}
