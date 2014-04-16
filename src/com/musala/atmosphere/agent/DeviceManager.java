package com.musala.atmosphere.agent;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.EmulatorWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.RealWrapDevice;
import com.musala.atmosphere.agent.exception.OnDeviceComponentCommunicationException;
import com.musala.atmosphere.agent.util.AgentIdCalculator;
import com.musala.atmosphere.commons.sa.IDeviceManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

/**
 * Manages wrapping, unwrapping, register and unregister devices. Keeps track of all connected devices.
 * 
 * @author yordan.petrov
 * 
 */
public class DeviceManager extends UnicastRemoteObject implements IDeviceManager {
    private static final long serialVersionUID = 8664090887381892035L;

    private final static Logger LOGGER = Logger.getLogger(DeviceManager.class.getCanonicalName());

    private static String agentID;

    private static AndroidDebugBridgeManager androidDebugBridgeManager;

    private static Registry rmiRegistry;

    private static int rmiRegistryPort;

    private static volatile List<IDevice> devicesList = Collections.synchronizedList(new LinkedList<IDevice>());

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
        return devicesList;
    }

    @Override
    public String getAgentId() {
        return agentID;
    }

    @Override
    public List<String> getAllDeviceWrappers() throws RemoteException {
        List<String> wrappersList = new LinkedList<>();

        synchronized (devicesList) {
            for (IDevice device : devicesList) {
                String rmiWrapperBindingId = getRmiWrapperBindingIdentifier(device);
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
        if (devicesList.contains(connectedDevice)) {
            // The device is already registered, nothing to do here.
            // This should not normally happen!
            LOGGER.warn("Trying to register a device that is already registered.");
            return "";
        }

        try {
            String publishId = createWrapperForDevice(connectedDevice);
            devicesList.add(connectedDevice);
            return publishId;
        } catch (RemoteException | OnDeviceComponentCommunicationException e) {
            LOGGER.fatal("Could not publish a wrapper for a device in the RMI registry.", e);
        }
        return "";
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
        if (!devicesList.contains(disconnectedDevice)) {
            // The device was never registered, so nothing to do here.
            // This should not normally happen!
            LOGGER.warn("Trying to unregister a device [" + disconnectedDevice.getSerialNumber()
                    + "] that was not present in the devices list.");
            return "";
        }

        try {
            String publishId = unbindWrapperForDevice(disconnectedDevice);
            devicesList.remove(disconnectedDevice);
            return publishId;
        } catch (RemoteException e) {
            LOGGER.error("Device wrapper unbinding failed.", e);
        }
        return "";
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
            return "";
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
        synchronized (devicesList) {
            for (IDevice device : devicesList) {
                if (device.getSerialNumber().equals(serialNumber)) {
                    return device;
                }
            }
        }
        throw new DeviceNotFoundException("Device with serial number " + serialNumber + " not found on this agent.");
    }

}
