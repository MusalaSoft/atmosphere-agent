package com.musala.atmosphere.agent;

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * A class to register ADB's device list changed events
 *
 * @author georgi.gaydarov
 *
 */
class DeviceChangeListener implements IDeviceChangeListener {
    private final DeviceManager deviceManager;

    private DeviceChangeHandler deviceChangeHandler;

    /**
     * <p>
     * Creates a new DeviceChangeListener that sends events to the server when something in the devices list has changed
     * and updates the list of devices on the agent.
     * </p>
     *
     * @param isConnectedToServer
     *        - whether the agent is connected to a server
     */
    public DeviceChangeListener(boolean isConnectedToServer) {
        deviceChangeHandler = new DeviceChangeHandler();
        deviceManager = new DeviceManager();

        // If the server is set, we can notify the server about changes in the device list.
        if (isConnectedToServer) {
            deviceChangeHandler = new DeviceChangeHandler(isConnectedToServer);
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
     * @throws CommandFailedException
     *         if getting device's information fails
     */
    public void onDeviceListChanged(IDevice device, boolean connected)
        throws CommandFailedException {
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
        String deviceSerial = device.getSerialNumber();
        return deviceManager.getAllDeviceSerialNumbers().contains(deviceSerial);
    }
}
