package com.musala.atmosphere.agent;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.commons.sa.IAgentEventSender;
import com.musala.atmosphere.commons.sa.RmiStringConstants;

/**
 * A class to handle ADB's device list changed events
 * 
 * @author georgi.gaydarov
 * 
 */
class DeviceChangeListener implements IDeviceChangeListener
{
	private AgentManager agentManagerRefference;

	private String serverIPAddress;

	private int serverRmiPort;

	private String agentId;

	private IAgentEventSender agentEventSender;

	private boolean isServerSet = false;

	/**
	 * <p>
	 * Creates a new DeviceChangeListener that sends events to the server when something in the devices list has changed
	 * and updates the list of devices on the agent.
	 * </p>
	 * 
	 * @param serverIPAddress
	 *        IP address of the server's RMI registry.
	 * @param serverRmiPort
	 *        Port on which the server's RMI registry is opened.
	 * @param agentId
	 *        ID that will be passed to the server, used for identifying the Agent from which the event came.
	 * @param devicesListRefference
	 *        A reference to the devices list of the AgentManager, so it can be updated when a device is added/removed.
	 * @throws RemoteException
	 *         When connecting to the server fails or the AgentEventSender could not be found.
	 */
	public DeviceChangeListener(String serverIPAddress,
			int serverRmiPort,
			String agentId,
			AgentManager agentManagerRefference) throws RemoteException
	{
		this.serverIPAddress = serverIPAddress;
		this.serverRmiPort = serverRmiPort;
		this.agentId = agentId;
		this.agentManagerRefference = agentManagerRefference;

		// If the server is set, get it's AgentEventSender so we can notify the server about changes in the device list.
		if (serverIPAddress.isEmpty() || serverIPAddress == null)
			return;

		isServerSet = true;
		try
		{
			// Get the registry on the server
			Registry serverRegistry = LocateRegistry.getRegistry(serverIPAddress, serverRmiPort);

			// Search for the AgentEventSender in the server's registry
			agentEventSender = (IAgentEventSender) serverRegistry.lookup(RmiStringConstants.AGENT_EVENT_SENDER.toString());
		}
		catch (RemoteException e)
		{
			// We could not get the registry on the server or we could not connect to it at all.
			throw e;
		}
		catch (NotBoundException e)
		{
			// The server has not published an AgentEventSender in it's registry under the constant
			// specified by StringConstants.AGENT_EVENT_SENDER_RMI.
			throw new RemoteException("AgentEventSender is not bound in the target RMI registry.", e);
		}
	}

	/**
	 * <p>
	 * Creates a new DeviceChangeListener that only updates a devices list and does not notify any server. Same as
	 * constructing with serverIPAddress = "".
	 * </p>
	 * 
	 * @param devicesListReference
	 * @throws RemoteException
	 */
	public DeviceChangeListener(AgentManager agentManagerRefference) throws RemoteException
	{
		this("", 0, "", agentManagerRefference);
	}

	/**
	 * Gets called when a device's state has changed.
	 */
	@Override
	public void deviceChanged(IDevice device, int changeMask)
	{
		// device is which device has changed, changeMask is what exactly changed in it
		// we are not using these arguments, as the AgentManager constructs it's
		// own DeviceInformation structures and doesn't care about the information in changeMask
		onDeviceListChanged();
	}

	/**
	 * Gets called when a device is connected to the computer.
	 */
	@Override
	public void deviceConnected(IDevice connectedDevice)
	{
		// Register the newly connected device on the AgentManager
		agentManagerRefference.registerDeviceOnAgent(connectedDevice);

		onDeviceListChanged();
	}

	/**
	 * Gets called when a device is disconnected from the computer.
	 */
	@Override
	public void deviceDisconnected(IDevice disconnectedDevice)
	{
		// Unregister the device from the AgentManager
		agentManagerRefference.unregisterDeviceOnAgent(disconnectedDevice);

		onDeviceListChanged();
	}

	/**
	 * Gets called when something in the device list has changed.
	 */
	private void onDeviceListChanged()
	{
		// If the server is not set return, as we have no one to notify
		if (isServerSet == false)
		{
			return;
		}

		// Else, notify the server using it's AgentEventSender
		try
		{
			agentEventSender.deviceListChanged(agentId);
		}
		catch (RemoteException e)
		{
			// We could not notify the server, maybe the connection was lost
			e.printStackTrace();
			// TODO what should we do now?
			// Try reconnecting maybe?
		}
	}
}