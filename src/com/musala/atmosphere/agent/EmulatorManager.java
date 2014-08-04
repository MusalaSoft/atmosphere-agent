package com.musala.atmosphere.agent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.util.PreconditionsManager;
import com.musala.atmosphere.agent.util.AndroidToolCommandBuilder;
import com.musala.atmosphere.agent.util.EmulatorToolCommandBuilder;
import com.musala.atmosphere.agent.util.SdkToolCommandSender;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.EmulatorParameters;
import com.musala.atmosphere.commons.sa.exceptions.DeviceBootTimeoutReachedException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;
import com.musala.atmosphere.commons.sa.exceptions.TimeoutReachedException;

/**
 * Manages creating, closing and erasing emulators.
 * 
 * @author georgi.gaydarov
 * 
 */
public class EmulatorManager implements IDeviceChangeListener {
    private final static Logger LOGGER = Logger.getLogger(EmulatorManager.class.getCanonicalName());

    private static final String EMULATOR_NAME_FORMAT = "AtmosphereTemporatyEmulator_%s";

    private static final int EMULATOR_WAIT_REVALIDATION_SLEEP_TIME = 1000;

    private static EmulatorManager emulatorManagerInstance = null;

    private AndroidDebugBridge androidDebugBridge;

    private SdkToolCommandSender sdkToolCommandSender;

    private Map<String, IDevice> connectedEmulatorsList = Collections.synchronizedMap(new HashMap<String, IDevice>());

    private Map<IDevice, String> connectedEmulatorNames = Collections.synchronizedMap(new HashMap<IDevice, String>());

    private Map<String, Process> startedEmulatorsProcess = Collections.synchronizedMap(new HashMap<String, Process>());

    public static EmulatorManager getInstance() {
        if (emulatorManagerInstance == null) {
            synchronized (EmulatorManager.class) {
                if (emulatorManagerInstance == null) {
                    emulatorManagerInstance = new EmulatorManager();
                    LOGGER.info("Emulator manager instance has been created.");
                }
            }
        }
        return emulatorManagerInstance;
    }

    private EmulatorManager() {
        sdkToolCommandSender = new SdkToolCommandSender();

        // Register the EmulatorManager for device change events so it can keep track of running emulators.
        AndroidDebugBridge.addDeviceChangeListener(this);

        // Get initial device list
        androidDebugBridge = AndroidDebugBridge.getBridge();
        List<IDevice> registeredDevices = getDeviceList();
        for (IDevice device : registeredDevices) {
            deviceConnected(device);
        }
    }

    /**
     * Returns the device list obtained from Android Debug Bridge.
     * 
     * @return the device list obtained from Android Debug Bridge.
     */
    private List<IDevice> getDeviceList() {
        // As the AgentmManager is calling methods in this class, it is already done with getting initial devices list.
        // This means we do not have to perform initial devices list checks and timeout loops.
        IDevice[] devicesArray = androidDebugBridge.getDevices();
        return Arrays.asList(devicesArray);
    }

    /**
     * Returns an emulator device with the given serial number.
     * 
     * @param serialNumber
     *        - serial number of the device.
     * @return an emulator device with the given serial number.
     */
    private IDevice getEmulatorBySerialNumber(String serialNumber) {
        synchronized (connectedEmulatorsList) {
            for (Entry<String, IDevice> currentEmulatorEntry : connectedEmulatorsList.entrySet()) {
                IDevice currentEmulator = currentEmulatorEntry.getValue();
                String currentEmulatorSerialNumber = currentEmulator.getSerialNumber();
                if (serialNumber.equals(currentEmulatorSerialNumber)) {
                    return currentEmulator;
                }
            }
        }
        return null;
    }

    @Override
    public void deviceChanged(IDevice arg0, int arg1) {
        // we do not care is a device state has changed, this has no impact to the emulator list we keep
    }

    @Override
    public void deviceConnected(IDevice connectedDevice) {
        if (connectedDevice.isEmulator()) {
            EmulatorConsole emulatorConsole = EmulatorConsole.getConsole(connectedDevice);
            String emulatorName = emulatorConsole.getAvdName();

            synchronized (this) {
                connectedEmulatorsList.put(emulatorName, connectedDevice);
                connectedEmulatorNames.put(connectedDevice, emulatorName);
            }

            LOGGER.info("Emulator " + emulatorName + " connected.");
        }
    }

