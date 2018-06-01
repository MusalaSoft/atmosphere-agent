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
import com.musala.atmosphere.agent.websocket.AgentDispatcher;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.EmulatorParameters;
import com.musala.atmosphere.commons.sa.SystemSpecification;
import com.musala.atmosphere.commons.sa.exceptions.DeviceBootTimeoutReachedException;
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

    private AgentDispatcher dispatcher;

    /**
     * Creates a new AgentManager on this computer.
     *
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

        dispatcher = AgentDispatcher.getInstance();
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
            deviceManager.releaseAllOnDeviceComponents();

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
     */
    public void registerServer() {
        // Try to construct a new device change listener that will notify the newly set server
        DeviceChangeListener newDeviceChangeListener = new DeviceChangeListener(true);
        androidDebugBridgeManager.setListener(newDeviceChangeListener);
    }

    /**
     * Connects this Agent to a Server.
     *
     * @param ipAddress
     *        server's IP address.
     * @param port
     *        server's port.
     * @throws IllegalPortException
     *         thrown when the given port is not valid
     * @throws URISyntaxException
     *         thrown when a string could not be parsed as a URI reference
     * @throws IOException
     *         thrown when an I/O exception of some sort has occurred
     * @throws DeploymentException
     *         thrown when failed to connect to the Server
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

    private boolean isPortValueValid(int port) {
        boolean isPortOk = (port > 0 && port <= 65535);
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
     * Gets a list of all published and available device identifiers on the current Agent.
     *
     * @return List of the DeviceInformation objects, one for every available device on the current Agent.
     */
    public List<String> getAllDeviceIdentifiers() {
        return deviceManager.getAllDeviceSerialNumbers();
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

}
