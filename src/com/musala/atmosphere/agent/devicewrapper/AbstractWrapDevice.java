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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.agent.util.DeviceScreenResolutionParser;
import com.musala.atmosphere.agent.util.MemoryUnitConverter;
import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.DeviceOrientation;
import com.musala.atmosphere.commons.Pair;
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

	private static final String BATTERY_STATE_EXTRACTION_REGEX = "status: (\\d)";

	private static final String DUMP_BATTERY_INFO_COMMAND = "dumpsys battery";

	private static final String AIRPLANE_MODE_COMMAND = "settings put global airplane_mode_on ";

	private static final String AIRPLANE_MODE_INTENT_COMMAND = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state ";

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
		try
		{
			int level = wrappedDevice.getBatteryLevel(0 /* renew value, don't return old one */);
			return level;
		}
		catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e)
		{
			// Redirect the exception to the server
			throw new CommandFailedException(	"getBatteryLevel failed. See the enclosed exception for more information.",
												e);
		}
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
		final int COMMAND_EXECUTION_TIMEOUT = AgentPropertiesLoader.getCommandExecutionTimeout();

		try
		{
			CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
			wrappedDevice.executeShellCommand(command, outputReceiver, COMMAND_EXECUTION_TIMEOUT);

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
	public void discardAPK() throws RemoteException
	{
		if (tempApkFileOutputStream != null)
		{
			try
			{
				tempApkFileOutputStream.close();
			}
			catch (IOException e)
			{
				// closing failed, it was never functional. nothing to do here.
			}
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
	public String getUiXml() throws RemoteException, CommandFailedException
	{
		String dumpCommand = "uiautomator dump " + XMLDUMP_REMOTE_FILE_NAME;
		// TODO VALIDATE GETUIXML on AbstractWrapDevice
		executeShellCommand(dumpCommand);

		try
		{
			wrappedDevice.pullFile(XMLDUMP_REMOTE_FILE_NAME, XMLDUMP_LOCAL_FILE_NAME);

			File xmlDumpFile = new File(XMLDUMP_LOCAL_FILE_NAME);
			Scanner xmlDumpFileScanner = new Scanner(xmlDumpFile, "UTF-8");
			xmlDumpFileScanner.useDelimiter("\\Z");
			String uiDumpContents = xmlDumpFileScanner.next();
			xmlDumpFileScanner.close();
			return uiDumpContents;
		}
		catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e)
		{
			LOGGER.error("UI dump failed.", e);
			throw new CommandFailedException("UI dump failed. See the enclosed exception for more information.", e);
		}
	}

	@Override
	public int getNetworkLatency() throws RemoteException
	{
		// TODO implement get network latency
		return 0;
	}

	@Override
	public BatteryState getBatteryState() throws RemoteException, CommandFailedException
	{
		String response = executeShellCommand(DUMP_BATTERY_INFO_COMMAND);
		Pattern extractionPattern = Pattern.compile(BATTERY_STATE_EXTRACTION_REGEX);
		Matcher stateMatch = extractionPattern.matcher(response);

		if (!stateMatch.find())
		{
			throw new CommandFailedException("Getting battery state failed.");
		}

		int stateId = Integer.parseInt(stateMatch.group(1));
		BatteryState currentBatteryState = BatteryState.getStateById(stateId);

		return currentBatteryState;
	}

	@Override
	public boolean getPowerState() throws RemoteException, CommandFailedException
	{
		final String DUMP_POWER_INFO_COMMAND = "dumpsys power";
		String response = executeShellCommand(DUMP_POWER_INFO_COMMAND);

		final String POWER_STATE_EXTRACTION_REGEX = "mPlugType=(\\d)";
		Pattern extractionPattern = Pattern.compile(POWER_STATE_EXTRACTION_REGEX);
		Matcher stateMatch = extractionPattern.matcher(response);

		if (!stateMatch.find())
		{
			throw new CommandFailedException("Getting power state failed.");
		}

		int powerStateInt = Integer.parseInt(stateMatch.group(1));
		boolean powerState;
		powerState = powerStateInt != 0;
		return powerState;
	}

	@Override
	public abstract void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException, CommandFailedException;

	@Override
	public abstract void setBatteryLevel(int level) throws RemoteException, CommandFailedException;

	@Override
	public abstract void setNetworkLatency(int latency) throws RemoteException;

	@Override
	public abstract void setBatteryState(BatteryState state) throws RemoteException, CommandFailedException;

	@Override
	public abstract void setPowerState(boolean state) throws RemoteException, CommandFailedException;

	@Override
	public void setAirplaneMode(boolean airplaneMode) throws CommandFailedException, RemoteException
	{
		String deviceOs = getDeviceInformation().getOS();

		final String DEVICE_OS_PATTERN = "(4\\.[2-9]+.*)|([5-9]+\\.\\d+.*)";
		Pattern osPattern = Pattern.compile(DEVICE_OS_PATTERN);
		Matcher osMatcher = osPattern.matcher(deviceOs);

		final String INTENT_COMMAND_RESPONSE = "Broadcast completed: result=0";
		Pattern intentCommandResponsePattern = Pattern.compile(INTENT_COMMAND_RESPONSE);

		if (osMatcher.find())
		{
			List<String> commands = new ArrayList<String>();

			if (airplaneMode)
			{
				commands.add(AIRPLANE_MODE_COMMAND + "1");
				commands.add(AIRPLANE_MODE_INTENT_COMMAND + "true");
			}
			else
			{
				commands.add(AIRPLANE_MODE_COMMAND + "0");
				commands.add(AIRPLANE_MODE_INTENT_COMMAND + "false");
			}

			List<String> response = executeSequenceOfShellCommands(commands);

			String commandResponse = response.get(0);
			String intentCommandResponse = response.get(1);

			Matcher intentCommandResponseMatcher = intentCommandResponsePattern.matcher(intentCommandResponse);

			boolean isCommandSuccessful = commandResponse.isEmpty();
			boolean isIntentCommandSuccessful = intentCommandResponseMatcher.find();

			if (!(isCommandSuccessful && isIntentCommandSuccessful))
			{
				throw new CommandFailedException("Setting airplane mode failed.");
			}
		}
		else
		{
			/*
			 * TODO implement setting airplane mode for devices with OS older than Android Jelly Bean 4.2 - can be done
			 * by updating a row in /data/data/com.android.providers.settings/databases/settings.db global table, but
			 * device needs to be rooted. Installing app on the device is an option. For some helpful resources refer to
			 * ticket #2289.
			 */
			throw new CommandFailedException("Can not set airplane mode for device with OS " + deviceOs + ".");
		}
	}

	@Override
	public abstract void setOrientation(DeviceOrientation deviceOrientation)
		throws RemoteException,
			CommandFailedException;
}
