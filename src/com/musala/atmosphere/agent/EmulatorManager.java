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

	private final static EmulatorManager emulatorManagerInstance = new EmulatorManager();

	private static AgentManager agentManagerReference;

	// TODO only one device is currenty supported, for simplicity.
	// this will be easily changed.
	private static final String emulatorName = "TempEmuDevice";

	private DeviceParameters emulatorParameters;

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
		String abi = "armeabi"; // "x86";

		StringBuilder createCommandBuilder = new StringBuilder();
		createCommandBuilder.append("create avd -n ");
		createCommandBuilder.append(emulatorName);
		createCommandBuilder.append(" -t ");
		createCommandBuilder.append(target);
		createCommandBuilder.append(" -b ");
		createCommandBuilder.append(abi);
		createCommandBuilder.append(" -s ");
		createCommandBuilder.append(screenResolutionString);

		String createCommand = createCommandBuilder.toString();

		StringBuilder runCommandBuilder = new StringBuilder();
		runCommandBuilder.append("-avd ");
		runCommandBuilder.append(emulatorName);
		runCommandBuilder.append(" -memory ");
		runCommandBuilder.append(emulatorParameters.getRam());
		runCommandBuilder.append(" -dpi-device ");
		runCommandBuilder.append(emulatorParameters.getDpi());

		try
		{
			List<String> input = new LinkedList<String>();
			input.add("\n");

			System.out.println(sendCommandToAndroidTool(createCommand, input));

			System.out.println(sendCommandToEmulatorTool("-avd " + emulatorName, new LinkedList<String>()));
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

		try
		{
			System.out.println(sendCommandToAndroidTool("delete avd -n " + emulatorName, new LinkedList<String>()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private String sendCommandToAndroidTool(String command, List<String> inputList) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		builder.append("java -Dcom.android.sdkmanager.toolsdir=\"");
		builder.append(ANDROID_TOOLSDIR_PATH);
		builder.append("\" -Dcom.android.sdkmanager.workdir=\"");
		builder.append(ANDROID_WORKDIR_PATH);
		builder.append("\" -classpath \"");
		builder.append(ANDROID_TOOL_PATH);
		builder.append(File.separator);
		builder.append("lib");
		builder.append(File.separator);
		builder.append("sdkmanager.jar;");
		builder.append(ANDROID_TOOL_PATH);
		builder.append(File.separator);
		builder.append("lib");
		builder.append(File.separator);
		builder.append("swtmenubar.jar;");
		builder.append(ANDROID_TOOL_PATH);
		builder.append(File.separator);
		builder.append("lib");
		builder.append(File.separator);
		builder.append("x86");
		builder.append(File.separator);
		builder.append("swt.jar\" com.android.sdkmanager.Main ");
		builder.append(command);

		String builtCommand = builder.toString();

		return sendCommand(builtCommand, inputList, "android-tool");
	}

	private String sendCommandToEmulatorTool(String command, List<String> inputList) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		builder.append(ANDROID_TOOLSDIR_PATH);
		builder.append("\\emulator.exe ");
		builder.append(command);

		String builtCommand = builder.toString();

		return sendCommand(builtCommand, inputList, "emulator.exe tool");
	}

	/**
	 * 
	 * @param command
	 * @param inputList
	 * @param executableDescription
	 * @return
	 * @throws IOException
	 */
	private String sendCommand(String command, List<String> inputList, String executableDescription) throws IOException
	{
		Process process = null;
		try
		{
			Runtime runtime = Runtime.getRuntime();
			process = runtime.exec(command);
			BufferedWriter inputBuffer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			for (String inputCommand : inputList)
			{
				inputBuffer.write(inputCommand);
			}
			inputBuffer.flush();
			process.waitFor();
		}
		catch (InterruptedException e)
		{
			LOGGER.log(Level.WARNING, "Process execution wait was interrupted.", e);
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE, "Running " + executableDescription + " resulted in an IOException.", e);
			throw e;
		}

		if (process.exitValue() != 0)
		{
			LOGGER.log(Level.SEVERE, executableDescription + " return code is nonzero for the command line '" + command
					+ "'.");
			System.out.println("Process return code is nonzero.");
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
			LOGGER.log(	Level.SEVERE,
						"Reading the shell response of " + executableDescription + " in an IOException.",
						e);
			throw e;
		}

		return responseBuilder.toString();
	}
}
