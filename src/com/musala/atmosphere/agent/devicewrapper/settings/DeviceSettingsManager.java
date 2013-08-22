package com.musala.atmosphere.agent.devicewrapper.settings;

import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.AbstractWrapDevice;
import com.musala.atmosphere.commons.CommandFailedException;

/**
 * Provides better interface for getting and inserting all kinds of Android device settings.
 * 
 * @author nikola.taushanov
 * 
 */
public class DeviceSettingsManager
{
	private final static Logger LOGGER = Logger.getLogger(DeviceSettingsManager.class.getCanonicalName());

	private AbstractWrapDevice wrappedDevice;

	public DeviceSettingsManager(AbstractWrapDevice wrappedDevice)
	{
		this.wrappedDevice = wrappedDevice;
	}

	/**
	 * Retrieves a single setting value as floating point number or returns default value if it is not found.
	 * 
	 * @param setting
	 * @param defaultValue
	 * @return
	 */

	public float getFloat(IAndroidSettings setting, float defaultValue)
	{
		try
		{
			float result = getFloat(setting);
			return result;
		}
		catch (SettingsParsingException e)
		{
			return defaultValue;
		}
	}

	/**
	 * Retrieves a single setting value as floating point number.
	 * 
	 * @param setting
	 * @return
	 * @throws SettingsParsingException
	 */
	public float getFloat(IAndroidSettings setting) throws SettingsParsingException
	{
		String settingStringValue = getSetting(setting);
		try
		{
			float settingValue = Float.parseFloat(settingStringValue);
			return settingValue;
		}
		catch (NumberFormatException e)
		{
			throw new SettingsParsingException(e.getMessage());
		}
	}

	/**
	 * Retrieves a single setting value as integer or returns default value if it is not found.
	 * 
	 * @param setting
	 * @param defaultValue
	 * @return
	 */
	public int getInt(IAndroidSettings setting, int defaultValue)
	{
		try
		{
			int result = getInt(setting);
			return result;
		}
		catch (SettingsParsingException e)
		{
			return defaultValue;
		}
	}

	/**
	 * Retrieves a single setting value as integer.
	 * 
	 * @param setting
	 * @return
	 * @throws SettingsParsingException
	 */
	public int getInt(IAndroidSettings setting) throws SettingsParsingException
	{
		String settingStringValue = getSetting(setting);
		try
		{
			int settingValue = Integer.parseInt(settingStringValue);
			return settingValue;
		}
		catch (NumberFormatException e)
		{
			throw new SettingsParsingException(e.getMessage());
		}
	}

	/**
	 * Retrieves a single setting value as long or returns default value if it is not found.
	 * 
	 * @param setting
	 * @param defaultValue
	 * @return
	 */
	public long getLong(IAndroidSettings setting, long defaultValue)
	{
		try
		{
			long result = getLong(setting);
			return result;
		}
		catch (SettingsParsingException e)
		{
			return defaultValue;
		}
	}

	/**
	 * Retrieves a single setting value as long.
	 * 
	 * @param setting
	 * @return
	 * @throws SettingsParsingException
	 */
	public long getLong(IAndroidSettings setting) throws SettingsParsingException
	{
		String settingStringValue = getSetting(setting);
		try
		{
			long settingValue = Long.parseLong(settingStringValue, 0);
			return settingValue;
		}
		catch (NumberFormatException e)
		{
			throw new SettingsParsingException(e.getMessage());
		}
	}

	/**
	 * Retrieves a single setting value as String or returns default value if it is not found.
	 * 
	 * @param setting
	 * @return
	 */
	public String getString(IAndroidSettings setting, String defaultValue)
	{
		String settingValue = getSetting(setting);

		if (settingValue != null)
		{
			return settingValue;
		}
		else
		{
			return defaultValue;
		}
	}

	/**
	 * Retrieves a single setting value as String.
	 * 
	 * @param setting
	 * @return
	 */
	public String getString(IAndroidSettings setting)
	{
		String settingValue = getSetting(setting);
		return settingValue;
	}

	/**
	 * Updates a single settings value as a floating point number.
	 * 
	 * @param setting
	 * @param value
	 */
	public void putFloat(IAndroidSettings setting, float value)
	{
		putSetting(setting, "f", Float.toString(value));
	}

	/**
	 * Updates a single settings value as integer.
	 * 
	 * @param setting
	 * @param value
	 */
	public void putInt(IAndroidSettings setting, int value)
	{
		putSetting(setting, "i", Integer.toString(value));
	}

	/**
	 * Updates a single settings value as long.
	 * 
	 * @param setting
	 * @param value
	 */
	public void putLong(IAndroidSettings setting, long value)
	{
		putSetting(setting, "l", Long.toString(value));
	}

	/**
	 * Updates a single settings value as String.
	 * 
	 * @param setting
	 * @param value
	 */
	public void putString(IAndroidSettings setting, String value)
	{
		putSetting(setting, "s", value);
	}

	private String getSetting(IAndroidSettings setting)
	{
		StringBuilder contentShellCommand = new StringBuilder();
		contentShellCommand.append("content query --uri " + setting.getContentUri());
		contentShellCommand.append(" --projection value");
		contentShellCommand.append(" --where \"name=\'" + setting + "\'\"");

		String shellCommandResult = "";
		try
		{
			shellCommandResult = wrappedDevice.executeShellCommand(contentShellCommand.toString());
		}
		catch (CommandFailedException e)
		{
			LOGGER.fatal("Shell command execution failed.", e);
		}
		catch (RemoteException e)
		{
			// Can never be thrown.
		}

		Pattern returnValuePattern = Pattern.compile("value=(.*)$");
		Matcher returnValueMatcher = returnValuePattern.matcher(shellCommandResult);

		if (returnValueMatcher.find())
		{
			return returnValueMatcher.group(1);
		}
		else
		{
			return null;
		}
	}

	private void putSetting(IAndroidSettings setting, String valueType, String value)
	{
		StringBuilder contentShellCommand = new StringBuilder();
		contentShellCommand.append("content insert --uri " + setting.getContentUri());
		contentShellCommand.append(" --bind name:s:" + setting);
		contentShellCommand.append(" --bind value:" + valueType + ":" + value);

		try
		{
			wrappedDevice.executeShellCommand(contentShellCommand.toString());
		}
		catch (CommandFailedException e)
		{
			LOGGER.fatal("Shell command execution failed.", e);
		}
		catch (RemoteException e)
		{
			// Can never be thrown.
		}
	}
}
