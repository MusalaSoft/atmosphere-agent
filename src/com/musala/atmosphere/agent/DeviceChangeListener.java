package com.musala.atmosphere.agent;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.util.AgentIdCalculator;
import com.musala.atmosphere.commons.sa.IAgentEventSender;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;

/**
 * A class to handle ADB's device list changed events
 * 
 * @author georgi.gaydarov
 * 
 */
class DeviceChangeListener implements IDeviceChangeListener {
    private DeviceManager deviceManager;

    private String agentId;

    private IAgentEventSender agentEventSender;

    private boolean isServerSet = false;

    public DeviceChangeListener() throws RemoteException {
        AgentIdCalculator agentIdCalculator = new AgentIdCalculator();
        agentId = agentIdCalculator.getId();
        deviceManager = new DeviceManager();
    }

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
     * @throws RemoteException
     *         When connecting to the server fails or the AgentEventSender could not be found.
     * @throws ADBridgeFailException
     */
    public DeviceChangeListener(String serverIPAddress, int serverRmiPort) throws RemoteException {
        this();

        // If the server is set, get it's AgentEventSender so we can notify the server about changes in the device list.
        if (serverIPAddress.isEmpty() || serverIPAddress == null)
            return;

        isServerSet = true;
        try {
            // Get the registry on the server
            Registry serverRegistry = LocateRegistry.getRegistry(serverIPAddress, serverRmiPort);

            // Search for the AgentEventSender in the server's registry
            agentEventSender = (IAgentEventSender) serverRegistry.lookup(RmiStringConstants.AGENT_EVENT_SENDER.toString());
        } catch (RemoteException e) {
            // We could not get the registry on the server or we could not connect to it at all.
            throw e;
        } catch (NotBoundException e) {
            // The server has not published an AgentEventSender in it's registry under the constant
            // specified by StringConstants.AGENT_EVENT_SENDER_RMI.
            throw new RemoteException("AgentEventSender is not bound in the target RMI registry.", e);
        }
    }

    /**
     * Gets called when a device's state has changed.
     */
    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        // device is which device has changed, changeMask is what exactly changed in it
        // If the device became online, we can now use it.
        // If the device became offline, we can no longer use it.
        if (changeMask == IDevice.CHANGE_STATE) {
            if (device.isOnline()) {
                deviceConnected(device);
            } else if (device.isOffline()) {
                deviceDisconnected(device);
            }
        }
    }

    /**
     * Gets called when a device is connected to the computer.
     */
    @Override
    public void deviceConnected(IDevice connectedDevice) {
        if (connectedDevice.isOffline()) {
            // If the device is offline, we have no use for it, so we don't have to register it.
            return;
        }

        // Register the newly connected device on the AgentManager
        String publishId = deviceManager.registerDevice(connectedDevice);
        if (publishId != null && !publishId.isEmpty()) {
            onDeviceListChanged(publishId, true /* device connected */);
        }
    }

    /**
     * Gets called when a device is disconnected from the computer.
     */
    @Override
    public void deviceDisconnected(IDevice disconnectedDevice) {
        // Unregister the device from the AgentManager
        String publishId = deviceManager.unregisterDevice(disconnectedDevice);
        if (publishId != null && !publishId.isEmpty()) {
            onDeviceListChanged(publishId, false /* device disconnected */);
        }
    }

    /**
     * Gets called when something in the device list has changed.
     * 
     * @param deviceRmiBindingId
     *        - RMI binding ID of the changed device's wrapper.
     * @param connected
     *        - true if the device is now available, false if it became unavailable.
     */
    private void onDeviceListChanged(String deviceRmiBindingId, boolean connected) {
        // If the server is not set return, as we have no one to notify
        if (isServerSet == false) {
            return;
        }

        // Else, notify the server using it's AgentEventSender
        try {
            agentEventSender.deviceListChanged(agentId, deviceRmiBindingId, connected);
        } catch (RemoteException e) {
            // We could not notify the server, maybe the connection was lost
            e.printStackTrace();
            // TODO what should we do now?
            // Try reconnecting maybe?
        }
    }

}