    @Override
    public void deviceDisconnected(IDevice disconnectedDevice) {
        if (disconnectedDevice.isEmulator()) {
            String disconnectedEmulatorAvdName = null;

            synchronized (this) {
                connectedEmulatorsList.remove(disconnectedEmulatorAvdName);
                disconnectedEmulatorAvdName = connectedEmulatorNames.remove(disconnectedDevice);
                startedEmulatorsProcess.remove(disconnectedEmulatorAvdName);
            }

            String messageFormat = "Emulator %s disconnected.";
            String message = String.format(messageFormat, disconnectedEmulatorAvdName);
            LOGGER.info(message);
        }
    }

    /**
     * Returns a new unique emulator name.
     * 
     * @return a new unique emulator name.
     */
    private String getNewEmulatorName() {
        Date timeNow = new Date();
        String emulatorName = String.format(EMULATOR_NAME_FORMAT, timeNow.getTime());
        return emulatorName;
    }

    /**
     * Creates and starts a new emulator device fulfilling the desired device parameters.
     * 
     * @param desiredDeviceParameters
     *        - desired device parameters.
     * @return the name of the created emulator.
     * @throws IOException
     * @throws CommandFailedException
     */
    public String createAndStartEmulator(EmulatorParameters desiredDeviceParameters)
        throws IOException,
            CommandFailedException {
        String emulatorName = getNewEmulatorName();

        createEmulator(emulatorName, desiredDeviceParameters);
        Process startedEmulatorProcess = startEmulator(emulatorName, desiredDeviceParameters);
        startedEmulatorsProcess.put(emulatorName, startedEmulatorProcess);

        return emulatorName;
    }

    /**
     * Creates and emulator profile with the given name and device parameters.
     * 
     * @param emulatorName
     *        - the name of the emulator device.
     * @param desiredDeviceParameters
     *        - the desired device parameters.
     * @throws IOException
     * @throws CommandFailedException
     */
    private void createEmulator(String emulatorName, EmulatorParameters desiredDeviceParameters)
        throws IOException,
            CommandFailedException {
        AndroidToolCommandBuilder androidToolCommandBuilder = new AndroidToolCommandBuilder(emulatorName,
                                                                                            desiredDeviceParameters);
        String createCommand = androidToolCommandBuilder.getCreateAvdCommand();
        String createCommandReturnValue = sdkToolCommandSender.sendCommandToAndroidTool(createCommand, "\n");
        LOGGER.info("Create AVD shell command printed: " + createCommandReturnValue);
    }

    /**
     * Starts and emulator device with the given name and device parameters.
     * 
     * @param emulatorName
     *        - the name of the emulator device. Also the name of its profile.
     * @param desiredDeviceParameters
     *        - the desired device parameters.
     * @return the emulator tool process.
     * @throws IOException
     */
    private Process startEmulator(String emulatorName, EmulatorParameters desiredDeviceParameters) throws IOException {
        EmulatorToolCommandBuilder emulatorToolCommandBuilder = new EmulatorToolCommandBuilder(emulatorName,
                                                                                               desiredDeviceParameters);
        List<String> emulatorToolCommand = emulatorToolCommandBuilder.getStartCommand();
        Process startedEmulatorProcess = sdkToolCommandSender.sendCommandToEmulatorToolAndReturn(emulatorToolCommand,
                                                                                                 "");

        LOGGER.info("An emulator instance has been started.");

        return startedEmulatorProcess;

    }

    /**
     * Closes a running emulator device.
     * 
     * @param emulatorDevice
     *        - running emulator device.
     */
    private void closeEmulator(IDevice emulatorDevice) {
        EmulatorConsole emulatorConsole = EmulatorConsole.getConsole(emulatorDevice);
        emulatorConsole.kill();

        String emulatorName = emulatorDevice.getAvdName();
        // wait for the emulator to exit
        Process emulatorProcess = startedEmulatorsProcess.remove(emulatorName);
        try {
            if (emulatorProcess != null) {
                emulatorProcess.waitFor();
            }
        } catch (InterruptedException e) {
            // waiting for the emulator closing was interrupted. This can not happen?
            LOGGER.warn("Waiting for emulator to close was interrupted.", e);
        }
    }

