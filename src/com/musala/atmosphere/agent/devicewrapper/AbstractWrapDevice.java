package com.musala.atmosphere.agent.devicewrapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.DevicePropertyStringConstants;
import com.musala.atmosphere.agent.devicewrapper.util.DeviceProfiler;
import com.musala.atmosphere.agent.util.DeviceScreenResolutionParser;
import com.musala.atmosphere.agent.util.MemoryUnitConverter;
import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.sa.DeviceInformation;
import com.musala.atmosphere.commons.sa.IWrapDevice;

public abstract class AbstractWrapDevice extends UnicastRemoteObject implements IWrapDevice
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9122701818928360023L;

	// WARNING : do not change the remote folder unless you really know what you are doing.
	private static final String XMLDUMP_REMOTE_FILE_NAME = "/data/local/tmp/uidump.xml";

	private static final String XMLDUMP_LOCAL_FILE_NAME = "uidump.xml";

	private static final String SCREENSHOT_REMOTE_FILE_NAME = "/data/local/tmp/screen.png";

	private static final String SCREENSHOT_LOCAL_FILE_NAME = "screen.png";

	private static final String TEMP_APK_FILE_SUFFIX = ".apk";

	private File tempApkFile;

	private OutputStream tempApkFileOutputStream;

	protected IDevice wrappedDevice;

	private final static Logger LOGGER = Logger.getLogger(AbstractWrapDevice.class.getCanonicalName());

	public AbstractWrapDevice(IDevice deviceToWrap) throws RemoteException
	{
		wrappedDevice = deviceToWrap;
	}

	@Override
	public Pair<Integer, Integer> getNetworkSpeed() throws RemoteException
	{
		// TODO get network speed for abstract devices
		return null;
	}

	@Override
	public int getBatteryLevel() throws RemoteException, CommandFailedException
	{
		int level;
		try
		{
			level = wrappedDevice.getBatteryLevel(0 /* renew value, don't return old one */);
		}
		catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e)
		{
			// Redirect the exception to the server
			throw new CommandFailedException(	"getBatteryLevel failed. See the enclosed exception for more information.",
												e);
		}
		return level;
	}

	@Override
	public long getFreeRAM() throws RemoteException, CommandFailedException
	{
		DeviceProfiler profiler = new DeviceProfiler(wrappedDevice);
		try
		{
			Map<String, Long> memUsage = profiler.getMeminfoDataset();
			long freeMemory = memUsage.get(DeviceProfiler.FREE_MEMORY_ID);
			return freeMemory;
		}
		catch (IOException | TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException e)
		{
			LOGGER.warn("Getting device '" + wrappedDevice.getSerialNumber() + "' memory usage resulted in exception.",
						e);
			throw new CommandFailedException("Getting device memory usage resulted in exception.", e);
		}
	}

	@Override
	public String executeShellCommand(String command) throws RemoteException, CommandFailedException
	{
		String response = "";

		try
		{
			CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
			wrappedDevice.executeShellCommand(command, outputReceiver);
			response = outputReceiver.getOutput();
		}
		catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e)
		{
			// Redirect the exception to the server
			throw new CommandFailedException(	"Shell command execution failed. See the enclosed exception for more information.",
												e);
		}

		return response;
	}

	@Override
	public List<String> executeSequenceOfShellCommands(List<String> commandsList)
		throws RemoteException,
			CommandFailedException
	{
		List<String> responses = new ArrayList<String>();

		for (String commandForExecution : commandsList)
		{
			String responseFromCommandExecution = executeShellCommand(commandForExecution);
			responses.add(responseFromCommandExecution);
		}

		return responses;
	}

	@Override
	public DeviceInformation getDeviceInformation() throws RemoteException
	{
		DeviceInformation deviceInformation = new DeviceInformation();

		// Serial number
		deviceInformation.setSerialNumber(wrappedDevice.getSerialNumber());

		// isEmulator
		deviceInformation.setEmulator(wrappedDevice.isEmulator());

		// If the device will not give us it's valid properties, return the structure with the fallback values set.
		if (wrappedDevice.isOffline() || wrappedDevice.arePropertiesSet() == false)
		{
			return deviceInformation;
		}

		// Attempt to get the device properties only if the device is online.
		Map<String, String> devicePropertiesMap = wrappedDevice.getProperties();

		// CPU
		if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_CPU_TYPE.toString()))
		{
			String cpu = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_CPU_TYPE.toString());
			deviceInformation.setCpu(cpu);
		}

		// Density
		String lcdDensityString = DeviceInformation.FALLBACK_DISPLAY_DENSITY.toString();
		if (wrappedDevice.isEmulator())
		{
			if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_EMUDEVICE_LCD_DENSITY.toString()))
			{
				lcdDensityString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_EMUDEVICE_LCD_DENSITY.toString());
			}
		}
		else
		{
			if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_REALDEVICE_LCD_DENSITY.toString()))
			{
				lcdDensityString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_REALDEVICE_LCD_DENSITY.toString());
			}
		}
		deviceInformation.setDpi(Integer.parseInt(lcdDensityString));

		// Model
		if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_PRODUCT_MODEL.toString()))
		{
			String productModel = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_PRODUCT_MODEL.toString());
			deviceInformation.setModel(productModel);
		}

		// OS
		if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_OS_VERSION.toString()))
		{
			String deviceOs = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_OS_VERSION.toString());
			deviceInformation.setOs(deviceOs);
		}

		// RAM
		String ramMemoryString = DeviceInformation.FALLBACK_RAM_AMOUNT.toString();
		if (wrappedDevice.isEmulator())
		{
			// FIXME get the ram for emulators too.
		}
		else
		{
			if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_REALDEVICE_RAM.toString()))
			{
				ramMemoryString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_REALDEVICE_RAM.toString());
			}
		}
		deviceInformation.setRam(MemoryUnitConverter.convertMemoryToMB(ramMemoryString));

		// Resolution
		try
		{
			CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
			wrappedDevice.executeShellCommand("dumpsys window policy", outputReceiver);

			String shellResponse = outputReceiver.getOutput();
			Pair<Integer, Integer> screenResolution = DeviceScreenResolutionParser.parseScreenResolutionFromShell(shellResponse);
			deviceInformation.setResolution(screenResolution);

		}
		catch (ShellCommandUnresponsiveException | TimeoutException | AdbCommandRejectedException | IOException e)
		{
			// Shell command execution failed.
			e.printStackTrace();
			LOGGER.fatal("Shell command execution failed.", e);
		}
		catch (StringIndexOutOfBoundsException e)
		{
			LOGGER.warn("Parsing shell response failed when attempting to get device screen size.");
		}

		return deviceInformation;
	}

	@Override
	public byte[] getScreenshot() throws RemoteException, CommandFailedException
	{
		String screenshotCommand = "screencap -p " + SCREENSHOT_REMOTE_FILE_NAME;
		executeShellCommand(screenshotCommand);

		try
		{
			wrappedDevice.pullFile(SCREENSHOT_REMOTE_FILE_NAME, SCREENSHOT_LOCAL_FILE_NAME);

			Path screenshotPath = Paths.get(SCREENSHOT_LOCAL_FILE_NAME);
			byte[] screenshotData = Files.readAllBytes(screenshotPath);
			return screenshotData;
		}
		catch (IOException | AdbCommandRejectedException | TimeoutException | SyncException e)
		{
			LOGGER.error("Screenshot fetching failed.", e);
			throw new CommandFailedException(	"Screenshot fetching failed. See the enclosed exception for more information.",
												e);
		}
	}

	@Override
	public void initAPKInstall() throws RemoteException, IOException
	{
		discardAPK();

		String tempApkFilePrefix = wrappedDevice.getSerialNumber();
		tempApkFilePrefix = tempApkFilePrefix.replaceAll("\\W+", "_"); // replaces everything that is not a letter,
																		// number or underscore with an underscore

		tempApkFile = File.createTempFile(tempApkFilePrefix, TEMP_APK_FILE_SUFFIX);
		tempApkFileOutputStream = new BufferedOutputStream(new FileOutputStream(tempApkFile));
	}

	@Override
	public void appendToAPK(byte[] bytes) throws RemoteException, IOException
	{
		if (tempApkFile == null || tempApkFileOutputStream == null)
		{
			throw new IllegalStateException("Temp .apk file should be created (by calling initAPKInstall()) before any calls to appendToAPK() and buildAndInstallAPK().");
		}
		tempApkFileOutputStream.write(bytes);
	}

	@Override
	public void buildAndInstallAPK() throws RemoteException, IOException, CommandFailedException
	{
		if (tempApkFile == null || tempApkFileOutputStream == null)
		{
			throw new IllegalStateException("Temp .apk file should be created (by calling initAPKInstall()) before any calls to appendToAPK() and buildAndInstallAPK().");
		}

		try
		{
			tempApkFileOutputStream.flush();
			tempApkFileOutputStream.close();
			tempApkFileOutputStream = null;
			String absolutePathToApk = tempApkFile.getAbsolutePath();

			String installResult = wrappedDevice.installPackage(absolutePathToApk, true /* force reinstall */);
			discardAPK();

			if (installResult != null)
			{
				LOGGER.error("PacketManager installation returned error code '" + installResult + "'.");
				throw new CommandFailedException("PacketManager installation returned error code '" + installResult
						+ "'.");
			}
		}
		catch (InstallException e)
		{
			LOGGER.error("Installing apk failed.", e);
			throw new CommandFailedException(	"Installing .apk file failed. See the enclosed exception for more information.",
												e);
		}
	}

	@Override
	public void discardAPK() throws RemoteException, IOException
	{
		if (tempApkFileOutputStream != null)
		{
			tempApkFileOutputStream.close();
			tempApkFileOutputStream = null;
		}

		if (tempApkFile != null)
		{
			if (tempApkFile.exists())
			{
				tempApkFile.delete();
			}
			tempApkFile = null;
		}
	}

	@Override
	public abstract void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException;

	@Override
	public abstract void setBatteryLevel(int level) throws RemoteException;

	@Override
	public String getUiXml() throws RemoteException, CommandFailedException
	{
		String dumpCommand = "uiautomator dump " + XMLDUMP_REMOTE_FILE_NAME;
		// TODO VALIDATE GETUIXML on AbstractWrapDevice
		executeShellCommand(dumpCommand);

		StringBuilder uiDumpBuilder = new StringBuilder();

		try
		{
			wrappedDevice.pullFile(XMLDUMP_REMOTE_FILE_NAME, XMLDUMP_LOCAL_FILE_NAME);

			File xmlDumpFile = new File(XMLDUMP_LOCAL_FILE_NAME);
			Scanner xmlDumpFileScanner = new Scanner(xmlDumpFile);
			while (xmlDumpFileScanner.hasNextLine())
			{
				String xmlLine = xmlDumpFileScanner.nextLine();
				uiDumpBuilder.append(xmlLine);
			}
		}
		catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e)
		{
			LOGGER.error("UI dump failed.", e);
			throw new CommandFailedException("UI dump failed. See the enclosed exception for more information.", e);
		}

		String uiDumpContents = uiDumpBuilder.toString();
		return uiDumpContents;
	}

	@Override
	public int getNetworkLatency() throws RemoteException
	{
		// TODO implement get network latency
		return 0;
	}

	@Override
	public abstract void setNetworkLatency(int latency) throws RemoteException;

	@Override
	public BatteryState getBatteryState() throws RemoteException
	{
		// TODO implement get battery state method
		return null;
	}

	@Override
	public abstract void setBatteryState(BatteryState state) throws RemoteException;
}
