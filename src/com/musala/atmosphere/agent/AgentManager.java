package com.musala.atmosphere.agent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.devicewrapper.EmulatorWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.RealWrapDevice;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IConnectionRequestReceiver;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

/**
 * Used for managing all devices on the current Agent.
 * 
 * @author georgi.gaydarov
 * 
 */
public class AgentManager extends UnicastRemoteObject implements IAgentManager
{
	/**
	 * Automatically generated serialization id
	 */
	private static final long serialVersionUID = 8467038223162311366L;

	private final static String AGENTID_HASH_ALGORITHM = "md5";

	private final static Logger LOGGER = Logger.getLogger(AgentManager.class.getCanonicalName());

	private AndroidDebugBridge androidDebugBridge;

	private Registry rmiRegistry;

	private final String agentId;

	// CopyOnWriteArrayList, as we will not have many devices (more than 10 or 15 practically) connected on a single
	// agent and we are concerned about the DeviceChangeListener not to break things.
	private volatile List<IDevice> devicesList = new CopyOnWriteArrayList<IDevice>();

	private DeviceChangeListener currentDeviceChangeListener;

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
	 * @throws CommandFailedException
	 * @throws AdbCommandRejectedException
	 * @throws TimeoutException
	 */
	public AgentManager(String adbPath, int rmiPort) throws RemoteException, ADBridgeFailException
	{
		// Start the bridge
		try
		{
			AndroidDebugBridge.init(false /* debugger support */);
			androidDebugBridge = AndroidDebugBridge.createBridge(adbPath, false /*
																				 * force new bridge, no need for that
																				 */);
		}
		catch (IllegalStateException e)
		{
			// The debug bridge library was already init-ed.
			// This means we are creating a new AgentManager while another is active, which is not okay.
			close();
			throw new ADBridgeFailException("The debug bridge failed to init, see the enclosed exception for more information.",
											e);
		}
		catch (NullPointerException e)
		{
			// The debug bridge creation failed internally.
			close();
			throw new ADBridgeFailException("The debug bridge failed to init, see the enclosed exception for more information.",
											e);
		}

		// Calculate the current Agent ID.
		agentId = calculateId();

		// Publish this AgentManager in the RMI registry
		try
		{
			rmiRegistry = LocateRegistry.createRegistry(rmiPort);
			rmiRegistry.rebind(RmiStringConstants.AGENT_MANAGER.toString(), this);
		}
		catch (RemoteException e)
		{
			close();
			throw e;
		}
		rmiRegistryPort = rmiPort;

		// Get the initial devices list
		List<IDevice> initialDevicesList = null;
		try
		{
			initialDevicesList = getInitialDeviceList();
		}
		catch (ADBridgeFailException e)
		{
			close();
			throw e;
		}

		LOGGER.info("Initial device list fetched containing " + initialDevicesList.size() + " devices.");
		for (IDevice initialDevice : initialDevicesList)
		{
			registerDeviceOnAgent(initialDevice);
		}

		// Set up a device change listener that will update this agent, but not connect and notify any server.
		// Server connection will be established later, when a server registers itself.
		currentDeviceChangeListener = new DeviceChangeListener(this);
		AndroidDebugBridge.addDeviceChangeListener(currentDeviceChangeListener);
	}

	/**
	 * Gets the initial devices list (IDevices). Gets called in the AgentManager constructor.
	 * 
	 * @return List of IDevices
	 * @throws ADBridgeFailException
	 */
	private List<IDevice> getInitialDeviceList() throws ADBridgeFailException
	{
		// From an adb example :
		// we can't just ask for the device list right away, as the internal thread getting
		// them from ADB may not be done getting the first list.
		// Since we don't really want getDevices() to be blocking, we wait here manually.
		int timeout = 0;

		while (androidDebugBridge.hasInitialDeviceList() == false)
		{
			try
			{
				Thread.sleep(100);
				timeout++;
			}
			catch (InterruptedException e)
			{
			}
			// let's not wait > timeout milliseconds.
			if (timeout * 100 > AgentPropertiesLoader.getADBridgeTimeout())
			{
				LOGGER.fatal("Timeout getting initial device list.");

				throw new ADBridgeFailException("Bridge timed out.");
			}
		}

		IDevice[] devicesArray = androidDebugBridge.getDevices();
		return Arrays.asList(devicesArray);
	}

	/**
	 * Calls the {@link #close() close()} method just to be sure everything is closed.
	 */
	@Override
	public void finalize()
	{
		close();
	}

