package com.musala.atmosphere.agent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.websocket.DeploymentException;

import org.apache.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.musala.atmosphere.agent.devicewrapper.IWrapDevice;
import com.musala.atmosphere.agent.exception.IllegalPortException;
import com.musala.atmosphere.agent.util.AgentIdCalculator;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.agent.util.FileRecycler;
import com.musala.atmosphere.agent.util.SystemSpecificationLoader;
import com.musala.atmosphere.agent.websocket.AgentWebSocketDispatcher;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.EmulatorParameters;
import com.musala.atmosphere.commons.sa.SystemSpecification;
import com.musala.atmosphere.commons.sa.exceptions.DeviceBootTimeoutReachedException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;
import com.musala.atmosphere.commons.sa.exceptions.TimeoutReachedException;

/**
 * Used for managing all devices on the current Agent.
 *
 * @author georgi.gaydarov
 *
 */
public class AgentManager {
    private static final Logger LOGGER = Logger.getLogger(AgentManager.class.getCanonicalName());

    private AndroidDebugBridgeManager androidDebugBridgeManager;

    private EmulatorManager emulatorManager;

    private DeviceManager deviceManager;

    private final String agentId;

    private SystemSpecificationLoader systemSpecificationLoader;

    private AgentWebSocketDispatcher dispatcher;

    /**
     * Creates a new AgentManager on this computer.
     *
     * @param rmiPort
     *        Port, which will be used for the RMI Registry
     * @param fileRecycler
     *        {@link FileRecycler FileRecycler} object
     */
    public AgentManager(FileRecycler fileRecycler) {
        systemSpecificationLoader = new SystemSpecificationLoader();
        systemSpecificationLoader.getSpecification();
        androidDebugBridgeManager = new AndroidDebugBridgeManager();
        emulatorManager = EmulatorManager.getInstance();

        // Calculate the current Agent ID.
        AgentIdCalculator agentIdCalculator = new AgentIdCalculator();
        agentId = agentIdCalculator.getId();

        deviceManager = new DeviceManager(fileRecycler);

        dispatcher = AgentWebSocketDispatcher.getInstance();
        dispatcher.setDeviceManager(deviceManager);
        dispatcher.setAgentManager(this);

        LOGGER.info("AgentManager created successfully.");
    }

    /**
     * Calls the {@link #close() close()} method just to be sure everything is closed.
     */
    @Override
    public void finalize() {
        close();
    }

    /**
     * Closes all open resources. <b>MUST BE CALLED WHEN THIS CLASS IS NO LONGER NEEDED.</b>
     */
    public void close() {
        LOGGER.info("Closing the AgentManager.");
        try {
            // We close the bridge and adb service, so bridge creation wont fail next time we try. This is a workaround,
            // ddmlib is bugged.
            AndroidDebugBridge.disconnectBridge();

            // Terminate the bridge connection
            AndroidDebugBridge.terminate();

            dispatcher.close();

            // Stops the chrome driver started as a service
            deviceManager.stopChromeDriverService();

            if(AgentPropertiesLoader.hasFtpServer()) {
                // Disconnect and logout the FTP client
                deviceManager.stopFtpFileTransferService();
            }
        } catch (Exception e) {
            // If something cannot be closed it was never opened, so it's okay.
            // Nothing to do here.
            LOGGER.info(e);
        }
    }

    /**
     * Creates and starts a new emulator with specific DeviceParameters or just starts an emulator with the
     * DeviceParameters if such an emulator already exists.
     *
     * @param parameters
     *        DeviceParameters of the device we want created.
     * @return Device wrapper identifier.
     * @throws IOException
     *         thrown when an I/O exception of some sort has occurred.
     */
    public String createAndStartEmulator(EmulatorParameters parameters) throws IOException {
        String emulatorName = null;
        try {
            emulatorName = emulatorManager.createAndStartEmulator(parameters);
        } catch (CommandFailedException e) {
            LOGGER.fatal("Creating emulator device failed.", e);
        }
        return emulatorName;
    }

