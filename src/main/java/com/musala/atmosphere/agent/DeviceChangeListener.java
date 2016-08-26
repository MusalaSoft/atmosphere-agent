package com.musala.atmosphere.agent;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.IAgentEventSender;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;

/**
 * A class to register ADB's device list changed events
 * 
 * @author georgi.gaydarov
 * 
 */
class DeviceChangeListener implements IDeviceChangeListener {
    private final DeviceManager deviceManager;

    private IAgentEventSender agentEventSender;

    private boolean isServerSet = false;

    private DeviceChangeHandler deviceChangeHandler;

    /**
     * 
     * @throws RemoteException
     */
    public DeviceChangeListener() throws RemoteException {
        deviceChangeHandler = new DeviceChangeHandler();
        deviceManager = new DeviceManager();
    }

    /**
     * <p>
     * Creates a new DeviceChangeListener that sends events to the server when something in the devices list has changed
     * and updates the list of devices on the agent.
     * </p>
     * 
     * @param serverIPAddress
     *        - IP address of the server's RMI registry
     * @param serverRmiPort
     *        - Port on which the server's RMI registry is opened
     * @throws RemoteException
     *         When connecting to the server fails or the AgentEventSender could not be found
     * @throws ADBridgeFailException
     *         when adb connection fails
     */
    public DeviceChangeListener(String serverIPAddress, int serverRmiPort) throws RemoteException {
        this();

        // If the server is set, get it's AgentEventSender so we can notify the server about changes in the device list.
        if (serverIPAddress.isEmpty() || serverIPAddress == null) {
            return;
        }

        isServerSet = true;
        try {
            // Get the registry on the server
            Registry serverRegistry = LocateRegistry.getRegistry(serverIPAddress, serverRmiPort);

            // Search for the AgentEventSender in the server's registry
            agentEventSender = (IAgentEventSender) serverRegistry.lookup(RmiStringConstants.AGENT_EVENT_SENDER.toString());
            deviceChangeHandler = new DeviceChangeHandler(agentEventSender, isServerSet);
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
     * Gets called when a device's state, properties or client has changed.
     */
    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        switch (changeMask) {
            case IDevice.CHANGE_STATE:
                if (device.isOnline()) {
                    deviceConnected(device);
                } else if (device.isOffline()) {
                    deviceDisconnected(device);
                }

                break;
            case IDevice.CHANGE_BUILD_INFO:
                updateDevice(device);

                break;
            default:
                break;
        }
    }

    /**
     * Gets called when a device is connected to the Android Debug Bridge.
     */
    @Override
    public void deviceConnected(IDevice connectedDevice) {
        deviceChangeHandler.handleAction(DeviceChangeAction.CONNECT_DEVICE, connectedDevice);
    }

    /**
     * Gets called when a device is disconnected from the Android Debug Bridge.
     */
    @Override
    public void deviceDisconnected(IDevice disconnectedDevice) {
        deviceChangeHandler.handleAction(DeviceChangeAction.DISCONNECT_DEVICE, disconnectedDevice);
    }

    /**
     * Sends event to the server in order to inform it for device list change.
     * 
     * @param device
     *        - on which the change has occurred
     * @param connected
     *        - <code>true</code> if the device has been connected and <code>false</code> if it has been disconnected
     * @throws NotBoundException
     *         if an attempt is made to operate with non-existing device
     * @throws CommandFailedException
     *         if getting device's information fails
     */
    public void onDeviceListChanged(IDevice device, boolean connected) throws CommandFailedException, NotBoundException {
        deviceChangeHandler.onDeviceListChanged(device.getSerialNumber(), connected);
    }

    /**
     * Gets called when device's information is changed.
     * 
     * @param device
     *        - on which the change has occurred
     */
    private void updateDevice(IDevice device) {
        if (!isDeviceRegistered(device)) {
            deviceConnected(device);
        }
    }

    /**
     * Checks whether this device is already registered on the agent.
     *
     * @param device
     *        - a device to look for in the registered devices
     * @return <code>true</code> if the device is registered and <code>false</code> otherwise.
     */
    private boolean isDeviceRegistered(IDevice device) {
        try {
            String deviceRmiId = device.getSerialNumber();
            return deviceManager.getAllDeviceRmiIdentifiers().contains(deviceRmiId);
        } catch (RemoteException e) {
            return false;
        }
    }
}