	/**
	 * Closes all open resources. <b>MUST BE CALLED WHEN THIS CLASS IS NO LONGER NEEDED.</b>
	 */
	public void close()
	{
		LOGGER.info("Closing the AgentManager.");
		try
		{
			// We close the bridge and adb service, so bridge creation wont fail next time we try. This is a workaround,
			// ddmlib is bugged.
			AndroidDebugBridge.disconnectBridge();

			// Terminate the bridge connection
			AndroidDebugBridge.terminate();

			// Remove all items in the registry so the RMI threads will be closed
			String[] rmiIds = rmiRegistry.list();
			for (String currentRmiIdObject : rmiIds)
			{
				Object registeredObject = rmiRegistry.lookup(currentRmiIdObject);
				rmiRegistry.unbind(currentRmiIdObject);
				try
				{
					UnicastRemoteObject.unexportObject((Remote) registeredObject, true);
				}
				catch (Exception e)
				{
					LOGGER.warn("Could not unexport RMI object with ID: " + currentRmiIdObject);
				}
			}

			if (rmiRegistry != null)
			{
				UnicastRemoteObject.unexportObject(rmiRegistry, true);
			}
		}
		catch (Exception e)
		{
			// If something cannot be closed it was never opened, so it's okay.
			// Nothing to do here.
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getAllDeviceWrappers() throws RemoteException
	{
		List<String> wrappersList = new LinkedList<>();

		for (IDevice device : devicesList)
		{
			String rmiWrapperBindingId = getRmiWrapperBindingIdentifier(device);
			wrappersList.add(rmiWrapperBindingId);
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
	 * @throws CommandFailedException
	 * @throws AdbCommandRejectedException
	 * @throws TimeoutException
	 */
	String registerDeviceOnAgent(IDevice connectedDevice)
	{
		if (devicesList.contains(connectedDevice))
		{
			// The device is already registered, nothing to do here.
			// This should not normally happen!
			LOGGER.warn("Trying to register a device that is already registered.");
			return "";
		}

		try
		{
			String publishId = createWrapperForDevice(connectedDevice);
			devicesList.add(connectedDevice);
			return publishId;
		}
		catch (RemoteException e)
		{
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
	String unregisterDeviceOnAgent(IDevice disconnectedDevice)
	{
		if (devicesList.contains(disconnectedDevice) == false)
		{
			// The device was never registered, so nothing to do here.
			// This should not normally happen!
			LOGGER.warn("Trying to unregister a device that was not registered at all.");
			return "";
		}

		try
		{
			String publishId = removeWrapperForDevice(disconnectedDevice);
			devicesList.remove(disconnectedDevice);
			return publishId;
		}
		catch (RemoteException e)
		{
			LOGGER.fatal("Could not unbind a device wrapper from the RMI registry.", e);
			e.printStackTrace();
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
	 * @throws CommandFailedException
	 * @throws AdbCommandRejectedException
	 * @throws TimeoutException
	 */
	private String createWrapperForDevice(IDevice device) throws RemoteException
	{
		IWrapDevice deviceWrapper = null;
		String rmiWrapperBindingId = getRmiWrapperBindingIdentifier(device);

		// Create a device wrapper depending on the device type (emulator/real)
		try
		{
			if (device.isEmulator())
			{
				deviceWrapper = new EmulatorWrapDevice(device);
			}
			else
			{
				deviceWrapper = new RealWrapDevice(device);
			}
		}
		catch (NotPossibleForDeviceException e)
		{
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
	private String getRmiWrapperBindingIdentifier(IDevice device)
	{
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
	private String removeWrapperForDevice(IDevice device) throws RemoteException
	{
		String rmiWrapperBindingId = getRmiWrapperBindingIdentifier(device);

		try
		{
			rmiRegistry.unbind(rmiWrapperBindingId);
		}
		catch (NotBoundException e)
		{
			// Wrapper for the device was never published, so we have nothing to unbind.
			// Nothing to do here.
			e.printStackTrace();
			return "";
		}
		catch (AccessException e)
		{
			throw new RemoteException("Unbinding a device wrapper resulted in an unexpected exception (enclosed).", e);
		}

		LOGGER.info("Removed wrapper for device with bindingId = " + rmiWrapperBindingId);

		return rmiWrapperBindingId;
	}

	@Override
	public boolean isDevicePresent(String serialNumber) throws RemoteException
	{
		// FIXME REMOVE THIS METHOD
		for (IDevice device : devicesList)
		{
			if (device.getSerialNumber() == serialNumber)
				return true;
		}
		return false;
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
	private IDevice getDeviceBySerialNumber(String serialNumber) throws DeviceNotFoundException
	{
		for (IDevice device : devicesList)
		{
			if (device.getSerialNumber().equals(serialNumber))
			{
				return device;
			}
		}
		throw new DeviceNotFoundException("Device with serial number " + serialNumber + " not found on this agent.");
	}

	@Override
	public void createAndStartEmulator(DeviceParameters parameters) throws RemoteException, IOException
	{
		EmulatorManager emulatorManager = EmulatorManager.getInstance();
		String createdEmulatorName = emulatorManager.createAndStartEmulator(parameters);
	}

	// FIXME remove/edit this method
	@Override
	public void closeEmulator(String serialNumber)
		throws RemoteException,
			NotPossibleForDeviceException,
			DeviceNotFoundException
	{
		IDevice device = getDeviceBySerialNumber(serialNumber);

		// If a device is a real, physical device, throw an exception
		if (device.isEmulator() == false)
		{
			throw new NotPossibleForDeviceException("Cannot close a real device.");
		}

		// Get the emulator's EmulatorConsole and send a kill.
		EmulatorConsole emulatorConsole = EmulatorConsole.getConsole(device);
		emulatorConsole.kill();
	}

	@Override
	public void wipeEmulator(String serialNumber) throws RemoteException
	{
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
			NotPossibleForDeviceException
	{
		// FIXME remove/edit this method.
		IDevice device = getDeviceBySerialNumber(serialNumber);
		if (device.isEmulator() == false)
		{
			throw new NotPossibleForDeviceException("Cannot close and erase a real device.");
		}
		EmulatorManager emulatorManager = EmulatorManager.getInstance();
		emulatorManager.closeAndEraseEmulator(device);
	}

	@Override
	public String getAgentId() throws RemoteException
	{
		return agentId;
	}

	private static String calculateId()
	{
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			StringBuilder ipConcatBuilder = new StringBuilder();
			while (interfaces.hasMoreElements())
			{
				NetworkInterface netInterface = interfaces.nextElement();
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements())
				{
					InetAddress address = addresses.nextElement();
					String addressString = address.getHostAddress();
					ipConcatBuilder.append(addressString);
				}
			}

			String ipConcatString = ipConcatBuilder.toString();
			byte[] ipConcatBytes = ipConcatString.getBytes();
			MessageDigest digest = MessageDigest.getInstance(AGENTID_HASH_ALGORITHM);
			byte[] hashBytes = digest.digest(ipConcatBytes);
			String hash = (new HexBinaryAdapter()).marshal(hashBytes);

			return hash;
		}
		catch (SocketException e)
		{
			LOGGER.fatal("Calculating unique ID failed.", e);
			throw new RuntimeException("Unique ID for the current Agent calculation failed.", e);
		}
		catch (NoSuchAlgorithmException e)
		{
			LOGGER.fatal("Could not get instance of MessageDigest for the passed hash algorithm.", e);
			throw new RuntimeException("Unique ID for the current Agent calculation failed.", e);
		}
	}

	@Override
	public String getInvokerIpAddress() throws RemoteException
	{
		try
		{
			return RemoteServer.getClientHost();
		}
		catch (ServerNotActiveException e)
		{
			// Thrown when this method is not invoked by RMI. Nothing to do here, this should not happen.
			LOGGER.warn("The getInvokerIpAddress method was invoked locally and resulted in exception.");
		}
		return "";
	}

	@Override
	public void registerServer(String serverIPAddress, int serverRmiPort) throws RemoteException
	{
		// Try to construct a new device change listener that will notify the newly set server
		DeviceChangeListener newDeviceChangeListener = new DeviceChangeListener(serverIPAddress,
																				serverRmiPort,
																				getAgentId(),
																				this);

		// And if everything went well, unsubscribe the old device change listener
		AndroidDebugBridge.removeDeviceChangeListener(currentDeviceChangeListener);

		// And subscribe the new one
		AndroidDebugBridge.addDeviceChangeListener(newDeviceChangeListener);
		currentDeviceChangeListener = newDeviceChangeListener;

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
	 */
	public void connectToServer(String ipAddress, int port) throws AccessException, RemoteException, NotBoundException
	{
		Registry serverRegistry = LocateRegistry.getRegistry(ipAddress, port);
		IConnectionRequestReceiver requestReceiver = (IConnectionRequestReceiver) serverRegistry.lookup(RmiStringConstants.CONNECTION_REQUEST_RECEIVER.toString());
		requestReceiver.postConnectionRequest(rmiRegistryPort);
		LOGGER.info("Connection request sent to Server with address (" + ipAddress + ":" + port + ")");
	}
}
