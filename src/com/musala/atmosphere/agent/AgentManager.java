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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.exception.IllegalPortException;
import com.musala.atmosphere.agent.util.AgentIdCalculator;
import com.musala.atmosphere.agent.util.SystemSpecificationLoader;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IConnectionRequestReceiver;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.commons.sa.SystemSpecification;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

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

    private AndroidDebugBridge androidDebugBridge;

    private Registry rmiRegistry;

    private final String agentId;

    private String serverIPAddress;

    private int serverRmiPort;

    private AgentIdCalculator agentIdCalculator;

    private SystemSpecificationLoader systemSpecificationLoader;

    private DeviceChangeListener deviceChangeListener;

    // CopyOnWriteArrayList, as we will not have many devices (more than 10 or 15 practically) connected on a single
    // agent and we are concerned about the DeviceChangeListener not to break things.
    private volatile List<IDevice> devicesList = new CopyOnWriteArrayList<IDevice>();

    private int rmiRegistryPort;

    private DeviceManager deviceManager;

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
    public AgentManager(AndroidDebugBridgeManager androidDebugBridgeManager, int rmiPort, DeviceManager deviceManager)
        throws RemoteException {
        systemSpecificationLoader = new SystemSpecificationLoader();
        systemSpecificationLoader.getSpecification();

        this.deviceManager = deviceManager;

        // Calculate the current Agent ID.
        agentIdCalculator = new AgentIdCalculator();
        agentId = agentIdCalculator.getId();

        androidDebugBridgeManager.setListener(deviceChangeListener);

        // Publish this AgentManager in the RMI registry
        try {
            rmiRegistry = LocateRegistry.createRegistry(rmiPort);
            rmiRegistry.rebind(RmiStringConstants.AGENT_MANAGER.toString(), this);
        } catch (RemoteException e) {
            close();
            throw e;
        }
        rmiRegistryPort = rmiPort;

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

    public List<String> getAllDeviceWrappers() throws RemoteException {
        List<String> wrappersList = new LinkedList<>();

        for (IDevice device : devicesList) {
            String rmiWrapperBindingId = deviceManager.getRmiWrapperBindingIdentifier(device);
            wrappersList.add(rmiWrapperBindingId);
        }

        return wrappersList;
    }

    @Override
    public void createAndStartEmulator(DeviceParameters parameters) throws RemoteException, IOException {
        EmulatorManager emulatorManager = EmulatorManager.getInstance();
        emulatorManager.createAndStartEmulator(parameters);
    }

    // FIXME remove/edit this method
    @Override
    public void closeEmulator(String serialNumber)
        throws RemoteException,
            NotPossibleForDeviceException,
            DeviceNotFoundException {
        IDevice device = deviceManager.getDeviceBySerialNumber(serialNumber);

        // If a device is a real, physical device, throw an exception
        if (!device.isEmulator()) {
            throw new NotPossibleForDeviceException("Cannot close a real device.");
        }

        // Get the emulator's EmulatorConsole and send a kill.
        EmulatorConsole emulatorConsole = EmulatorConsole.getConsole(device);
        emulatorConsole.kill();
    }

    @Override
    public void wipeEmulator(String serialNumber) throws RemoteException {
        // FIXME remove / edit this method
        // TODO wipe emulator method
        // from emulator.exe help :
        // -wipe-data reset the user data image (copy it from initdata)

    }

    @Override
    public void eraseEmulator(String serialNumber)
        throws RemoteException,
            IOException,
            DeviceNotFoundException,
            NotPossibleForDeviceException {
        // FIXME remove/edit this method.
        IDevice device = deviceManager.getDeviceBySerialNumber(serialNumber);
        if (device.isEmulator() == false) {
            throw new NotPossibleForDeviceException("Cannot close and erase a real device.");
        }
        EmulatorManager emulatorManager = EmulatorManager.getInstance();
        emulatorManager.closeAndEraseEmulator(device);
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
        DeviceChangeListener newDeviceChangeListener = new DeviceChangeListener(serverIPAddress,
                                                                                serverRmiPort,
                                                                                getAgentId(),
                                                                                deviceManager);
        // And if everything went well, unsubscribe the old device change listener
        AndroidDebugBridge.removeDeviceChangeListener(deviceChangeListener);

        // And subscribe the new one
        AndroidDebugBridge.addDeviceChangeListener(newDeviceChangeListener);
        deviceChangeListener = newDeviceChangeListener;

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
}