    /**
     * Closes the process of an emulator specified by it's serial number.
     *
     * @param serialNumber
     *        Serial number of the emulator we want closed.
     *
     * @throws DeviceNotFoundException
     *         Thrown when a method an Agent method was invoked with serial number of a device that is not present on
     *         the Agent.
     * @throws IOException
     *         thrown when an I/O exception of some sort has occurred.
     * @throws NotPossibleForDeviceException
     *         thrown when a command for real devices only was attempted on an emulator and vice versa.
     *
     */
    public void closeAndEraseEmulator(String serialNumber)
            throws DeviceNotFoundException,
            NotPossibleForDeviceException,
            IOException {
        emulatorManager.closeAndEraseEmulator(serialNumber);
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
     * Registers the server for events, related to changes in the devices on the agent. Should be called after the
     * server has published it's IAgentEventSender.
     *
     * @param serverIPAddress
     *        The server's RMI IP address.
     * @param serverRmiPort
     *        Port on which the RMI registry is opened.
     */
    public void registerServer(String serverIPAddress, int serverRmiPort) {
        // Try to construct a new device change listener that will notify the newly set server
        DeviceChangeListener newDeviceChangeListener = new DeviceChangeListener(serverIPAddress, serverRmiPort);
        androidDebugBridgeManager.setListener(newDeviceChangeListener);

        LOGGER.info("Server with IP (" + serverIPAddress + ":" + serverRmiPort + ") registered.");
    }

    /**
     * Connects this Agent to a Server.
     *
     * @param ipAddress
     *        server's IP address.
     * @param port
     *        server's RMI port.
     * @throws IllegalPortException
     *         thrown when the given port is not valid
     * @throws URISyntaxException
     * @throws IOException
     * @throws DeploymentException
     */
    public void connectToServer(String ipAddress, int port)
            throws IllegalPortException,
            DeploymentException,
            IOException,
            URISyntaxException {
        if (!isPortValueValid(port)) {
            throw new IllegalPortException("Given port " + port + " is not valid.");
        }

        dispatcher.connectToServer(ipAddress, port, agentId);
        LOGGER.info("Connection request sent to Server with address (" + ipAddress + ":" + port + ")");
    }

    private boolean isPortValueValid(int rmiPort) {
        boolean isPortOk = (rmiPort > 0 && rmiPort <= 65535);
        return isPortOk;
    }

    /**
     * Gets the hardware specifications of the device the agent is running on.
     *
     * @return {@link SystemSpecification} object describing the specifications of the device the agent is running on.
     */
    public SystemSpecification getSpecification() {
        SystemSpecification agentParameters = systemSpecificationLoader.getSpecification();
        return agentParameters;
    }

    /**
     * Returns a score based on how well an emulator with given parameters will perform on the agent.
     *
     * @param requiredDeviceParameters
     *        - the parameters of the emulator device.
     * @return a score based on how well the emulator will perform on the agent.
     */
    public double getPerformanceScore(EmulatorParameters requiredDeviceParameters) {
        double score = 0d;

        SystemSpecification systemSpecification = getSpecification();

        long freeRam = systemSpecification.getFreeRam();

        if (requiredDeviceParameters.getRam() >= freeRam) {
            // If there's no free RAM memory on the agent, running new emulator on it should not happen.
            return 0d;
        } else {
            score += freeRam;
        }

        boolean isHaxm = systemSpecification.isHaxm();
        double scimarkScore = systemSpecification.getScimarkScore();

        if (isHaxm) {
            // Emulators using HAXM perform ~50% faster.
            score += 1.5d * scimarkScore;
        } else {
            score += scimarkScore;
        }

        return score;
    }

    /**
     * Gets the serial number of an emulator with given AVD name.
     *
     * @param emulatorName
     *        - the AVD name of the emulator.
     * @return the serial number of the emulator.
     * @throws DeviceNotFoundException
     *         Thrown when a method an Agent method was invoked with serial number of a device that is not present on
     *         the Agent.
     */
    public String getSerialNumberOfEmulator(String emulatorName) throws DeviceNotFoundException {
        return emulatorManager.getSerialNumberOfEmulator(emulatorName);
    }

    /**
     * Waits until an emulator device with given AVD name is present on the agent or the timeout is reached.
     *
     * @param emulatorName
     *        - the AVD name of the emulator.
     * @param timeout
     *        - the timeout in milliseconds.
     * @throws TimeoutReachedException
     *         Thrown when the timeout for an action is reached.
     */
    public void waitForEmulatorExists(String emulatorName, long timeout)
            throws TimeoutReachedException {
        emulatorManager.waitForEmulatorExists(emulatorName, timeout);
    }

    public boolean isAnyEmulatorPresent() {
        return emulatorManager.isAnyEmulatorPresent();
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
     *         thrown when a command failed
     * @throws DeviceBootTimeoutReachedException
     *         thrown when a device boot timeout is reached
     * @throws DeviceNotFoundException
     *         thrown when a method an Agent method was invoked with serial number of a device that is not present on
     *         the Agent.
     */
    public void waitForEmulatorToBoot(String emulatorName, long timeout)
            throws CommandFailedException,
            DeviceBootTimeoutReachedException,
            DeviceNotFoundException {
        emulatorManager.waitForEmulatorToBoot(emulatorName, timeout);
    }

    /**
     * Gets a list of all published and available device wrapper RMI string identifiers on the current Agent.
     *
     * @return List of the DeviceInformation objects, one for every available device on the current Agent.
     */
    public List<String> getAllDeviceRmiIdentifiers() {
        return deviceManager.getAllDeviceRmiIdentifiers();
    }

    /**
     * Waits until a device with given serial number is present on the agent or the timeout is reached.
     *
     * @param serialNumber
     *        - the serial number of the device.
     * @param timeout
     *        - the timeout in milliseconds.
     * @throws TimeoutReachedException
     *         Thrown when the timeout for an action is reached.
     */
    public void waitForDeviceExists(String serialNumber, long timeout) throws TimeoutReachedException {
        deviceManager.waitForDeviceExists(serialNumber, timeout);
    }

    /**
     * Checks if any device is present on the agent (current machine).
     *
     * @return true if a device is present, false otherwise.
     */
    public boolean isAnyDevicePresent() {
        return deviceManager.isAnyDevicePresent();
    }

    /**
     * Gets the first available device that is present on the agent (current machine).
     *
     * @return the first available device wrapper ({@link IWrapDevice} interface).
     */
    public IWrapDevice getFirstAvailableDeviceWrapper() {
        return deviceManager.getFirstAvailableDeviceWrapper();
    }

    /**
     * Gets the first available emulator device that is present on the agent (current machine).
     *
     * @return the first available emulator wrapper ({@link IWrapDevice} interface).
     */
    public IWrapDevice getFirstAvailableEmulatorDeviceWrapper() {
        return deviceManager.getFirstAvailableEmulatorDeviceWrapper();
    }

    /**
     * Returns a unique identifier for this device, which will be used as a publishing string for the wrapper of the
     * device in RMI.
     *
     * @param deviceSerialNumber
     *        serial number of the device we want to get unique identifier for.
     * @throws DeviceNotFoundException
     *         thrown when a method an Agent method was invoked with serial number of a device that is not present on
     *         the Agent.
     * @return unique identifier for the device.
     */
    public String getRmiWrapperBindingIdentifier(String deviceSerialNumber)
            throws DeviceNotFoundException {
        return deviceManager.getRmiWrapperBindingIdentifier(deviceSerialNumber);
    }
}
