package com.musala.atmosphere.agent.util;

import com.musala.atmosphere.commons.util.PropertiesLoader;

/**
 * Reads properties from the Agent properties config file.
 * 
 * @author valyo.yolovski
 * 
 */

public class AgentPropertiesLoader
{
	private static final String AGENT_PROPERTIES_FILE = "./agent.properties";

	/**
	 * Returns the desired property from the config file in String type. If there is no user config file, default values
	 * are returned.
	 * 
	 * @param property
	 *        - the Agent property to be returned.
	 * @return the desired agent property value. Returns String properties only!
	 */
	private synchronized static String getPropertyString(AgentProperties property)
	{
		PropertiesLoader propertiesLoader = PropertiesLoader.getInstance(AGENT_PROPERTIES_FILE);
		String propertyString = property.toString();
		String resultProperty = propertiesLoader.getPropertyString(propertyString);
		return resultProperty;
	}

	/**
	 * Returns the path to ADB from the config file.
	 * 
	 * @return - the path to the Android Debug Bridge.
	 */
	public static String getADBPath()
	{
		String returnValueString = getPropertyString(AgentProperties.ADB_PATH);
		return returnValueString;
	}

	/**
	 * Returns the timeout for connecting with the Android Debug Bridge from the config file.
	 * 
	 * @return - int, the timeout in miliseconds.
	 */
	public static int getADBConnectionTimeout()
	{
		String returnValueString = getPropertyString(AgentProperties.ADB_CONNECTION_TIMEOUT);
		int returnValue = Integer.parseInt(returnValueString);
		return returnValue;
	}

	/**
	 * Returns the Agent RMI port from the config file.
	 * 
	 * @return - the port on which the Agent is published in RMI.
	 */
	public static int getAgentRmiPort()
	{
		String returnValueString = getPropertyString(AgentProperties.AGENT_RMI_PORT);
		int returnValue = Integer.parseInt(returnValueString);
		return returnValue;
	}

	/**
	 * Returns the timeout for the creation of the emulator form the config file.
	 * 
	 * @return
	 */
	public static int getEmulatorCreationWaitTimeout()
	{
		String returnValueString = getPropertyString(AgentProperties.EMULATOR_CREATION_WAIT_TIMEOUT);
		int returnValue = Integer.parseInt(returnValueString);
		return returnValue;
	}

	/**
	 * Returns the path to Android tool directory from the config file.
	 * 
	 * @return the path to Android tool directory from the config file
	 */
	public static String getAndroidToolPath()
	{
		String returnValueString = getPropertyString(AgentProperties.ANDROID_TOOL_PATH);
		return returnValueString;
	}

	/**
	 * Returns the path to the Android SDK tools directory from the config file.
	 * 
	 * @return
	 */
	public static String getAndroidSdkToolsDirPath()
	{
		String returnValueString = getPropertyString(AgentProperties.ANDROID_SDK_TOOLS_PATH);
		return returnValueString;
	}

	/**
	 * Returns the path to the Android Tool working directory from the config file.
	 * 
	 * @return the path to the Android tool working directory.
	 */
	public static String getAndroidToolWorkDirPath()
	{
		String returnValueString = getPropertyString(AgentProperties.ANDROID_TOOL_WORKDIR_PATH);
		return returnValueString;
	}

	/**
	 * Returns the name of the Android tools class from the config file.
	 * 
	 * @return - the name of the class.
	 */
	public static String getAndroidToolClass()
	{
		String returnValueString = getPropertyString(AgentProperties.ANDROID_TOOL_CLASS);
		return returnValueString;
	}

	/**
	 * Returns the path of the emulator executable file from the config file.
	 * 
	 * @return - String, the path to the directory.
	 */
	public static String getEmulatorExecutable()
	{
		String returnValueString = getPropertyString(AgentProperties.EMULATOR_EXECUTABLE_PATH);
		return returnValueString;
	}

	/**
	 * Returns the timeout for command execution.
	 * 
	 * @return - int, the timeout for command execution.
	 */
	public static int getCommandExecutionTimeout()
	{
		String returnValueString = getPropertyString(AgentProperties.COMMAND_EXECUTION_TIMEOUT);
		int returnValueInt = Integer.parseInt(returnValueString);
		return returnValueInt;
	}
}
