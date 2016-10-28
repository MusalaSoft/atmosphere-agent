package com.musala.atmosphere.agent;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriverService;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.AbstractWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.EmulatorWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.RealWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.util.BackgroundShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.devicewrapper.util.PreconditionsManager;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceRequestSender;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorRequestSender;
import com.musala.atmosphere.agent.exception.ForwardingPortFailedException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentCommunicationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentInitializationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentStartingException;
import com.musala.atmosphere.agent.util.AgentIdCalculator;
import com.musala.atmosphere.agent.util.FileRecycler;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.ad.service.ConnectionConstants;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceBootTimeoutReachedException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;
import com.musala.atmosphere.commons.sa.exceptions.TimeoutReachedException;

import io.github.bonigarcia.wdm.ChromeDriverManager;

/**
 * Manages wrapping, unwrapping, register and unregister devices. Keeps track of all connected devices.
 *
 * @author yordan.petrov
 *
 */
public class DeviceManager {
    private static final Logger LOGGER = Logger.getLogger(DeviceManager.class.getCanonicalName());

    private static final String CURRENT_DIR = System.getProperty("user.dir");

    private static final int BOOT_VALIDATION_TIMEOUT = 120000;

    private static final int DEVICE_EXISTANCE_CHECK_TIMEOUT = 1000;

    private static final int ATMOSPHERE_MIN_ALLOWED_API_LEVEL = 17;

    private static final int NO_AVAIBLE_API_LEVEL = -1;

    private static String agentId;

    private static AndroidDebugBridgeManager androidDebugBridgeManager;

    private static final int THREAD_COUNT = 20;

    private static ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    private static Registry rmiRegistry;

    private static int rmiRegistryPort;

    private static volatile Map<String, IDevice> connectedDevicesList = new HashMap<>();

    private ChromeDriverService chromeDriverService;

    private FileRecycler fileRecycler;

    public DeviceManager() {
    }

