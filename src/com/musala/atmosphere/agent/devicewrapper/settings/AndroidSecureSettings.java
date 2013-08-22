package com.musala.atmosphere.agent.devicewrapper.settings;

/**
 * Secure system settings, containing system preferences that applications can read but are not allowed to write. These
 * are for preferences that the user must explicitly modify through the system UI or specialized APIs for those values,
 * not modified directly by applications. We can get or change these settings using the 'content' command utility
 * provided by Android's shell.
 * 
 * @see <a href="http://developer.android.com/reference/android/provider/Settings.Secure.html">developer.android.com</a>
 * 
 * @author nikola.taushanov
 * 
 */
public enum AndroidSecureSettings implements IAndroidSettings
{
	// TODO: Implement someday when required.
	NONE("");

	private String settingName;

	private static final String CONTENT_URI = "content://settings/secure";

	private AndroidSecureSettings(String settingName)
	{
		this.settingName = settingName;
	}

	@Override
	public String toString()
	{
		return settingName;
	}

	@Override
	public String getContentUri()
	{
		return CONTENT_URI;
	}
}
