// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.agent;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriverService;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.EmulatorWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.IWrapDevice;
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
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.agent.util.FileRecycler;
import com.musala.atmosphere.agent.util.FtpConnectionManager;
import com.musala.atmosphere.agent.util.FtpFileTransferService;
import com.musala.atmosphere.agent.util.FtpServerPropertiesLoader;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.ad.service.ConnectionConstants;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.exceptions.NoAvailableDeviceFoundException;
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

    private static final int BOOT_VALIDATION_TIMEOUT = 120000;

    private static final int DEVICE_EXISTANCE_CHECK_TIMEOUT = 1000;

    private static final int ATMOSPHERE_MIN_ALLOWED_API_LEVEL = 17;

    private static final int NO_AVAIBLE_API_LEVEL = -1;

    private static final int FTP_TRANSFER_SERVICE_DELAY = 3;

    private static final String QUEUE_FILE_NAME = "pending_transfers.txt";

    private static String agentId;

    private static AndroidDebugBridgeManager androidDebugBridgeManager;

    private static final int THREAD_COUNT = 20;

    private static ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    /**
     * Maps a device serial number to a {@link connectedDevicesList} device
     */
    private static volatile Map<String, IDevice> connectedDevicesList = new HashMap<>();

    private ChromeDriverService chromeDriverService;

    private static FileRecycler fileRecycler;

    private static FtpFileTransferService ftpFileTransferService;

    private static ScheduledExecutorService fileTransferServiceScheduler;

    /**
     * Maps a device serial number to a {@link IWrapDevice} device wrapper
     */
    private static Map<String, IWrapDevice> deviceSerialToDeviceWrapper = new HashMap<>();

    public DeviceManager() {
    }

    public DeviceManager(FileRecycler fileRecycler) {
        if (AgentPropertiesLoader.hasFtpServer() && ftpFileTransferService == null) {
            boolean isSecuredFtp = FtpServerPropertiesLoader.isFtps();
            FtpConnectionManager ftpConnectionManager = new FtpConnectionManager(isSecuredFtp);
            ftpConnectionManager.connectToFtpServer();

            fileTransferServiceScheduler = Executors.newSingleThreadScheduledExecutor();

            try {
                ftpFileTransferService = new FtpFileTransferService(QUEUE_FILE_NAME, ftpConnectionManager);

                fileTransferServiceScheduler.scheduleAtFixedRate(ftpFileTransferService,
                                                                 0,
                                                                 FTP_TRANSFER_SERVICE_DELAY,
                                                                 TimeUnit.SECONDS);
            } catch (IOException e) {
                LOGGER.error("The FTP file transfer service failed to initialize.", e);
            }
        }

        if (androidDebugBridgeManager == null) {
            androidDebugBridgeManager = new AndroidDebugBridgeManager();

            AgentIdCalculator agentIdCalculator = new AgentIdCalculator();
            agentId = agentIdCalculator.getId();

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
            androidDebugBridgeManager.setListener(new DeviceChangeListener(false));

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
                        } catch (CommandFailedException e) {
                            String sendingEventFailedMessage = String.format("Sending device list change event to the Server failed for device [%s]",
                                                                             initialDevice.getSerialNumber());
                            LOGGER.error(sendingEventFailedMessage, e);
                        }
                    }
                });
            }

            String version = AgentPropertiesLoader.getChromeDriverVersion();
            ChromeDriverManager.getInstance().setup(version);

            chromeDriverService = ChromeDriverService.createDefaultService();
            try {
                chromeDriverService.start();
            } catch (IOException e) {
                String startChromeDriverServiceFailedMessage = String.format("Starting %s failed.",
                                                                             ChromeDriverService.class.getName());
                LOGGER.error(startChromeDriverServiceFailedMessage, e);
            }

            DeviceManager.fileRecycler = fileRecycler;
            LOGGER.info("Device manager created successfully.");

            deviceManagerExecutor.releaseResourcesAwaitTermination();
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
     * Gets a list of all serial numbers of the published and available device on the current Agent.
     *
     * @return List of the serial numbers of the devices on the current Agent.
     *
     */
    public List<String> getAllDeviceSerialNumbers() {
        List<String> serialNumbersList = new LinkedList<>();

        synchronized (connectedDevicesList) {
            for (Entry<String, IDevice> deviceEntry : connectedDevicesList.entrySet()) {
                IDevice currentDevice = deviceEntry.getValue();
                String serialNumber = getSerialNumber(currentDevice);
                serialNumbersList.add(serialNumber);
            }
        }

        return serialNumbersList;
    }

    public List<DeviceInformation> getDevicesInformation() {
        List<DeviceInformation> wrappersList = new LinkedList<>();

        synchronized (connectedDevicesList) {
            for (Entry<String, IDevice> deviceEntry : connectedDevicesList.entrySet()) {
                String deviceId = deviceEntry.getKey();
                IWrapDevice wrapDevice = getDeviceWrapperByDeviceId(deviceId);
                try {
                    wrappersList.add((DeviceInformation) wrapDevice.route(RoutingAction.GET_DEVICE_INFORMATION));
                } catch (CommandFailedException e) {
                    String message = String.format("Getting the information from a device with id %s failed.",
                                                   deviceId);
                    LOGGER.error(message, e);
                }
            }
        }

        return wrappersList;
    }

    /**
     * Registers a newly connected device on this AgentManager (adds it to the internal list of devices and creates a
     * wrapper). Gets invoked by the {@link DeviceChangeHandler}.
     *
     * @param connectedDevice
     *        the newly connected device.
     * @return the identifier of the newly bound wrapper, i. e. the serial number.
     */
    String registerDevice(IDevice connectedDevice) {
        String connectedDeviceSerialNumber = connectedDevice.getSerialNumber();
        if (connectedDevicesList.containsKey(connectedDeviceSerialNumber)) {
            // The device is already registered, nothing to do here.
            // This should not normally happen!
            LOGGER.warn("Trying to register a device that is already registered.");
            return null;
        }

        String apiLevelString = connectedDevice.getProperty(IDevice.PROP_BUILD_API_LEVEL);
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
            String wrapperId = createWrapperForDevice(connectedDevice);
            connectedDevicesList.put(connectedDeviceSerialNumber, connectedDevice);
            return wrapperId;
        } catch (OnDeviceComponentCommunicationException e) {
            LOGGER.fatal("Could not create a wrapper for a device", e);
        }
        return null;
    }

    /**
     * Unregisters a disconnected device on this AgentManager (removes it from the internal list of devices). 
     * Gets invoked by the {@link DeviceChangeHandler}.
     *
     * @param disconnectedDevice
     *        the disconnected device.
     * @return the wrapper identifier of the device.
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

        String publishId = releaseWrapperForDevice(disconnectedDevice);
        connectedDevicesList.remove(disconnectedDeviceSerialNumber);

        return publishId;
    }

    /**
     * Creates a wrapper for a device with specific serial number.
     *
     * @param device
     *        that will be wrapped.
     * @return an identifier for the newly created wrapper.
     *
     */
    private String createWrapperForDevice(IDevice device) {
        IWrapDevice deviceWrapper = null;
        String wrapperSerial = getSerialNumber(device);

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
                                                       fileRecycler,
                                                       ftpFileTransferService);
            } else {
                deviceWrapper = new RealWrapDevice(device,
                                                   executor,
                                                   shellCommandExecutor,
                                                   serviceCommunicator,
                                                   automatorCommunicator,
                                                   chromeDriverService,
                                                   fileRecycler,
                                                   ftpFileTransferService);
            }
        } catch (NotPossibleForDeviceException e) {
            // Not really possible as we have just checked.
            // Nothing to do here.
            LOGGER.error("Failed to create wrapper for device with serialNumber = " + wrapperSerial, e);
        }

        deviceSerialToDeviceWrapper.put(serialNumber, deviceWrapper);
        LOGGER.info("Created wrapper for device with serialNumber = " + wrapperSerial);

        return wrapperSerial;
    }

    /**
     * Returns a unique identifier for this device.
     *
     * @param device
     *        which we want to get unique identifier for.
     * @return unique identifier for the device.
     */
    public String getSerialNumber(IDevice device) {
        String wrapperId = device.getSerialNumber();
        return wrapperId;
    }

    /**
     * Release a device wrapper
     *
     * @param device
     *        the device with the wrapper to be released.
     * @return identifier of the wrapper.
     *
     */
    private String releaseWrapperForDevice(IDevice device) {
        // IWrapDevice deviceWrapper = getDeviceWrapper(wrapperId);
        /*
         * TODO: The method 'unbindWrapper()' fails because the physical device is missing (if is disconnected manually
         * by removing the USB cable). Find an another solution for future work. Maybe is a good idea to stop the
         * onDeviceComponents from the service.
         */
        // deviceWrapper.unbindWrapper();
        String wrapperId = getSerialNumber(device);
        LOGGER.info("Released wrapper for device with Id [" + wrapperId + "].");

        return wrapperId;
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
     *         - thrown when the device is not found after the given timeout
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
     *
     */
    public IWrapDevice getFirstAvailableDeviceWrapper() {
        List<String> wrapperIdentifiers = getAllDeviceSerialNumbers();

        if (wrapperIdentifiers.isEmpty()) {
            throw new NoAvailableDeviceFoundException("No devices are present on the current agent. Consider creating and starting an emulator.");
        }
        IWrapDevice deviceWrapper = getDeviceWrapperByDeviceId(wrapperIdentifiers.get(0));

        return deviceWrapper;
    }

    /**
     * Gets the first available emulator device that is present on the agent (current machine).
     *
     * @return the first available emulator wrapper ({@link IWrapDevice} interface).
     *
     */
    public IWrapDevice getFirstAvailableEmulatorDeviceWrapper() {
        // TODO: Move to EmulatorManager.
        List<String> wrapperIdentifiers = getAllDeviceSerialNumbers();

        for (String wrapperId : wrapperIdentifiers) {
            IWrapDevice deviceWrapper = getDeviceWrapperByDeviceId(wrapperId);
            DeviceInformation deviceInformation;
            try {
                deviceInformation = (DeviceInformation) deviceWrapper.route(RoutingAction.GET_DEVICE_INFORMATION);
                if (deviceInformation.isEmulator()) {
                    return deviceWrapper;
                }
            } catch (CommandFailedException e) {
                LOGGER.error("Getting the emulator wrapper failed.", e);
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

    public void stopFtpFileTransferService() {
        ftpFileTransferService.stop();
        fileTransferServiceScheduler.shutdown();
    }

    /**
     * Gets the wrapper of a device with the given Id.
     *
     * @param deviceId
     *        - id of the wrapper we want to get.
     * @return {@link IWrapDevice} of a device containing given id.
     *
     */
    public IWrapDevice getDeviceWrapperByDeviceId(String deviceId) {
        return deviceSerialToDeviceWrapper.get(deviceId);
    }
}
