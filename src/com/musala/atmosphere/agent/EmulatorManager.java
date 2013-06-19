package com.musala.atmosphere.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.util.Pair;

/**
 * 
 * @author georgi.gaydarov
 * 
 */
public class EmulatorManager implements IDeviceChangeListener
{
	// FIXME Path constants should either be loaded from a config file or set dynamically at startup by something
	private final static String ANDROID_TOOL_PATH = "C:\\prj\\Source\\ATMOSPHERE\\AtmosphereAgentManagerCode\\android-tool";

	private final static String ANDROID_TOOLSDIR_PATH = "C:\\Android Development Tools\\sdk\\tools";

	private final static String ANDROID_WORKDIR_PATH = "";

	private final static Logger LOGGER = Logger.getLogger(EmulatorManager.class.getCanonicalName());

	// TODO change code related to abi selection so it can be done by code
	private final static String EMULATOR_CPU_ARCHITECTURE = "armeabi-v7a";

	private static final String emulatorNamePrefix = "TempEmuDevice";

	private static final String EMULATOR_EXECUTABLE = "emulator.exe";

	private static final String ANDROIDTOOL_CLASS = "com.android.sdkmanager.Main";

	private static EmulatorManager emulatorManagerInstance = null;

	private AndroidDebugBridge androidDebugBridge;

	private List<IDevice> emulatorList = new LinkedList<IDevice>();

	private List<Pair<String, Process>> startedEmulatorsProcessList = new LinkedList<Pair<String, Process>>();

	public static EmulatorManager getInstance()
	{
		if (emulatorManagerInstance == null)
		{
			synchronized (EmulatorManager.class)
			{
				if (emulatorManagerInstance == null)
				{
					emulatorManagerInstance = new EmulatorManager();
					LOGGER.info("Emulator manager instance has been created.");
				}
			}
		}
		return emulatorManagerInstance;
	}

	private EmulatorManager()
	{
		// Register the EmulatorManager for device change events so it can keep track of running emulators.
		AndroidDebugBridge.addDeviceChangeListener(this);

		// Get initial device list
		androidDebugBridge = AndroidDebugBridge.getBridge();
		List<IDevice> initialDeviceList = getInitialDeviceList();
		for (IDevice device : initialDeviceList)
		{
			deviceConnected(device);
		}
	}

	private List<IDevice> getInitialDeviceList()
	{
		// As the AgentmManager is calling methods in this class, it is already done with getting initial devices list.
		// This means we do not have to perform initial devices list checks and timeout loops.
		IDevice[] devicesArray = androidDebugBridge.getDevices();
		return Arrays.asList(devicesArray);
	}

	@Override
	public void deviceChanged(IDevice arg0, int arg1)
	{
		// we do not care is a device state has changed, this has no impact to the emulator list we keep
	}

	@Override
	public void deviceConnected(IDevice connectedDevice)
	{
		if (connectedDevice.isEmulator())
		{
			emulatorList.add(connectedDevice);
			LOGGER.info("Emulator " + connectedDevice.getAvdName() + " connected.");
		}
	}

	@Override
	public void deviceDisconnected(IDevice disconnectedDevice)
	{
		if (disconnectedDevice.isEmulator() && emulatorList.contains(disconnectedDevice))
		{
			emulatorList.remove(disconnectedDevice);
			String removedEmulatorAvdName = disconnectedDevice.getAvdName();
			for (Pair<String, Process> emulatorProcessPair : startedEmulatorsProcessList)
			{
				if (emulatorProcessPair.getKey().equals(removedEmulatorAvdName))
				{
					startedEmulatorsProcessList.remove(emulatorProcessPair);
					LOGGER.info("Emulator " + removedEmulatorAvdName + " disconnected.");
				}
			}
		}
	}

	public String createAndStartEmulator(DeviceParameters parameters) throws IOException
	{
		Pair<Integer, Integer> resolution = parameters.getResolution();
		String screenResolutionString = resolution.getKey() + "x" + resolution.getValue();

		Date now = new Date();
		String emulatorName = emulatorNamePrefix + now.getTime();

		// ignore these constants for now
		String target = "1"; // TODO change to get from the parameters.getApiLevel();
		String abi = EMULATOR_CPU_ARCHITECTURE;

		StringBuilder createCommandBuilder = new StringBuilder();
		createCommandBuilder.append("create avd -n ");
		createCommandBuilder.append(emulatorName);
		createCommandBuilder.append(" -t ");
		createCommandBuilder.append(target);
		createCommandBuilder.append(" -b ");
		createCommandBuilder.append(abi);
		createCommandBuilder.append(" -s ");
		createCommandBuilder.append(screenResolutionString);
		createCommandBuilder.append(" --force"); // force emulator creation, overwrite if emulator with this name exists
													// already.
		String createCommand = createCommandBuilder.toString();
		String createCommandReturnValue = sendCommandToAndroidTool(createCommand, "\n");
		LOGGER.info("Create AVD shell command printed: " + createCommandReturnValue);

		List<String> runCommandParameters = new LinkedList<String>();
		runCommandParameters.add("-avd " + emulatorName);
		runCommandParameters.add("-memory " + parameters.getRam());
		runCommandParameters.add("-dpi-device " + parameters.getDpi());

		Process startedEmulatorProcess = sendCommandToEmulatorToolAndReturn(runCommandParameters, "");
		Pair<String, Process> startedEmulatorProcessPair = new Pair<String, Process>(	emulatorName,
																						startedEmulatorProcess);
		startedEmulatorsProcessList.add(startedEmulatorProcessPair);

		return emulatorName;
	}

