package com.musala.atmosphere.agent;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.EmulatorWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.RealWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.util.PreconditionsManager;
import com.musala.atmosphere.agent.exception.OnDeviceComponentCommunicationException;
import com.musala.atmosphere.agent.util.AgentIdCalculator;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.IDeviceManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceBootTimeoutReachedException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;
import com.musala.atmosphere.commons.sa.exceptions.TimeoutReachedException;

/**
 * Manages wrapping, unwrapping, register and unregister devices. Keeps track of all connected devices.
 * 
 * @author yordan.petrov
 * 
 */
public class DeviceManager extends UnicastRemoteObject implements IDeviceManager {
    private static final long serialVersionUID = 8664090887381892035L;

    private final static Logger LOGGER = Logger.getLogger(DeviceManager.class.getCanonicalName());

    private static final int BOOT_VALIDATION_TIMEOUT = 120000;

    private final static int DEVICE_EXISTANCE_CHECK_TIMEOUT = 1000;

    private static String agentID;

    private static AndroidDebugBridgeManager androidDebugBridgeManager;

    private static Registry rmiRegistry;

    private static int rmiRegistryPort;

    private static volatile Map<String, IDevice> connectedDevicesList = Collections.synchronizedMap(new HashMap<String, IDevice>());

    public DeviceManager() throws RemoteException {
    }

    public DeviceManager(int rmiPort) throws RemoteException {
        if (androidDebugBridgeManager == null) {
            rmiRegistryPort = rmiPort;
            androidDebugBridgeManager = new AndroidDebugBridgeManager();

            AgentIdCalculator agentIdCalculator = new AgentIdCalculator();
            agentID = agentIdCalculator.getId();

            // Publish this AgentManager in the RMI registry
            try {
                rmiRegistry = LocateRegistry.getRegistry(rmiPort);
                rmiRegistry.rebind(RmiStringConstants.DEVICE_MANAGER.toString(), this);
            } catch (RemoteException e) {
                throw e;
            }

            // Get the initial devices list
            List<IDevice> initialDevicesList = null;
            try {
                initialDevicesList = androidDebugBridgeManager.getInitialDeviceList();
            } catch (ADBridgeFailException e) {
                LOGGER.fatal("Getting initial device list failed.", e);
            }
            LOGGER.info("Initial device list fetched containing " + initialDevicesList.size() + " devices.");

            // Set up a device change listener that will update this agent, but not connect and notify any server.
            // Server connection will be established later, when a server registers itself.
            androidDebugBridgeManager.setListener(new DeviceChangeListener());

            // Register initial device list.
            for (IDevice initialDevice : initialDevicesList) {
                registerDevice(initialDevice);
            }

            LOGGER.info("Device manager created successfully.");
        }
    }

    public List<IDevice> getDevicesList() {
        Collection<IDevice> connectedDevices = connectedDevicesList.values();
        List<IDevice> deviceList = new LinkedList<IDevice>();
        deviceList.addAll(connectedDevices);
        return deviceList;
    }

    @Override
    public String getAgentId() {
        return agentID;
    }

    @Override
    public List<String> getAllDeviceWrappers() throws RemoteException {
        List<String> wrappersList = new LinkedList<>();

        synchronized (connectedDevicesList) {
            for (Entry<String, IDevice> deviceEntry : connectedDevicesList.entrySet()) {
                IDevice currentDevice = deviceEntry.getValue();
                String rmiWrapperBindingId = getRmiWrapperBindingIdentifier(currentDevice);
                wrappersList.add(rmiWrapperBindingId);
            }
        }

        return wrappersList;
    }

    /**
     * Registers a newly connected device on this AgentManager (adds it to the internal list of devices and creates a
     * wrapper for it in the RMI registry). Gets invoked by the DeviceChangeListener.
     * 
     * @param connectedDevice
     *        the newly connected device.
     * @return the RMI binding ID of the newly bound wrapper.
     */
    String registerDevice(IDevice connectedDevice) {
        String connectedDeviceSerialNumber = connectedDevice.getSerialNumber();
        if (connectedDevicesList.containsKey(connectedDeviceSerialNumber)) {
            // The device is already registered, nothing to do here.
            // This should not normally happen!
            LOGGER.warn("Trying to register a device that is already registered.");
            return null;
        }

        try {
            String publishId = createWrapperForDevice(connectedDevice);
            connectedDevicesList.put(connectedDeviceSerialNumber, connectedDevice);
            return publishId;
        } catch (RemoteException | OnDeviceComponentCommunicationException e) {
            LOGGER.fatal("Could not publish a wrapper for a device in the RMI registry.", e);
        }
        return null;
    }

