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
	 * @return the specified date.
	 */
	public abstract Date getStartDate();

	/**
	 * Prints a string to the Agent's console output.
	 * 
	 * @param message
	 *        - the message to be printed.
	 */
	public void writeToConsole(String message)
	{
		agentConsole.write(message);
	}

	/**
	 * Prints a line to the Agent's console output.
	 * 
	 * @param message
	 *        - the message to be printed.
	 */
	public void writeLineToConsole(String message)
	{
		agentConsole.writeLine(message);
	}

	/**
	 * Gets the IP of the server to which the agent is currently connected to.
	 * 
	 * @return the current server's IP address.
	 */
	public abstract String getServerIp();

	/**
	 * Gets the port of the server to which the agent is currently connected to.
	 * 
	 * @return - the current server's port.
	 */
	public abstract int getServerRmiPort();

	/**
	 * Connects this Agent to a Server.
	 * 
	 * @param ipAddress
	 *        - server's IP address.
	 * @param port
	 *        - server's port.
	 * @throws NotBoundException
	 * @throws RemoteException
	 * @throws AccessException
	 */
	public abstract void connectToServer(String ipAddress, int port)
		throws AccessException,
			RemoteException,
			NotBoundException;

	/**
	 * Runs the Agent.
	 */
	public abstract void run();

	/**
	 * Stops the agent thread and releases the Android Debug Bridge.
	 */
	public abstract void stop();

	/**
	 * Gets the Agent's port.
	 * 
	 * @return Agent's RMI port.
	 */
	public abstract int getAgentRmiPort();

	/**
	 * Gets a list of all attached devices' serial numbers.
	 * 
	 * @return list of Strings that represent device serial numbers.
	 * @throws RemoteException
	 */
	public abstract List<String> getAllDevicesSerialNumbers();

	/**
	 * Gets list of all devices attached to this Agent.
	 * 
	 * @return list of {@link IDevice IDevice} objects.
	 */
	public abstract List<IDevice> getAllAttachedDevices();

	/**
	 * Creates and starts an emulator with specified properties on the current Agent.
	 * 
	 * @param parameters
	 *        - parameters to be passed to the emulator creation procedure.
	 * @throws IOException
	 */
	public abstract void createAndStartEmulator(DeviceParameters parameters) throws IOException;

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
	 * */
	public abstract void removeEmulatorBySerialNumber(String deviceSN)
		throws RemoteException,
			IOException,
			DeviceNotFoundException,
			NotPossibleForDeviceException;

	/**
	 * Reads one line from the Agent's console.
	 * 
	 * @return the first line in the console buffer as a String.
	 * @throws IOException
	 *         - when a console reading error occurs.
	 */
	public String readCommandFromConsole() throws IOException
	{
		String command = agentConsole.readCommand();
		return command;
	}
}
