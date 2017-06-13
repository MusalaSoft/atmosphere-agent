package com.musala.atmosphere.agent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.util.AgentIdCalculator;
import com.musala.atmosphere.agent.websocket.AgentDispatcher;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

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

    private boolean isServerSet = false;

    private AgentDispatcher webSocketCommunicator;

    /**
     * Creates a new {@link DeviceChangeHandler} that handles all on device change actions that occur.
     *
     */
    public DeviceChangeHandler() {
        AgentIdCalculator agentIdCalculator = new AgentIdCalculator();
        agentId = agentIdCalculator.getId();
        deviceManager = new DeviceManager();
        deviceManagerExecutor = new DeviceManagerExecutor();
        webSocketCommunicator = AgentDispatcher.getInstance();
    }

    /**
     * Creates a new {@link DeviceChangeHandler} that handles all on device change actions that occur.
     *
     * @param agentEventSender
     *        - agent event sender that sends events to the Server
     * @param isServerSet
     *        - the state of the Server
     */
    public DeviceChangeHandler(boolean isServerSet) {
        this();

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
     * @param deviceSerial
     *        - serial number of the changed device
     * @param connected
     *        - true if the device is now available, false if it became unavailable
     * @throws CommandFailedException
     *         if getting device's information fails
     */
    public void onDeviceListChanged(String deviceSerial, boolean connected)
            throws CommandFailedException {
        // If the server is not set return, as we have no one to notify
        if (isServerSet == false) {
            return;
        }

        webSocketCommunicator.sendConnectedDeviceInformation(agentId, deviceSerial, connected);
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
                } catch (CommandFailedException e) {
                    String errorMessage = String.format("Failed to unregister a device with serial number %s.",
                                                        device.getSerialNumber());
                    LOGGER.error(errorMessage, e);
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

            if (IDevice.PROP_BUILD_API_LEVEL != null) {
                String publishId = deviceManager.registerDevice(device);

                if (publishId != null && !publishId.isEmpty()) {
                    try {
                        onDeviceListChanged(publishId, true /* device connected */);
                    } catch (CommandFailedException e) {
                        // The exceptions are handled by the AgentEventSender
                    }
                }
            }

            currentlyHandledDevicesSet.remove(deviceSerialNumber);
        }
    }
}
