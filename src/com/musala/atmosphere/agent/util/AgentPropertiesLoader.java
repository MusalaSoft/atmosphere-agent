package com.musala.atmosphere.agent.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Read and writes Agent properties from and to config file.
 * 
 * @author valyo.yolovski
 * 
 */

public class AgentPropertiesLoader
{
	private static final String AGENT_PROPERTIES_FILE = "./agent.properties";

	private final static Logger LOGGER = Logger.getLogger(AgentPropertiesLoader.class.getCanonicalName());

	// private static Properties defaultPoperties = null;

	private static Properties configProperties = null;

	private static Properties readInConfigFile(String fileName)
	{
		try
		{
			Properties properties = new Properties();
			FileReader readInPropertiesFile = new FileReader(fileName);
			properties.load(readInPropertiesFile);
			LOGGER.info(fileName + " has been loaded.");
			return properties;
		}
		catch (IOException e)
		{
			LOGGER.fatal(fileName + " loading failed.", e);
			throw new RuntimeException("Could not load config file.");
		}
	}

	/**
	 * Returns the desired property from the config file in String type. If there is no user config file, default values
	 * are returned.
	 * 
	 * @param property
	 *        The Agent property to be returned.
	 * @return Returns the desired agent property value. Returns String properties only!
	 */
	private synchronized static String getPropertyString(AgentProperties property)
	{
		if (configProperties == null)
		{
			configProperties = readInConfigFile(AGENT_PROPERTIES_FILE);
		}
		String propertyString = property.toString();
		String agentProperty = configProperties.getProperty(propertyString);
		if (agentProperty == null)
		{
			LOGGER.fatal("Property " + propertyString + " could not be found in the properties file.");
			throw new RuntimeException("Property " + propertyString + " could not be found in the properties file.");
		}
		return agentProperty;
	}

	/**
	 * Returns the path to ADB from the config file.
	 * 
	 * @return
	 */
	public static String getPathToADB()
	{
		String returnValueString = getPropertyString(AgentProperties.PATH_TO_ADB);
		return returnValueString;
	}

	/**
	 * Returns the timeout for connecting with the Android Debug Bridge from the config file.
	 * 
	 * @return
	 */
	public static int getADBridgeTimeout()
	{
		String returnValueString = getPropertyString(AgentProperties.ADBRIDGE_TIMEOUT);
		int returnValue = Integer.parseInt(returnValueString);
		return returnValue;
	}

	/**
	 * Returns the Agent RMI port from the config file.
	 * 
	 * @return
	 */
	public static int getAgentRmiPort()
	{
		String returnValueString = getPropertyString(AgentProperties.AGENT_RMI_PORT);
		int returnValue = Integer.parseInt(returnValueString);
		return returnValue;
	}

	/**
	 * Returns the waiting time for the creation of the emulator from the config file.
	 * 
	 * @return
	 */
	public static int getEmulatorCreationWait()
	{
		String returnValueString = getPropertyString(AgentProperties.EMULATOR_CREATION_WAIT);
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
	 * Returns the path to android tool from the config file.
	 * 
	 * @return
	 */
	public static String getAndroidToolPath()
	{
		String returnValueString = getPropertyString(AgentProperties.ANDROID_TOOL_PATH);
		return returnValueString;
	}

	/**
	 * Returns the path to the Android tools directory from the config file.
	 * 
	 * @return
	 */
	public static String getAndroidToolsDirPath()
	{
		String returnValueString = getPropertyString(AgentProperties.ANDROID_TOOLSDIR_PATH);
		return returnValueString;
	}

	/**
	 * Returns the path to the Android working directory from the config file.
	 * 
	 * @return
	 */
	public static String getAndroidWorkDirPath()
	{
		String returnValueString = getPropertyString(AgentProperties.ANDROID_WORKDIR_PATH);
		return returnValueString;
	}

	/**
	 * Returns the name of the Android tools class from the config file.
	 * 
	 * @return
	 */
	public static String getAndroidToolClass()
	{
		String returnValueString = getPropertyString(AgentProperties.ANDROIDTOOL_CLASS);
		return returnValueString;
	}

	/**
	 * Returns the path of the emulator executable file from the config file..
	 * 
	 * @return
	 */
	public static String getEmulatorExecutable()
	{
		String returnValueString = getPropertyString(AgentProperties.EMULATOR_EXECUTABLE);
		return returnValueString;
	}

}
