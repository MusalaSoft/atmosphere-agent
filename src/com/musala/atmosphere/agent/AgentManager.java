package com.musala.atmosphere.agent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.devicewrapper.EmulatorWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.RealWrapDevice;
import com.musala.atmosphere.agent.util.DeviceScreenResolutionParser;
import com.musala.atmosphere.agent.util.MemoryUnitConverter;
import com.musala.atmosphere.commons.sa.DeviceInformation;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.IAgentManager;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.RmiStringConstants;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;
import com.musala.atmosphere.commons.sa.util.Pair;

/**
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

	private static final String FALLBACK_DISPLAY_DENSITY = "0";

	private static final String FALLBACK_RAM_AMOUNT = "0";

	private static final Pair<Integer, Integer> FALLBACK_SCREEN_RESOLUTION = new Pair<Integer, Integer>(0, 0);

	private final static Logger LOGGER = Logger.getLogger(AgentManager.class.getName());

	private AndroidDebugBridge androidDebugBridge;

	private Registry rmiRegistry;

	private volatile List<IDevice> devicesList;

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
	 */
	public AgentManager(String adbPath, int rmiPort) throws RemoteException, ADBridgeFailException
	{
		// Set up the logger
		try
		{
			Handler fileHandler = new FileHandler("agentmanager.log");
			LOGGER.addHandler(fileHandler);
		}
		catch (SecurityException | IOException e)
		{
			// Could not create the log file.
			// Well, we can't log this...
			e.printStackTrace();
		}

		LOGGER.setLevel(Level.ALL);

		// Start the bridge
		AndroidDebugBridge.init(false /* debugger support */);
		androidDebugBridge = AndroidDebugBridge.createBridge(adbPath, false);

		// Get the initial devices list
		devicesList = getInitialDeviceList();

		// Set up a device change listener that will update the devices list, but not connect to any server.
		// Server connection will be established later, when a server registers itself.
		currentDeviceChangeListener = new DeviceChangeListener(devicesList);
		AndroidDebugBridge.addDeviceChangeListener(currentDeviceChangeListener);

		// Publish this AgentManager in the RMI registry
		rmiRegistry = LocateRegistry.createRegistry(rmiPort);
		rmiRegistry.rebind(RmiStringConstants.AGENT_MANAGER.toString(), this);

		rmiRegistryPort = rmiPort;

	}

	/**
	 * Gets the initial devices list (IDevices). Gets called in the AgentManager constructor.
	 * 
	 * @return List of IDevices (CopyOnWrite array list, for thread-safety)
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
			// let's not wait > 10 seconds.
			if (timeout > 100)
			{
				LOGGER.severe("Timeout getting initial device list.");

				throw new ADBridgeFailException("Bridge timed out.");
			}
		}

		IDevice[] devicesArray = androidDebugBridge.getDevices();

		// CopyOnWriteArrayList. Because we wont have more than 15 devices on a single Agent
		// and the device change listener will not be able to mess things up this way.
		List<IDevice> devicesList = new CopyOnWriteArrayList<IDevice>(devicesArray);

		return devicesList;
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
		try
		{
			// Terminate the bridge connection
			AndroidDebugBridge.terminate();

			// Close the registry
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
	public List<DeviceInformation> getAllDevicesInformation() throws RemoteException
	{
		List<DeviceInformation> deviceInfoList = new LinkedList<DeviceInformation>();

		// For each device, get it's device info structure and add it to the list
		for (IDevice device : devicesList)
		{
			DeviceInformation thisDeviceDeviceInfo = getDeviceInformation(device);
			deviceInfoList.add(thisDeviceDeviceInfo);
		}

		return deviceInfoList;
	}

	@Override
	public String createWrapperForDevice(String serialNumber) throws RemoteException, DeviceNotFoundException
	{
		IWrapDevice deviceWrapper = null;
		IDevice device = getDeviceBySerialNumber(serialNumber);

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

		// Register the wrapper in the RMI registry under the device's serial number
		// This code resulted in an exception : this.rmiRegistry.rebind(serialNumber, deviceWrapper);
		// It was understood as "bind the deviceWrapper on the invoking machine's registry"
		// This is a workaround.
		try
		{
			Naming.rebind("//localhost:" + rmiRegistryPort + "/" + serialNumber, deviceWrapper);
		}
		catch (MalformedURLException e)
		{
			throw new RemoteException(	"Exception occured when rebinding the device wrapper. See the enclosed exception.",
										e);
		}

		return device.getSerialNumber();
	}

	@Override
	public boolean isDevicePresent(String serialNumber) throws RemoteException
	{
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
	public DeviceInformation getDeviceInformation(String serialNumber) throws RemoteException, DeviceNotFoundException
	{
		IDevice device = getDeviceBySerialNumber(serialNumber);
		DeviceInformation deviceInformation = getDeviceInformation(device);
		return deviceInformation;
	}

	/**
	 * Gets a {@link DeviceInformation DeviceInformation} structure for a specific IDevice.
	 * 
	 * @param device
	 * @return The populated {@link DeviceInformation DeviceInformation}.
	 */
	private DeviceInformation getDeviceInformation(IDevice device)
	{
		DeviceInformation deviceInformation = new DeviceInformation();

		Map<String, String> devicePropertiesMap = device.getProperties();

		// Density
		String lcdDensityString = FALLBACK_DISPLAY_DENSITY;
		if (device.isEmulator())
		{
			lcdDensityString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_EMUDEVICE_LCD_DENSITY.toString());
		}
		else
		{
			lcdDensityString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_REALDEVICE_LCD_DENSITY.toString());
		}
		deviceInformation.setDpi(Integer.parseInt(lcdDensityString));

		// isEmulator
		deviceInformation.setEmulator(device.isEmulator());

		// Model
		deviceInformation.setModel(devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_PRODUCT_MODEL.toString()));

		// OS
		deviceInformation.setOs(devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_OS_VERSION.toString()));

		// RAM
		String ramMemoryString = FALLBACK_RAM_AMOUNT;
		if (device.isEmulator())
		{
			// FIXME get the ram for emulators too.
		}
		else
		{
			devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_REALDEVICE_RAM.toString());
		}
		deviceInformation.setRam(MemoryUnitConverter.convertMemoryToMB(ramMemoryString));

		// Resolution
		deviceInformation.setResolution(FALLBACK_SCREEN_RESOLUTION);
		try
		{
			CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
			device.executeShellCommand("dumpsys window policy", outputReceiver);
			String shellResponse = outputReceiver.getOutput();
			deviceInformation.setResolution(DeviceScreenResolutionParser.parseScreenResolutionFromShell(shellResponse));
		}
		catch (ShellCommandUnresponsiveException | TimeoutException | AdbCommandRejectedException | IOException e)
		{
			// Shell command execution failed.
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, "Shell command execution failed.", e);
		}
		/*
		 * catch (ShellCommandUnresponsiveException e) { // The shell does not respond. e.printStackTrace(); } catch
		 * (TimeoutException e) { // Adb does not respond. Wut. e.printStackTrace(); } catch
		 * (AdbCommandRejectedException e) { // ADB will not send commands to the device. Maybe it's offline?
		 * e.printStackTrace(); } catch (IOException e) { // luck life. Socket connection is bad. e.printStackTrace(); }
		 */

		// Serial number
		deviceInformation.setSerialNumber(device.getSerialNumber());

		return deviceInformation;
	}

	@Override
	public DeviceInformation createEmulator(DeviceParameters parameters) throws RemoteException
	{
		// TODO Create emulator method
		return null;
	}

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
		// TODO wipe emulator method
		// from emulator.exe help :
		// -wipe-data reset the user data image (copy it from initdata)

	}

	@Override
	public void eraseEmulator(String serialNumber) throws RemoteException
	{
		// TODO Erase emulator method
	}

	@Override
	public String getAgentId() throws RemoteException
	{
		// TODO Should discuss what id would be unique for an agent
		// as selecting IP is a rather difficult process
		return null;
	}

	@Override
	public void registerServer(String serverIPAddress, int serverRmiPort) throws RemoteException
	{
		// Try to construct a new device change listener that will notify the newly set server
		DeviceChangeListener newDeviceChangeListener = new DeviceChangeListener(serverIPAddress,
																				serverRmiPort,
																				getAgentId(),
																				devicesList);

		// And if everything went well, unsubscribe the old device change listener
		AndroidDebugBridge.removeDeviceChangeListener(currentDeviceChangeListener);

		// And subscribe the new one
		AndroidDebugBridge.addDeviceChangeListener(newDeviceChangeListener);
		currentDeviceChangeListener = newDeviceChangeListener;
	}
}
