package com.musala.atmosphere.agent.devicewrapper.settings;

/**
 * Thrown when the parsing of the settings is not successful.
 * 
 * @author nikola.taushanov
 * 
 */
public class SettingsParsingException extends Exception
{
	private static final long serialVersionUID = -7142031090292495482L;

	public SettingsParsingException(String message)
	{
		super(message);
	}

	public SettingsParsingException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
