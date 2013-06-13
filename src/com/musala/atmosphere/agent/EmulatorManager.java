package com.musala.atmosphere.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.util.Pair;

/**
 * 
 * @author georgi.gaydarov
 * 
 */
public class EmulatorManager
{
	// FIXME Path constants should either be loaded from a config file or set dynamically at startup by something
	private final static String ANDROID_TOOL_PATH = "C:\\prj\\Source\\ATMOSPHERE\\AtmosphereAgentManagerCode\\android-tool";

	private final static String ANDROID_TOOLSDIR_PATH = "C:\\Android Development Tools\\sdk\\tools";

	private final static String ANDROID_WORKDIR_PATH = "";

	private final static Logger LOGGER = Logger.getLogger(EmulatorManager.class.getName());

	private final static String LOGGER_FILENAME = "emulatormanager.log";

	private final static String EMULATOR_CPU_ARCHITECTURE = "armeabi-v7a";

	// TODO only one device is currenty supported, for simplicity.
	// this will be easily changed.
	private static final String emulatorName = "TempEmuDevice";

	private static final String EMULATOR_EXECUTABLE = "emulator.exe";

	private static final String ANDROIDTOOL_CLASS = "com.android.sdkmanager.Main";

	private final static EmulatorManager emulatorManagerInstance = new EmulatorManager();

	private static AgentManager agentManagerReference;

	private DeviceParameters emulatorParameters;

	private Thread emulatorThread;

	public static EmulatorManager getInstance()
	{
		return emulatorManagerInstance;
	}

	public static void registerAgentManagerReference(AgentManager agentManager)
	{
		agentManagerReference = agentManager;
	}

	private EmulatorManager()
	{
		// Set up the logger
		try
		{
			Handler fileHandler = new FileHandler(LOGGER_FILENAME);
			LOGGER.addHandler(fileHandler);
		}
		catch (SecurityException | IOException e)
		{
			// Could not create the log file.
			// Well, we can't log this...
			e.printStackTrace();
		}

		LOGGER.setLevel(Level.ALL);
	}

	public void createAndStartEmulator(DeviceParameters parameters)
	{
		emulatorParameters = parameters;
		Pair<Integer, Integer> resolution = parameters.getResolution();
		String screenResolutionString = resolution.getKey() + "x" + resolution.getValue();

		// ignore these constants for now
		String target = "1"; // TODO change to get from the parameters.getApiLevel();
		String abi = EMULATOR_CPU_ARCHITECTURE; // "x86";

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

		List<String> runCommandParameters = new LinkedList<String>();
		runCommandParameters.add("-avd " + emulatorName);
		runCommandParameters.add("-memory " + emulatorParameters.getRam());
		runCommandParameters.add("-dpi-device " + emulatorParameters.getDpi());

		try
		{
			String returnValue = sendCommandToAndroidTool(createCommand, "\n");
			LOGGER.log(Level.INFO, "sendCommandToAndroidTool returned :\n" + returnValue);

			// Running the emulator must be done in a separate thread.
			// Otherwise, our code will block until the emulator is closed because
			// of the process.waitFor() function that is being called in methods below.
			final List<String> finalizedRunCommandParameters = runCommandParameters;
			emulatorThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						String returnValue = sendCommandToEmulatorTool(finalizedRunCommandParameters, "");
						LOGGER.log(Level.INFO, "sendCommandToEmulatorTool returned :\n" + returnValue);
					}
					catch (IOException e)
					{
						LOGGER.log(	Level.SEVERE,
									"Running the emulator tool in a separate thread resulted in exception.",
									e);
						// TODO this will change when emulator creation mechanism is discussed
						e.printStackTrace();
					}
				}
			});
			emulatorThread.setName("Emulator starter thread");
			emulatorThread.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void closeAndEraseEmulator()
	{
		IDevice emulator = agentManagerReference.getDeviceByEmulatorName(emulatorName);
		EmulatorConsole emulatorConsole = EmulatorConsole.getConsole(emulator);
		emulatorConsole.kill();

		// wait for the emulator to exit
		try
		{
			emulatorThread.join();
		}
		catch (InterruptedException e1)
		{
		}

		try
		{
			String returnValue = sendCommandToAndroidTool("delete avd -n " + emulatorName, "");
			LOGGER.log(Level.INFO, "sendCommandToAndroidTool returned :\n" + returnValue);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
	 * Executes a command on the system via the {@link ProcessBuilder ProcessBuilder} class.
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
			LOGGER.log(Level.WARNING, "Process execution wait was interrupted.", e);
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE, "Running " + commandDescription + " resulted in an IOException.", e);
			throw e;
		}

		if (process.exitValue() != 0)
		{
			LOGGER.log(Level.SEVERE, commandDescription + " return code is nonzero for the command line '" + command
					+ "'.");
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
			LOGGER.log(Level.SEVERE, "Reading the shell response of " + commandDescription + " in an IOException.", e);
			throw e;
		}

		return responseBuilder.toString();
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
			LOGGER.log(Level.WARNING, "Process execution wait was interrupted.", e);
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE, "Running " + commandDescription + " resulted in an IOException.", e);
			throw e;
		}

		if (process.exitValue() != 0)
		{
			LOGGER.log(Level.SEVERE, commandDescription + " return code is nonzero for the command line '" + command
					+ "'.");
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
			LOGGER.log(Level.SEVERE, "Reading the shell response of " + commandDescription + " in an IOException.", e);
			throw e;
		}

		return responseBuilder.toString();
	}
}