	public void closeAndEraseEmulator(IDevice emulator) throws IOException
	{
		String emulatorName = emulator.getAvdName();
		EmulatorConsole emulatorConsole = EmulatorConsole.getConsole(emulator);
		emulatorConsole.kill();

		// wait for the emulator to exit
		for (Pair<String, Process> emulatorProcessPair : startedEmulatorsProcessList)
		{
			if (emulatorProcessPair.getKey().equals(emulatorName))
			{
				Process emulatorProcess = emulatorProcessPair.getValue();
				try
				{
					emulatorProcess.waitFor();
				}
				catch (InterruptedException e)
				{
					// waiting for the emulator closing was interrupted. This can not happen?
					LOGGER.warn("Waiting for emulator to close was interrupted.", e);
				}
				startedEmulatorsProcessList.remove(emulatorProcessPair);
				break;
			}
		}

		String returnValue = sendCommandToAndroidTool("delete avd -n " + emulatorName, "");
		LOGGER.info("Delete AVD shell command printed: " + returnValue);
	}

	/**
	 * Sends a command to the android tool.
	 * 
	 * @param command
	 *        Command to be sent.
	 * @param commandInput
	 *        Input that should be sent to the android tool.
	 * @return STDOUT and STDERR output from the android tool.
	 * @throws IOException
	 */
	private String sendCommandToAndroidTool(String command, String commandInput) throws IOException
	{
		StringBuilder androidToolCommandBuilder = new StringBuilder();
		androidToolCommandBuilder.append("java"); // The android tool is a java application
		androidToolCommandBuilder.append(" \"-Dcom.android.sdkmanager.toolsdir="); // We must set this variable to the
																					// tools SDK folder
		androidToolCommandBuilder.append(ANDROID_TOOLSDIR_PATH);
		androidToolCommandBuilder.append("\" \"-Dcom.android.sdkmanager.workdir="); // We must set this variable to a
																					// desired temp folder
		androidToolCommandBuilder.append(ANDROID_WORKDIR_PATH);
		androidToolCommandBuilder.append("\" -classpath \"");
		androidToolCommandBuilder.append(ANDROID_TOOL_PATH);
		androidToolCommandBuilder.append(File.separator);
		androidToolCommandBuilder.append("lib");
		androidToolCommandBuilder.append(File.separator);
		androidToolCommandBuilder.append("sdkmanager.jar;");
		androidToolCommandBuilder.append(ANDROID_TOOL_PATH);
		androidToolCommandBuilder.append(File.separator);
		androidToolCommandBuilder.append("lib");
		androidToolCommandBuilder.append(File.separator);
		androidToolCommandBuilder.append("swtmenubar.jar;");
		androidToolCommandBuilder.append(ANDROID_TOOL_PATH);
		androidToolCommandBuilder.append(File.separator);
		androidToolCommandBuilder.append("lib");
		androidToolCommandBuilder.append(File.separator);
		androidToolCommandBuilder.append("x86");
		androidToolCommandBuilder.append(File.separator);
		androidToolCommandBuilder.append("swt.jar\" ");
		androidToolCommandBuilder.append(ANDROIDTOOL_CLASS);
		androidToolCommandBuilder.append(" ");
		androidToolCommandBuilder.append(command);

		String builtCommand = androidToolCommandBuilder.toString();

		String returnValue = sendCommandViaRuntime(builtCommand, commandInput, "android-tool");
		return returnValue;
	}

	/**
	 * Sends a command to the emulator executable.
	 * 
	 * @param parameters
	 *        Parameters to be passed to the executable.
	 * @param commandInput
	 *        Input to be sent to the emulator.
	 * @return STDOUT and STDERR of the emulator executable.
	 * @throws IOException
	 */
	private String sendCommandToEmulatorTool(List<String> parameters, String commandInput) throws IOException
	{
		List<String> command = new LinkedList<String>();
		command.add(ANDROID_TOOLSDIR_PATH + File.separator + EMULATOR_EXECUTABLE);
		command.addAll(parameters);

		String returnValue = sendCommandViaProcessBuilder(command, commandInput, "emulator.exe tool");
		return returnValue;
	}

