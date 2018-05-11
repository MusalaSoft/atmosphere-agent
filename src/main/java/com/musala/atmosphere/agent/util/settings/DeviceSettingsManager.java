// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.agent.util.settings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Provides better interface for getting and inserting all kinds of Android device settings.
 *
 * @author nikola.taushanov
 *
 */
public class DeviceSettingsManager {
    private ShellCommandExecutor shellCommandExecutor;

    public DeviceSettingsManager(ShellCommandExecutor shellCommandExecutor) {
        this.shellCommandExecutor = shellCommandExecutor;
    }

    /**
     * Retrieves a single setting value as floating point number or returns default value if it is not found.
     *
     * @param setting
     *        - android setting.
     * @param defaultValue
     *        - default value which should be returned if the retrieving fails.
     * @return Floating point number if the retrieving succeed, defaultValue if it fails.
     */

    public float getFloat(IAndroidSettings setting, float defaultValue) {
        try {
            float result = getFloat(setting);
            return result;
        } catch (SettingsParsingException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves a single setting value as floating point number.
     *
     * @param setting
     *        - android setting..
     * @return Floating point number if the retrieving succeed.
     * @throws SettingsParsingException
     *         - Retrieving the single setting value as floating point number failed.
     */
    public float getFloat(IAndroidSettings setting) throws SettingsParsingException {
        String settingStringValue = getSetting(setting);
        try {
            float settingValue = Float.parseFloat(settingStringValue);
            return settingValue;
        } catch (NumberFormatException e) {
            throw new SettingsParsingException(e.getMessage());
        }
    }

    /**
     * Retrieves a single setting value as integer or returns default value if it is not found.
     *
     * @param setting
     *        - android setting.
     * @param defaultValue
     *        - default value which should be returned if the retrieving fails.
     * @return The single setting value as integer if retrieving succeed, defaultValue if it fails.
     */
    public int getInt(IAndroidSettings setting, int defaultValue) {
        try {
            int result = getInt(setting);
            return result;
        } catch (SettingsParsingException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves a single setting value as integer.
     *
     * @param setting
     *        - android setting.
     * @return The single setting value as integer if the retrieving succeed.
     * @throws SettingsParsingException
     *         - Retrieving the single setting value as integer failed.
     */
    public int getInt(IAndroidSettings setting) throws SettingsParsingException {
        String settingStringValue = getSetting(setting);
        try {
            int settingValue = Integer.parseInt(settingStringValue);
            return settingValue;
        } catch (NumberFormatException e) {
            throw new SettingsParsingException(e.getMessage());
        }
    }

    /**
     * Retrieves a single setting value as long or returns default value if it is not found.
     *
     * @param setting
     *        - android setting.
     * @param defaultValue
     *        - default value which should be returned if the retrieving fails.
     * @return The single setting value as long if retrieving succeed, defaultValue if it fails.
     */
    public long getLong(IAndroidSettings setting, long defaultValue) {
        try {
            long result = getLong(setting);
            return result;
        } catch (SettingsParsingException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves a single setting value as long.
     *
     * @param setting
     *        - android setting.
     * @return The single setting value as long if the retrieving succeed.
     * @throws SettingsParsingException
     *         - Retrieving the single setting value as long failed.
     */
    public long getLong(IAndroidSettings setting) throws SettingsParsingException {
        String settingStringValue = getSetting(setting);
        try {
            long settingValue = Long.parseLong(settingStringValue);
            return settingValue;
        } catch (NumberFormatException e) {
            throw new SettingsParsingException(e.getMessage());
        }
    }

    /**
     * Retrieves a single setting value as String or returns default value if it is not found.
     *
     * @param setting
     *        - android setting.
     * @param defaultValue
     *        - default value which should be returned if the retrieving fails.
     * @return The string value of the setting or defaultValue if the fetching was not successful.
     */
    public String getString(IAndroidSettings setting, String defaultValue) {
        String settingValue = getSetting(setting);

        if (settingValue != null) {
            return settingValue;
        } else {
            return defaultValue;
        }
    }

    /**
     * Retrieves a single setting value as String.
     *
     * @param setting
     *        - android setting.
     * @return The string value of the setting or <code>null</code> if the fetching was not successful.
     */
    public String getString(IAndroidSettings setting) {
        String settingValue = getSetting(setting);
        return settingValue;
    }

    /**
     * Updates a single settings value as a floating point number.
     *
     * @param setting
     *        - android setting.
     * @param value
     *        - value to be set.
     * @return boolean indicating whether the updating was successful.
     */
    public boolean putFloat(IAndroidSettings setting, float value) {
        return putSetting(setting, "f", Float.toString(value));
    }

    /**
     * Updates a single settings value as integer.
     *
     * @param setting
     *        - android setting.
     * @param value
     *        - value to be set.
     * @return boolean indicating whether the updating was successful.
     */
    public boolean putInt(IAndroidSettings setting, int value) {
        return putSetting(setting, "i", Integer.toString(value));
    }

    /**
     * Updates a single settings value as long.
     *
     * @param setting
     *        - android setting.
     * @param value
     *        - value to be set.
     * @return boolean indicating whether the updating was successful.
     */
    public boolean putLong(IAndroidSettings setting, long value) {
        return putSetting(setting, "l", Long.toString(value));
    }

    /**
     * Updates a single settings value as String.
     *
     * @param setting
     *        - android setting.
     * @param value
     *        - value to be set.
     * @return boolean indicating whether the updating was successful.
     */
    public boolean putString(IAndroidSettings setting, String value) {
        return putSetting(setting, "s", value);
    }

    private String getSetting(IAndroidSettings setting) {
        StringBuilder contentShellCommand = new StringBuilder();
        contentShellCommand.append("content query --uri " + setting.getContentUri());
        contentShellCommand.append(" --projection value");
        contentShellCommand.append(" --where \"name=\'" + setting + "\'\"");

        String shellCommandResult = "";

        try {
            shellCommandResult = shellCommandExecutor.execute(contentShellCommand.toString());
        } catch (CommandFailedException e) {
            return null;
        }

        Pattern returnValuePattern = Pattern.compile("value=(.*)$");
        Matcher returnValueMatcher = returnValuePattern.matcher(shellCommandResult);

        if (returnValueMatcher.find()) {
            return returnValueMatcher.group(1);
        } else {
            return null;
        }
    }

    private boolean putSetting(IAndroidSettings setting, String valueType, String value) {
        StringBuilder contentShellCommand = new StringBuilder();
        contentShellCommand.append("content insert --uri " + setting.getContentUri());
        contentShellCommand.append(" --bind name:s:" + setting);
        contentShellCommand.append(" --bind value:" + valueType + ":" + value);

        return executeShellCommand(contentShellCommand.toString());
    }

    private boolean executeShellCommand(String shellCommand) {
        boolean isCommandExecutionSuccessful = true;

        try {
            shellCommandExecutor.execute(shellCommand);
        } catch (CommandFailedException e) {
            isCommandExecutionSuccessful = false;
        }

        return isCommandExecutionSuccessful;
    }
}
