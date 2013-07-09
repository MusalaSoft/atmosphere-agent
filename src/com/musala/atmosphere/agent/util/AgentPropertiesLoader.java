package com.musala.atmosphere.agent.util;

import java.io.FileReader;
import java.io.FileWriter;
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

	private static final String DEFAULT_CONFIG = "./defaultAgent.properties";

	private static Properties defaultPoperties = null;

	private static Properties configProperties = null;

	/**
	 * Sets Agent properties in config file.
	 * 
	 * @param name
	 *        the AgentProperty to be set
	 * @param value
	 *        the value of the property being set
	 * @throws IOException
	 */
	public synchronized static void setProperty(AgentProperties name, String value) throws IOException
	{
		String propertyString = name.toString();
		try
		{
			if (configProperties == null)
			{
				configProperties = readInConfigFile(AGENT_PROPERTIES_FILE);
			}
			FileWriter writeToPropertiesFile = new FileWriter(AGENT_PROPERTIES_FILE);
			configProperties.setProperty(propertyString, value);
			configProperties.store(writeToPropertiesFile, null /* comments on the property stored, not needed */);
			writeToPropertiesFile.flush();
			writeToPropertiesFile.close();
			LOGGER.info("Property has been written to the config file.");
		}
		catch (IOException e)
		{
			LOGGER.fatal("Could not open load properties file.", e);
			throw e;
		}
	}

	private static Properties readInConfigFile(String fileName)
	{
		if (defaultPoperties == null)
		{
			try
			{
				Properties properties = new Properties();
				FileReader readInPropertiesFile = new FileReader(DEFAULT_CONFIG);
				properties.load(readInPropertiesFile);
				defaultPoperties = properties;
				LOGGER.info(DEFAULT_CONFIG + " has been loaded.");
			}
			catch (IOException e)
			{
				LOGGER.warn("Could not load default propeprties file.");
				throw new RuntimeException("Could not load default config file.");
			}
		}

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
			LOGGER.warn(fileName + " loading failed.", e);
			return defaultPoperties;
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
	public synchronized static String getPropertyString(AgentProperties property)
	{
		if (configProperties == null)
		{
			configProperties = readInConfigFile(AGENT_PROPERTIES_FILE);
		}
		String propertyString = property.toString();
		String agentProperty = configProperties.getProperty(propertyString);
		if (agentProperty == null)
		{
			agentProperty = defaultPoperties.getProperty(propertyString);
			if (agentProperty == null)
			{
				LOGGER.fatal("Property '" + propertyString
						+ "' could not be found in both the main property file and default properties file.");
				throw new RuntimeException("Property '" + propertyString
						+ "' could not be found in both the main property file and default properties file.");
			}
			LOGGER.warn("Could not find peroperty " + propertyString + ", loaded from the default properties.");
		}
		return agentProperty;
	}

	/**
	 * Returns the desired agent property in int type.
	 * 
	 * @param property
	 *        Agent Property to be returned.
	 * @return Returns integer properties from the Agent Properties config file.
	 */
	public static int getPropertyInt(AgentProperties property)
	{
		String returnValueString = getPropertyString(property);
		int returnValue = Integer.parseInt(returnValueString);
		return returnValue;
	}
}