    /**
     * Erases an emulator device profile.
     * 
     * @param emulatorDevice
     *        - existing emulator device.
     * @throws IOException
     */
    private void eraseEmulator(IDevice emulatorDevice) throws IOException {
        String emulatorName = emulatorDevice.getAvdName();
        AndroidToolCommandBuilder androidToolCommandBuilder = new AndroidToolCommandBuilder(emulatorName, null);
        String deleteAvdCommand = androidToolCommandBuilder.getDeleteAvdCommand();
        String deleteAvdCommandResponse = sdkToolCommandSender.sendCommandToAndroidTool(deleteAvdCommand, "");
        LOGGER.info("Delete AVD shell command printed: " + deleteAvdCommandResponse);
    }

    /**
     * Closes and erases a running emulator device.
     * 
     * @param serialNumber
     *        - serial number of the emulator device.
     * @throws IOException
     * @throws NotPossibleForDeviceException
     */
    public void closeAndEraseEmulator(String serialNumber) throws IOException, NotPossibleForDeviceException {
        IDevice emulatorDevice = getEmulatorBySerialNumber(serialNumber);
        if (emulatorDevice == null) {
            return;
        }
        if (!emulatorDevice.isEmulator()) {
            throw new NotPossibleForDeviceException("Cannot close and erase a real device.");
        }

        closeEmulator(emulatorDevice);
        eraseEmulator(emulatorDevice);
    }

    /**
     * Checks whether any emulator device is present on the agent.
     * 
     * @return true if an emulator device is found on the agent, false if not.
     */
    public boolean isAnyEmulatorPresent() {
        return connectedEmulatorsList.size() > 0;
    }

    /**
     * Waits until an emulator device with given AVD name is present on the agent or the timeout is reached.
     * 
     * @param emulatorName
     *        - the AVD name of the emulator.
     * @param timeout
     *        - the timeout in milliseconds.
     * @throws TimeoutReachedException
     */
    public void waitForEmulatorExists(String emulatorName, long timeout) throws TimeoutReachedException {
        while (timeout > 0) {
            IDevice emulatorDevice = connectedEmulatorsList.get(emulatorName);
            if (emulatorDevice != null) {
                return;
            }
            try {
                Thread.sleep(EMULATOR_WAIT_REVALIDATION_SLEEP_TIME);
                timeout -= EMULATOR_WAIT_REVALIDATION_SLEEP_TIME;
            } catch (InterruptedException e) {
                // Nothing to do here.
            }
        }

        throw new TimeoutReachedException("Timeout was reached and an emulator with name " + emulatorName
                + " is still not present.");
    }

    /**
     * Gets the serial number of an emulator with given AVD name.
     * 
     * @param emulatorName
     *        - the AVD name of the emulator.
     * @return the serial number of the emulator.
     * @throws DeviceNotFoundException
     */
    public String getSerialNumberOfEmulator(String emulatorName) throws DeviceNotFoundException {
        IDevice desiredDevice = connectedEmulatorsList.get(emulatorName);
        if (desiredDevice == null) {
            throw new DeviceNotFoundException("Emulator with name" + emulatorName + " is not present on the agent.");
        }
        return desiredDevice.getSerialNumber();
    }

    /**
     * Waits until an emulator device with given AVD name boots or the timeout is reached. Make sure you have called
     * {@link #waitForEmulatorExists(String, long)} first.
     * 
     * @param emulatorName
     *        - the AVD name of the emulator.
     * @param timeout
     *        - the timeout in milliseconds.
     * @throws CommandFailedException
     * @throws DeviceBootTimeoutReachedException
     * @throws DeviceNotFoundException
     */
    public void waitForEmulatorToBoot(String emulatorName, long timeout)
        throws CommandFailedException,
            DeviceBootTimeoutReachedException,
            DeviceNotFoundException {
        IDevice desiredEmulator = connectedEmulatorsList.get(emulatorName);
        if (desiredEmulator == null) {
            throw new DeviceNotFoundException("Emulator with name " + emulatorName + " is not present on the agent.");
        }
        PreconditionsManager preconditionsManager = new PreconditionsManager(desiredEmulator);
        preconditionsManager.waitForDeviceToBoot(timeout);
    }
}
