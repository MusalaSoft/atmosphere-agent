package com.musala.atmosphere.agent.devicewrapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.DevicePropertyStringConstants;
import com.musala.atmosphere.agent.util.DeviceScreenResolutionParser;
import com.musala.atmosphere.agent.util.MemoryUnitConverter;
import com.musala.atmosphere.commons.sa.BatteryState;
import com.musala.atmosphere.commons.sa.DeviceInformation;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.util.Pair;

public abstract class AbstractWrapDevice extends UnicastRemoteObject implements IWrapDevice
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9122701818928360023L;

	// WARNING : do not change the remote folder unless you really know what you are doing.
	private static final String XMLDUMP_REMOTE_FILE_NAME = "/data/local/tmp/uidump.xml";

	private static final String XMLDUMP_LOCAL_FILE_NAME = "uidump.xml";

	private static final String TEMP_APK_FILE_SUFFIX = ".apk";

	private File tempApkFile;

	private OutputStream tempApkFileOutputStream;

	protected IDevice wrappedDevice;

	private final static Logger LOGGER = Logger.getLogger(AgentManager.class.getName());

	public AbstractWrapDevice(IDevice deviceToWrap) throws RemoteException
	{
		wrappedDevice = deviceToWrap;
		// TODO Set up logger
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
			level = wrappedDevice.getBatteryLevel();
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
	public int getFreeRAM() throws RemoteException
	{
		// TODO implement get free ram
		return 0;
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
	public List<String> executeSequenceOfShellCommands(List<String> commandsList) throws RemoteException
	{
		// TODO implement execute sequence of shell commands
		return null;
	}

	@Override
	public DeviceInformation getDeviceInformation() throws RemoteException
	{
		// TODO surround all data set procedures with try/catch
		// TODO just check the propmap and populate it with the fallbacks if something is wrong
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
		String cpu = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_CPU_TYPE.toString());
		deviceInformation.setCpu(cpu);

		// Density
		String lcdDensityString = DeviceInformation.FALLBACK_DISPLAY_DENSITY.toString();
		if (wrappedDevice.isEmulator())
		{
			lcdDensityString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_EMUDEVICE_LCD_DENSITY.toString());
		}
		else
		{
			lcdDensityString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_REALDEVICE_LCD_DENSITY.toString());
		}
		deviceInformation.setDpi(Integer.parseInt(lcdDensityString));

		// Model
		deviceInformation.setModel(devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_PRODUCT_MODEL.toString()));

		// OS
		deviceInformation.setOs(devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_OS_VERSION.toString()));

		// RAM
		String ramMemoryString = DeviceInformation.FALLBACK_RAM_AMOUNT.toString();
		if (wrappedDevice.isEmulator())
		{
			// FIXME get the ram for emulators too.
		}
		else
		{
			ramMemoryString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_REALDEVICE_RAM.toString());
		}
		deviceInformation.setRam(MemoryUnitConverter.convertMemoryToMB(ramMemoryString));

		// Resolution
		deviceInformation.setResolution(DeviceInformation.FALLBACK_SCREEN_RESOLUTION);
		try
		{
			CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
			wrappedDevice.executeShellCommand("dumpsys window policy", outputReceiver);

			String shellResponse = outputReceiver.getOutput();
			deviceInformation.setResolution(DeviceScreenResolutionParser.parseScreenResolutionFromShell(shellResponse));

		}
		catch (ShellCommandUnresponsiveException | TimeoutException | AdbCommandRejectedException | IOException e)
		{
			// Shell command execution failed.
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, "Shell command execution failed.", e);
		}
		catch (StringIndexOutOfBoundsException e)
		{
			LOGGER.log(Level.WARNING, "Parsing shell response failed when attempting to get device screen size.");
		}

		return deviceInformation;
	}

	@Override
	public Byte[] getScreenshot() throws RemoteException
	{
		// TODO implement get screenshot
		return null;
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

			wrappedDevice.installPackage(absolutePathToApk, true /* force reinstall */);

			discardAPK();
		}
		catch (InstallException e)
		{
			// TODO log this
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
			// TODO log this
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