    public DeviceManager(int rmiPort, FileRecycler fileRecycler) throws RemoteException {
        if (androidDebugBridgeManager == null) {
            rmiRegistryPort = rmiPort;
            androidDebugBridgeManager = new AndroidDebugBridgeManager();

            AgentIdCalculator agentIdCalculator = new AgentIdCalculator();
            agentId = agentIdCalculator.getId();

            // Publish this AgentManager in the RMI registry
            try {
                rmiRegistry = LocateRegistry.getRegistry(rmiPort);
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

            // Parallel wrapping of the devices that are currently connected to ADB.
            DeviceManagerExecutor deviceManagerExecutor = new DeviceManagerExecutor();

            for (final IDevice initialDevice : initialDevicesList) {
                deviceManagerExecutor.execute(new Runnable() {

                    @Override
                    public void run() {
                        registerDevice(initialDevice);
                        try {
                            DeviceChangeListener deviceChangeListener = androidDebugBridgeManager.getCurrentListener();
                            deviceChangeListener.onDeviceListChanged(initialDevice, true);
                        } catch (CommandFailedException | NotBoundException e) {
                            String sendingEventFailedMessage = String.format("Sending device list change event to the Server failed for device [%s]",
                                                                             initialDevice.getSerialNumber());
                            LOGGER.error(sendingEventFailedMessage, e);
                        }
                    }
                });
            }

            deviceManagerExecutor.releaseResources();

            ChromeDriverManager.getInstance().setup();
            chromeDriverService = ChromeDriverService.createDefaultService();
            try {
                chromeDriverService.start();
            } catch (IOException e) {
                String startChromeDriverServiceFailedMessage = String.format("Starting %s failed.",
                                                                             ChromeDriverService.class.getName());
                LOGGER.error(startChromeDriverServiceFailedMessage, e);
            }

            this.fileRecycler = fileRecycler;
            LOGGER.info("Device manager created successfully.");
        }
    }

    /**
     * Gets list of all connected {@link IDevice}.
     *
     * @return list of all connected {@link IDevice}.
     */
    public List<IDevice> getDevicesList() {
        Collection<IDevice> connectedDevices = connectedDevicesList.values();
        List<IDevice> deviceList = new LinkedList<>();
        deviceList.addAll(connectedDevices);
        return deviceList;
    }

    /**
     * Gets the unique identifier of the current Agent.
     *
     * @return Unique identifier for the current Agent.
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * Gets a list of all published and available device wrapper RMI string identifiers on the current Agent.
     *
     * @return List of the DeviceInformation objects, one for every available device on the current Agent.
     * @throws RemoteException
     */
    public List<String> getAllDeviceRmiIdentifiers() throws RemoteException {
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

        Map<String, String> devicePropertiesMap = connectedDevice.getProperties();
        String apiLevelString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_API_LEVEL.toString());
        int deviceApiLevel = NO_AVAIBLE_API_LEVEL;
        if (apiLevelString != null) {
            deviceApiLevel = Integer.parseInt(apiLevelString);
        }

        if (deviceApiLevel < ATMOSPHERE_MIN_ALLOWED_API_LEVEL) {
            String notCompatibleDeviceMessage = String.format("Device [%s] is not compatible with ATMOSPHERE. Device's API level needs to be %d or higher. ON THREAD: %s",
                                                              connectedDeviceSerialNumber,
                                                              ATMOSPHERE_MIN_ALLOWED_API_LEVEL,
                                                              Thread.currentThread().getName());
            LOGGER.info(notCompatibleDeviceMessage);
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

        String serialNumber = device.getSerialNumber();

        BackgroundShellCommandExecutor shellCommandExecutor = new BackgroundShellCommandExecutor(device, executor);

        ServiceCommunicator serviceCommunicator = null;
        UIAutomatorCommunicator automatorCommunicator = null;
        try {
            PortForwardingService serviceForwardingService = new PortForwardingService(device,
                                                                                       ConnectionConstants.SERVICE_PORT);
            serviceForwardingService.forwardPort();
            ServiceRequestSender serviceRequestSender = new ServiceRequestSender(serviceForwardingService);
            serviceCommunicator = new ServiceCommunicator(serviceRequestSender, shellCommandExecutor, serialNumber);
            serviceCommunicator.startComponent();
            serviceCommunicator.validateRemoteServer();

            PortForwardingService automatorForwardingService = new PortForwardingService(device,
                                                                                         ConnectionConstants.UI_AUTOMATOR_PORT);
            automatorForwardingService.forwardPort();
            UIAutomatorRequestSender automatorRequestSender = new UIAutomatorRequestSender(automatorForwardingService);
            automatorCommunicator = new UIAutomatorCommunicator(automatorRequestSender,
                                                                shellCommandExecutor,
                                                                serialNumber);
            automatorCommunicator.startComponent();
            automatorCommunicator.validateRemoteServer();

        } catch (ForwardingPortFailedException | OnDeviceComponentStartingException
                | OnDeviceComponentInitializationException e) {
            String errorMessage = String.format("Could not initialize communication to a on-device component for %s.",
                                                serialNumber);
            throw new OnDeviceComponentCommunicationException(errorMessage, e);
        }

        // Create a device wrapper depending on the device type (emulator/real)
        try {
            if (device.isEmulator()) {
                deviceWrapper = new EmulatorWrapDevice(device,
                                                       executor,
                                                       shellCommandExecutor,
                                                       serviceCommunicator,
                                                       automatorCommunicator,
                                                       chromeDriverService,
                                                       fileRecycler);
            } else {
                deviceWrapper = new RealWrapDevice(device,
                                                   executor,
                                                   shellCommandExecutor,
                                                   serviceCommunicator,
                                                   automatorCommunicator,
                                                   chromeDriverService,
                                                   fileRecycler);
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
     * Returns a unique identifier for this device, which will be used as a publishing string for the wrapper of the
     * device in RMI.
     *
     * @param deviceSerialNumber
     *        serial numver of the device we want to get unique identifier for.
     * @return unique identifier for the device.
     * @throws DeviceNotFoundException
     */
    public String getRmiWrapperBindingIdentifier(String deviceSerialNumber) throws DeviceNotFoundException {
        IDevice device = getDeviceBySerialNumber(deviceSerialNumber);
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
            IWrapDevice deviceWrapper = getDeviceWrapper(rmiWrapperBindingId);
            deviceWrapper.unbindWrapper();

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

    /**
     * Waits until a device with given serial number is present on the agent or the timeout is reached.
     *
     * @param serialNumber
     *        - the serial number of the device.
     * @param timeout
     *        - the timeout in milliseconds.
     * @throws TimeoutReachedException
     */
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

    /**
     * Checks if any device is present on the agent (current machine).
     *
     * @return true if a device is present, false otherwise.
     */
    public boolean isAnyDevicePresent() {
        return !connectedDevicesList.isEmpty();
    }

    /**
     * Gets the first available device that is present on the agent (current machine).
     *
     * @return the first available device wrapper ({@link IWrapDevice} interface).
     * @throws RemoteException
     * @throws NotBoundException
     */
    public IWrapDevice getFirstAvailableDeviceWrapper() throws RemoteException, NotBoundException {
        List<String> wrapperIdentifiers = getAllDeviceRmiIdentifiers();

        if (wrapperIdentifiers.isEmpty()) {
            throw new NoAvailableDeviceFoundException("No devices are present on the current agent. Consider creating and starting an emulator.");
        }
        IWrapDevice deviceWrapper = (IWrapDevice) rmiRegistry.lookup(wrapperIdentifiers.get(0));
        return deviceWrapper;
    }

    /**
     * Gets the first available emulator device that is present on the agent (current machine).
     *
     * @return the first available emulator wrapper ({@link IWrapDevice} interface).
     * @throws RemoteException
     * @throws NotBoundException
     */
    public IWrapDevice getFirstAvailableEmulatorDeviceWrapper() throws RemoteException, NotBoundException {
        // TODO: Move to EmulatorManager.
        List<String> wrapperIdentifiers = getAllDeviceRmiIdentifiers();

        for (String wrapperId : wrapperIdentifiers) {
            AbstractWrapDevice deviceWrapper = (AbstractWrapDevice) rmiRegistry.lookup(wrapperId);
            DeviceInformation deviceInformation = deviceWrapper.getDeviceInformation();

            if (deviceInformation.isEmulator()) {
                return deviceWrapper;
            }
        }
        throw new NoAvailableDeviceFoundException("No emulator devices are present on the agent (current machine).");
    }

    /**
     * Stops the chrome driver started as a service.
     *
     */
    public void stopChromeDriverService() {
        chromeDriverService.stop();
    }

    /**
     * Gets the wrapper of a device with the given Id.
     *
     * @param wrapperId
     *        - id of the wrapper we want to get.
     * @return {@link IWrapDevice} of a device containing given id.
     * @throws AccessException
     *         if the caller does not have the needed permission
     * @throws RemoteException
     *         if accessing the lookup method fails
     * @throws NotBoundException
     *         if the attempted lookup for the wrapper id has no associated binding
     */
    private IWrapDevice getDeviceWrapper(String wrapperId) throws AccessException, RemoteException, NotBoundException {
        IWrapDevice deviceWrapper = (IWrapDevice) rmiRegistry.lookup(wrapperId);
        return deviceWrapper;
    }
}
