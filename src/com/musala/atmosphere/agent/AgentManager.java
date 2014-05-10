package com.musala.atmosphere.agent;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.musala.atmosphere.agent.util.AgentIdCalculator;
import com.musala.atmosphere.agent.util.SystemSpecificationLoader;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IConnectionRequestReceiver;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.commons.sa.SystemSpecification;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceBootTimeoutReachedException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NoAvailableDeviceFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;
import com.musala.atmosphere.commons.sa.exceptions.TimeoutReachedException;

/**
 * Used for managing all devices on the current Agent.
 * 
 * @author georgi.gaydarov
 * 
 */
public class AgentManager extends UnicastRemoteObject implements IAgentManager {
    /**
     * Automatically generated serialization id
     */
    private static final long serialVersionUID = 8467038223162311366L;

    private final static Logger LOGGER = Logger.getLogger(AgentManager.class.getCanonicalName());

    private AndroidDebugBridgeManager androidDebugBridgeManager;

    private EmulatorManager emulatorManager;

    private DeviceManager deviceManager;

    private Registry rmiRegistry;

    private final String agentId;

    private String serverIPAddress;

    private int serverRmiPort;

    private SystemSpecificationLoader systemSpecificationLoader;

    private int rmiRegistryPort;

    /**
     * Creates a new AgentManager on this computer.
     * 
     * @param adbPath
     *        Path to adb.exe
     * @param rmiPort
     *        Port, which will be used for the RMI Registry
     * @throws RemoteException
     * @throws ADBridgeFailException
     */
    public AgentManager(int rmiPort) throws RemoteException {
        systemSpecificationLoader = new SystemSpecificationLoader();
        systemSpecificationLoader.getSpecification();
        androidDebugBridgeManager = new AndroidDebugBridgeManager();
        emulatorManager = EmulatorManager.getInstance();

        // Calculate the current Agent ID.
        AgentIdCalculator agentIdCalculator = new AgentIdCalculator();
        agentId = agentIdCalculator.getId();

        // Publish this AgentManager in the RMI registry
        try {
            rmiRegistry = LocateRegistry.createRegistry(rmiPort);
            rmiRegistry.rebind(RmiStringConstants.AGENT_MANAGER.toString(), this);
        } catch (RemoteException e) {
            close();
            throw e;
        }
        rmiRegistryPort = rmiPort;
        deviceManager = new DeviceManager();

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

            // Remove all items in the registry so the RMI threads will be closed
            if (rmiRegistry != null) {
                String[] rmiIds = rmiRegistry.list();
                for (String currentRmiIdObject : rmiIds) {
                    Object registeredObject = rmiRegistry.lookup(currentRmiIdObject);
                    rmiRegistry.unbind(currentRmiIdObject);
                    try {
                        UnicastRemoteObject.unexportObject((Remote) registeredObject, true);
                    } catch (NoSuchObjectException e) {
                        LOGGER.warn("Could not unexport RMI object with ID: " + currentRmiIdObject);
                    }
                }

                UnicastRemoteObject.unexportObject(rmiRegistry, true);
            }
        } catch (Exception e) {
            // If something cannot be closed it was never opened, so it's okay.
            // Nothing to do here.
            LOGGER.info(e);
        }
    }

    @Override
    public String createAndStartEmulator(DeviceParameters parameters) throws RemoteException, IOException {
        String emulatorName = null;
        try {
            emulatorName = emulatorManager.createAndStartEmulator(parameters);
        } catch (CommandFailedException e) {
            LOGGER.fatal("Creating emulator device failed.", e);
        }
        return emulatorName;
    }

    @Override
    public void closeAndEraseEmulator(String serialNumber)
        throws DeviceNotFoundException,
            NotPossibleForDeviceException,
            IOException {
        emulatorManager.closeAndEraseEmulator(serialNumber);
    }

    @Override
    public String getAgentId() throws RemoteException {
        return agentId;
    }

    @Override
    public String getInvokerIpAddress() throws RemoteException {
        try {
            return RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            // Thrown when this method is not invoked by RMI. Nothing to do here, this should not happen.
            LOGGER.warn("The getInvokerIpAddress method was invoked locally and resulted in exception.");
        }
        return "";
    }

    @Override
    public void registerServer(String serverIPAddress, int serverRmiPort) throws RemoteException {
        this.serverIPAddress = serverIPAddress;
        this.serverRmiPort = serverRmiPort;

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
     * @throws NotBoundException
     * @throws RemoteException
     * @throws AccessException
     * @throws IllegalPortException
     */
    public void connectToServer(String ipAddress, int port)
        throws AccessException,
            RemoteException,
            NotBoundException,
            IllegalPortException {
        if (!isPortValueValid(port)) {
            throw new IllegalPortException("Given port " + port + " is not valid.");
        }
        Registry serverRegistry = LocateRegistry.getRegistry(ipAddress, port);
        IConnectionRequestReceiver requestReceiver = (IConnectionRequestReceiver) serverRegistry.lookup(RmiStringConstants.CONNECTION_REQUEST_RECEIVER.toString());
        requestReceiver.postConnectionRequest(rmiRegistryPort);
        LOGGER.info("Connection request sent to Server with address (" + ipAddress + ":" + port + ")");
    }

    private boolean isPortValueValid(int rmiPort) {
        boolean isPortOk = (rmiPort > 0 && rmiPort <= 65535);
        return isPortOk;
    }

    @Override
    public SystemSpecification getSpecification() throws RemoteException {
        SystemSpecification agentParameters = systemSpecificationLoader.getSpecification();
        return agentParameters;
    }

    @Override
    public double getPerformanceScore(DeviceParameters requiredDeviceParameters) throws RemoteException {
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

    @Override
    public String getSerialNumberOfEmulator(String emulatorName) throws DeviceNotFoundException {
        return emulatorManager.getSerialNumberOfEmulator(emulatorName);
    }

    @Override
    public void waitForEmulatorExists(String emulatorName, long timeout)
        throws RemoteException,
            TimeoutReachedException {
        emulatorManager.waitForEmulatorExists(emulatorName, timeout);
    }

    @Override
    public boolean isAnyEmulatorPresent() throws RemoteException {
        return emulatorManager.isAnyEmulatorPresent();
    }

    @Override
    public void waitForEmulatorToBoot(String emulatorName, long timeout)
        throws RemoteException,
            CommandFailedException,
            DeviceBootTimeoutReachedException,
            DeviceNotFoundException {
        emulatorManager.waitForEmulatorToBoot(emulatorName, timeout);
    }

    @Override
    public List<String> getAllDeviceRmiIdentifiers() throws RemoteException {
        return deviceManager.getAllDeviceRmiIdentifiers();
    }

    @Override
    public void waitForDeviceExists(String serialNumber, long timeout) throws RemoteException, TimeoutReachedException {
        deviceManager.waitForDeviceExists(serialNumber, timeout);
    }

    @Override
    public boolean isAnyDevicePresent() throws RemoteException {
        return deviceManager.isAnyDevicePresent();
    }

    @Override
    public IWrapDevice getFirstAvailableDeviceWrapper()
        throws RemoteException,
            NotBoundException,
            NoAvailableDeviceFoundException {
        return deviceManager.getFirstAvailableDeviceWrapper();
    }

    @Override
    public IWrapDevice getFirstAvailableEmulatorDeviceWrapper()
        throws RemoteException,
            NotBoundException,
            NoAvailableDeviceFoundException {
        return deviceManager.getFirstAvailableEmulatorDeviceWrapper();
    }
}
