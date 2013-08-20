package com.musala.atmosphere.agent.state;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.IllegalPortException;
import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

/**
 * Common abstract class for each agent state.
 * 
 * @author nikola.taushanov
 * 
 */
public abstract class AgentState
{
	protected ConsoleControl agentConsole;

	protected AgentManager agentManager;

	protected Agent agent;

	public AgentState(Agent agent, AgentManager agentManager, ConsoleControl agentConsole)
	{
		this.agent = agent;
		this.agentManager = agentManager;
		this.agentConsole = agentConsole;
	}

	/**
	 * Gets the date and time in which the Agent was run on.
	 * 
	 * @return - the specified date
	 */
	public abstract Date getStartDate();

	/**
	 * Writes string to the agent's console output.
	 * 
	 * @param message
	 *        - the message that will be written
	 */
	public void writeToConsole(String message)
	{
		agentConsole.write(message);
	}

	/**
	 * Writes line to the agent's console output.
	 * 
	 * @param message
	 *        - the message that will be written
	 */
	public void writeLineToConsole(String message)
	{
		agentConsole.writeLine(message);
	}

	/**
	 * Gets the IP of the server which the agent is connected to.
	 * 
	 * @return - the specified IP address of the server
	 */
	public abstract String getServerIp();

	/**
	 * Gets the RMI Port which the server uses to connect to the agent.
	 * 
	 * @return - the specified RMI Port
	 */
	public abstract int getServerRmiPort();

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
	public abstract void connectToServer(String ipAddress, int port)
		throws AccessException,
			RemoteException,
			NotBoundException,
			IllegalPortException;

	/**
	 * Runs the agent.
	 */
	public abstract void run();

	/**
	 * Stops the agent thread and disconnects the Android Debug Bridge.
	 */
	public abstract void stop();

	/**
	 * Gets the RMI port of the Agent
	 * 
	 * @return - number of port, under which the Agent is published in RMI.
	 */
	public abstract int getAgentRmiPort();

	/**
	 * Gets list with all serial numbers of devices, attached to this Agent.
	 * 
	 * @return - list of Strings which are the serial numbers of the devices, attached to this Agent
	 * @throws RemoteException
	 *         - when there is some problem with the RMI connection
	 */
	public abstract List<String> getAllDevicesSerialNumbers() throws RemoteException;

	/**
	 * Gets list with all devices attached to this Agent.
	 * 
	 * @return - list of IDevices containing the attached devices.
	 */
	public abstract List<IDevice> getAllAttachedDevices();

	/**
	 * Creates and starts an emulator on this agent with desired properties.
	 * 
	 * @param parameters
	 *        - requred DeviceParameters for the emulator to have
	 * @throws IOException
	 */
	public abstract void createAndStartEmulator(DeviceParameters parameters) throws IOException;

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
	public abstract void closeEmulatorBySerialNumber(String deviceSN)
		throws RemoteException,
			NotPossibleForDeviceException,
			DeviceNotFoundException;

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
	public abstract void removeEmulatorBySerialNumber(String deviceSN)
		throws RemoteException,
			IOException,
			DeviceNotFoundException,
			NotPossibleForDeviceException;

	/**
	 * Reads one line from the agent's console.
	 * 
	 * @return - the first line in the console buffer as a String.
	 * @throws IOException
	 *         - when an error occurs when trying to read from console
	 */
	public String readCommandFromConsole() throws IOException
	{
		String command = agentConsole.readCommand();
		return command;
	}
}