	/**
	 * Sends a command to the emulator executable and returns the starter process.
	 * 
	 * @param parameters
	 *        Parameters to be passed to the executable.
	 * @param commandInput
	 *        Input to be sent to the emulator.
	 * @return {@link Process Process} - started process
	 * @throws IOException
	 */
	private Process sendCommandToEmulatorToolAndReturn(List<String> parameters, String commandInput) throws IOException
	{
		List<String> command = new LinkedList<String>();
		command.add(ANDROID_TOOLSDIR_PATH + File.separator + EMULATOR_EXECUTABLE);
		command.addAll(parameters);

		Process returnProcess = sendCommandViaProcessBuilderAndReturn(command, commandInput, "emulator.exe tool");
		return returnProcess;
	}

	/**
	 * Executes a command on the system via the {@link ProcessBuilder ProcessBuilder} class and waits for it to finish
	 * executing.
	 * 
	 * @param command
	 *        Command name, followed by command arguments.
	 * @param commandInputInput
	 *        that should be sent to the executed command.
	 * @param commandDescription
	 *        Description of the executed command, used in error handling.
	 * @return The STDOUT and STDERR of the executed command.
	 * @throws IOException
	 */
	private String sendCommandViaProcessBuilder(List<String> command, String commandInput, String commandDescription)
		throws IOException
	{
		Process process = null;
		try
		{
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true); // redirect STDERR to STDOUT
			process = processBuilder.start();
			BufferedWriter inputBuffer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			inputBuffer.write(commandInput);
			inputBuffer.flush();
			process.waitFor();
		}
		catch (InterruptedException e)
		{
			LOGGER.warn("Process execution wait was interrupted.", e);
		}
		catch (IOException e)
		{
			LOGGER.fatal("Running " + commandDescription + " resulted in an IOException.", e);
			throw e;
		}

		if (process.exitValue() != 0)
		{
			LOGGER.fatal(commandDescription + " return code is nonzero for the command line '" + command + "'.");
		}

		StringBuilder responseBuilder = new StringBuilder();
		try
		{
			String readLine = "";

			BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while (true)
			{
				readLine = responseBuffer.readLine();
				if (readLine == null)
				{
					break;
				}
				responseBuilder.append(readLine);
				responseBuilder.append('\n');
			}
		}
		catch (IOException e)
		{
			LOGGER.fatal("Reading the shell response of " + commandDescription + " in an IOException.", e);
			throw e;
		}

		return responseBuilder.toString();
	}

	/**
	 * Executes a command via the {@link ProcessBuilder ProcessBuilder} class and returns without waiting for it to
	 * finish executing.
	 * 
	 * @param command
	 *        Command name, followed by command arguments.
	 * @param commandInputInput
	 *        that should be sent to the executed command.
	 * @param commandDescription
	 *        Description of the executed command, used in error handling.
	 * @throws IOException
	 */
	private Process sendCommandViaProcessBuilderAndReturn(	List<String> command,
															String commandInput,
															String commandDescription) throws IOException
	{
		Process process = null;
		try
		{
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true); // redirect STDERR to STDOUT
			process = processBuilder.start();
			BufferedWriter inputBuffer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			inputBuffer.write(commandInput);
			inputBuffer.flush();
		}
		catch (IOException e)
		{
			LOGGER.fatal("Running " + commandDescription + " resulted in an IOException.", e);
			throw e;
		}
		return process;
	}

	/**
	 * Executes a command on the system via the {@link Runtime Runtime} .exec() method.
	 * 
	 * @param command
	 *        Command to be executed (as if being passed to the system shell).
	 * @param commandInput
	 *        Input that should be sent to the executed command.
	 * @param commandDescription
	 *        Description of the executed command, used in error handling.
	 * @return The STDOUT and STDERR of the executed command.
	 * @throws IOException
	 */
	private String sendCommandViaRuntime(String command, String commandInput, String commandDescription)
		throws IOException
	{
		Process process = null;
		try
		{
			Runtime runtime = Runtime.getRuntime();
			process = runtime.exec(command);
			BufferedWriter inputBuffer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			inputBuffer.write(commandInput);
			inputBuffer.flush();
			process.waitFor();
		}
		catch (InterruptedException e)
		{
			LOGGER.warn("Process execution wait was interrupted.", e);
		}
		catch (IOException e)
		{
			LOGGER.fatal("Running " + commandDescription + " resulted in an IOException.", e);
			throw e;
		}

		if (process.exitValue() != 0)
		{
			LOGGER.fatal(commandDescription + " return code is nonzero for the command line '" + command + "'.");
		}

		StringBuilder responseBuilder = new StringBuilder();
		try
		{
			String readLine = "";

			BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while (true)
			{
				readLine = responseBuffer.readLine();
				if (readLine == null)
				{
					break;
				}
				responseBuilder.append(readLine);
				responseBuilder.append('\n');
			}
			responseBuffer = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while (true)
			{
				readLine = responseBuffer.readLine();
				if (readLine == null)
				{
					break;
				}
				responseBuilder.append(readLine);
				responseBuilder.append('\n');
			}
		}
		catch (IOException e)
		{
			LOGGER.fatal("Reading the shell response of " + commandDescription + " in an IOException.", e);
			throw e;
		}

		return responseBuilder.toString();
	}

}