    /**
     * Unregisters a disconnected device on this AgentManager (removes it from the internal list of devices and unbinds
     * it's wrapper from the RMI registry). Gets invoked by the DeviceChangeListener.
     * 
     * @param disconnectedDevice
     *        the disconnected device.
     * @return The RMI binding ID of the unbound device.
     */
    String unregisterDevice(IDevice disconnectedDevice) {
        String disconnectedDeviceSerialNumber = disconnectedDevice.getSerialNumber();
        if (!connectedDevicesList.containsKey(disconnectedDeviceSerialNumber)) {
            // The device was never registered, so nothing to do here.
            // This should not normally happen!
            LOGGER.warn("Trying to unregister a device [" + disconnectedDeviceSerialNumber
                    + "] that was not present in the devices list.");
            return null;
        }

        try {
            String publishId = unbindWrapperForDevice(disconnectedDevice);
            connectedDevicesList.remove(disconnectedDeviceSerialNumber);
            return publishId;
        } catch (RemoteException e) {
            LOGGER.error("Device wrapper unbinding failed.", e);
        }
        return null;
    }

    /**
     * Creates a wrapper for a device with specific serial number.
     * 
     * @param device
     *        that will be wrapped.
     * @return RMI binding ID for the newly created wrapper.
     * @throws RemoteException
     */
    private String createWrapperForDevice(IDevice device) throws RemoteException {
        IWrapDevice deviceWrapper = null;
        String rmiWrapperBindingId = getRmiWrapperBindingIdentifier(device);

        PreconditionsManager preconditionsManager = new PreconditionsManager(device);
        try {
            preconditionsManager.waitForDeviceToBoot(BOOT_VALIDATION_TIMEOUT);
        } catch (CommandFailedException | DeviceBootTimeoutReachedException e) {
            LOGGER.warn("Could not ensure device " + device.getSerialNumber() + " has fully booted.", e);
        }
        preconditionsManager.manageOnDeviceComponents();

        // Create a device wrapper depending on the device type (emulator/real)
        try {
            if (device.isEmulator()) {
                deviceWrapper = new EmulatorWrapDevice(device);
            } else {
                deviceWrapper = new RealWrapDevice(device);
            }
        } catch (NotPossibleForDeviceException e) {
            // Not really possible as we have just checked.
            // Nothing to do here.
            e.printStackTrace();
        }

        rmiRegistry.rebind(rmiWrapperBindingId, deviceWrapper);
        LOGGER.info("Created wrapper for device with bindingId = " + rmiWrapperBindingId);

        return rmiWrapperBindingId;
    }

    /**
     * Returns a unique identifier for this device, which will be used as a publishing string for the wrapper of the
     * device in RMI.
     * 
     * @param device
     *        which we want to get unique identifier for.
     * @return unique identifier for the device.
     */
    public String getRmiWrapperBindingIdentifier(IDevice device) {
        String wrapperId = device.getSerialNumber();
        return wrapperId;
    }

    /**
     * Unbinds a device wrapper from the RMI registry.
     * 
     * @param device
     *        the device with the wrapper to be removed.
     * @return the RMI binding ID of the unbound wrapper.
     * @throws RemoteException
     */
    private String unbindWrapperForDevice(IDevice device) throws RemoteException {
        String rmiWrapperBindingId = getRmiWrapperBindingIdentifier(device);

        try {
            rmiRegistry.unbind(rmiWrapperBindingId);
        } catch (NotBoundException e) {
            // Wrapper for the device was never published, so we have nothing to unbind.
            // Nothing to do here.
            LOGGER.error("Unbinding device wrapper [" + rmiWrapperBindingId + "] failed.", e);
            return null;
        } catch (AccessException e) {
            throw new RemoteException("Unbinding device wrapper [" + rmiWrapperBindingId + "] failed.", e);
        }

        LOGGER.info("Removed wrapper for device with bindingId [" + rmiWrapperBindingId + "].");

        return rmiWrapperBindingId;
    }

    /**
     * Returns an IDevice by it's specified serial number.
     * 
     * @param serialNumber
     *        Serial number of the wanted IDevice.
     * @return The IDevice.
     * @throws DeviceNotFoundException
     *         If no device with serial number serialNumber is found.
     */
    IDevice getDeviceBySerialNumber(String serialNumber) throws DeviceNotFoundException {
        IDevice desiredDevice = connectedDevicesList.get(serialNumber);
        if (desiredDevice != null) {
            return desiredDevice;
        }
        throw new DeviceNotFoundException("Device with serial number " + serialNumber + " not found on this agent.");
    }

    @Override
    public void waitForDeviceExists(String serialNumber, long timeout) throws TimeoutReachedException {
        while (timeout > 0) {
            try {
                getDeviceBySerialNumber(serialNumber);
                return;
            } catch (DeviceNotFoundException e) {
                try {
                    Thread.sleep(DEVICE_EXISTANCE_CHECK_TIMEOUT);
                    timeout -= DEVICE_EXISTANCE_CHECK_TIMEOUT;
                } catch (InterruptedException e1) {
                    // Nothing to do here.
                }
            }
        }

        throw new TimeoutReachedException("Timeout was reached and a device with serial number " + serialNumber
                + " is still not present.");
    }
}
