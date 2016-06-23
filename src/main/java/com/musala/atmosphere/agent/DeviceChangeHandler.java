package com.musala.atmosphere.agent;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.util.AgentIdCalculator;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.IAgentEventSender;

/**
 * Handles all on device change actions that occur, such as connect and disconnect device.
 * 
 * @author denis.bialev
 *
 */
public class DeviceChangeHandler {

    private final static Logger LOGGER = Logger.getLogger(DeviceChangeListener.class.getCanonicalName());

    private static Set<String> currentlyHandledDevicesSet = Collections.synchronizedSet(new HashSet<String>());

    DeviceManagerExecutor deviceManagerExecutor;

    private final DeviceManager deviceManager;

    private final String agentId;

    private IAgentEventSender agentEventSender;

    private boolean isServerSet = false;

    /**
     * Creates a new {@link DeviceChangeHandler} that handles all on device change actions that occur.
     * 
     * @throws RemoteException
     *         if sending of a event to the server fails
     */
    public DeviceChangeHandler() throws RemoteException {
        AgentIdCalculator agentIdCalculator = new AgentIdCalculator();
        agentId = agentIdCalculator.getId();
        deviceManager = new DeviceManager();
        deviceManagerExecutor = new DeviceManagerExecutor();
    }

    /**
     * Creates a new {@link DeviceChangeHandler} that handles all on device change actions that occur.
     * 
     * @param agentEventSender
     *        - agent event sender that sends events to the Server
     * @param isServerSet
     *        - the state of the Server
     * @throws RemoteException
     *         if sending of the event to the server fails
     */
    public DeviceChangeHandler(IAgentEventSender agentEventSender, boolean isServerSet) throws RemoteException {
        this();

        this.agentEventSender = agentEventSender;
        this.isServerSet = isServerSet;
    }

    /**
     * Handles the on device changed action according to it's type.
     * 
     * @param action
     *        - type of action that will be handled
     * @param device
     *        - on which the on device changed action occurred
     */
    public void handleAction(DeviceChangeAction action, IDevice device) {
        Runnable runnableTask;
        switch (action) {
            case CONNECT_DEVICE:
                runnableTask = new DeviceConnectManager(device);
                break;

            case DISCONNECT_DEVICE:
                runnableTask = new DeviceDisconnectManager(device);
                break;

            default:
                return;
        }

        deviceManagerExecutor.execute(runnableTask);
    }

    /**
     * Gets called when something in the device list has changed.
     *
     * @param deviceRmiBindingId
     *        - RMI binding ID of the changed device's wrapper
     * @param connected
     *        - true if the device is now available, false if it became unavailable
     * @throws NotBoundException
     *         if an attempt is made to operate with non-existing device
     * @throws CommandFailedException
     *         if getting device's information fails
     */
    public void onDeviceListChanged(String deviceRmiBindingId, boolean connected)
            throws CommandFailedException,
            NotBoundException {
        // TODO: In future we can use AgentEventSender like sendEvent(Event event, parameters)
        // If the server is not set return, as we have no one to notify
        if (isServerSet == false) {
            return;
        }

        try {
            agentEventSender.deviceListChanged(agentId, deviceRmiBindingId, connected);
        } catch (RemoteException e) {
            LOGGER.warn("Sending onDeviceListChanged event to the Server failed.", e);
        }
    }

    /**
     * Responsible for disconnecting the device from the Agent when device disconnected event is received.
     * 
     * @author denis.bialev
     *
     */
    private class DeviceDisconnectManager implements Runnable {

        private final IDevice device;

        private final String deviceSerialNumber;

        DeviceDisconnectManager(IDevice disconnectedDevice) {
            this.device = disconnectedDevice;
            this.deviceSerialNumber = disconnectedDevice.getSerialNumber();
        }

        @Override
        public void run() {

            // Unregister the device from the AgentManager
            String publishId = deviceManager.unregisterDevice(device);
            if (publishId != null && !publishId.isEmpty()) {
                try {
                    onDeviceListChanged(publishId, false /* device disconnected */);
                } catch (CommandFailedException | NotBoundException e) {

                }
            }

            currentlyHandledDevicesSet.remove(deviceSerialNumber);
        }
    }

    /**
     * Responsible for connecting device to the Agent when device connected event is received.
     * 
     * @author denis.bialev
     *
     */
    private class DeviceConnectManager implements Runnable {
        private final IDevice device;

        private final String deviceSerialNumber;

        DeviceConnectManager(IDevice connectedDevice) {
            this.device = connectedDevice;
            this.deviceSerialNumber = connectedDevice.getSerialNumber();
        }

        @Override
        public void run() {
            if (device.isOffline()) {
                // If the device is offline, we have no use for it, so we don't have to register it.
                return;
            }

            if (!currentlyHandledDevicesSet.contains(deviceSerialNumber)) {
                synchronized (currentlyHandledDevicesSet) {
                    if (!currentlyHandledDevicesSet.contains(deviceSerialNumber)) {
                        currentlyHandledDevicesSet.add(deviceSerialNumber);
                    }
                }
            }

            if (device.PROP_BUILD_API_LEVEL != null) {
                String publishId = deviceManager.registerDevice(device);

                if (publishId != null && !publishId.isEmpty()) {
                    try {
                        onDeviceListChanged(publishId, true /* device connected */);
                    } catch (CommandFailedException | NotBoundException e) {
                        // The exceptions are handled by the AgentEventSender
                    }
                }
            }

            currentlyHandledDevicesSet.remove(deviceSerialNumber);
        }
    }
}
