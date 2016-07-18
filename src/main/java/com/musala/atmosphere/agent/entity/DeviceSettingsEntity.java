package com.musala.atmosphere.agent.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.agent.util.settings.AndroidGlobalSettings;
import com.musala.atmosphere.agent.util.settings.AndroidSystemSettings;
import com.musala.atmosphere.agent.util.settings.DeviceSettingsManager;
import com.musala.atmosphere.agent.util.settings.SettingsParsingException;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.ScreenOrientation;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.util.IntentBuilder;
import com.musala.atmosphere.commons.util.IntentBuilder.IntentAction;

/**
 * Entity responsible for retrieving and updating device settings.
 *
 * @author filareta.yordanova
 *
 */
public class DeviceSettingsEntity {
    private static final Logger LOGGER = Logger.getLogger(DeviceSettingsEntity.class);

    private ShellCommandExecutor shellCommandExecutor;

    private DeviceInformation deviceInformation;

    private DeviceSettingsManager deviceSettingsManager;

    /**
     * Constructs a new {@link DeviceSettingsEntity} object by given {@link ShellCommandExecutor shell command executor} and
     * {@link DeviceInformation}.
     *
     * @param shellCommandExecutor
     *        - a shell command executor to execute shell commands with
     * @param deviceInformation
     *        - information container for the basic device properties
     */
    DeviceSettingsEntity(ShellCommandExecutor shellCommandExecutor, DeviceInformation deviceInformation) {
        this.shellCommandExecutor = shellCommandExecutor;
        this.deviceInformation = deviceInformation;
        this.deviceSettingsManager = new DeviceSettingsManager(shellCommandExecutor);
    }

    /**
     * Gets a {@link ScreenOrientation} instance that contains information about the orientation of the screen.
     *
     * @return {@link ScreenOrientation object} that shows how android elements are rotated on the screen
     * @see ScreenOrientation
     */
    public ScreenOrientation getScreenOrientation() {
        ScreenOrientation screenOrientation = null;
        try {
            int obtainedScreenOrientationValue = deviceSettingsManager.getInt(AndroidSystemSettings.USER_ROTATION);
            screenOrientation = ScreenOrientation.getValueOfInt(obtainedScreenOrientationValue);
        } catch (SettingsParsingException e) {
            LOGGER.error("Failed to get screen orientation of the device.", e);
        }

        return screenOrientation;
    }

    /**
     * Returns device auto rotation state.
     *
     * @return <code>true</code> if the auto rotation is on , <code>false</code> if it's not, <code>null</code> if the
     *         method failed to get device auto rotation state
     */
    public Boolean isAutoRotationOn() {
        Boolean isAutoRotationOn = null;
        try {
            int autoRotationValue = deviceSettingsManager.getInt(AndroidSystemSettings.ACCELEROMETER_ROTATION);
            isAutoRotationOn = autoRotationValue == 1 ? true : false;

        } catch (SettingsParsingException e) {
            LOGGER.error("Getting autorotation status failed.", e);
        }

        return isAutoRotationOn;
    }

    /**
     * Sets the airplane mode state for this device.<br>
     * <i><b>Warning:</b> enabling airplane mode on an emulator disconnects it from the ATMOSPHERE Agent and this emulator
     * could only be connected back after Agent restart. Setting airplane mode for emulators is prohibited.</i>
     *
     * @param airplaneMode
     *        - <code>true</code> to enable airplane mode, <code>false</code> to disable airplane mode
     * @return <code>true</code> if the airplane mode setting is successful, <code>false</code> if it fails
     */
    public boolean setAirplaneMode(boolean airplaneMode) {
        boolean isEmulator = deviceInformation.isEmulator();
        if (isEmulator) {
            String message = "Enabling airplane mode on an emulator disconnects it from the ATMOSPHERE Agent and this emulator could only be connected back after Agent restart. Setting airplane mode for emulators is prohibited.";
            LOGGER.warn(message);
            return false;
        }

        int airplaneModeIntValue = airplaneMode ? 1 : 0;

        IntentBuilder intentBuilder = new IntentBuilder(IntentAction.AIRPLANE_MODE_NOTIFICATION);
        intentBuilder.putExtraBoolean("state", airplaneMode);
        String intentCommand = intentBuilder.buildIntentCommand();

        final String INTENT_COMMAND_RESPONSE = "Broadcast completed: result=0";

        boolean success = deviceSettingsManager.putInt(AndroidGlobalSettings.AIRPLANE_MODE_ON, airplaneModeIntValue);
        if (!success) {
            String message = "Updating airplane mode status failed.";
            LOGGER.error(message);
            return false;
        }

        String intentCommandResponse;
        try {
            intentCommandResponse = shellCommandExecutor.execute(intentCommand);
        } catch (CommandFailedException e) {
            String message = "Executing shell command failed.";
            LOGGER.error(message);
            return false;
        }
        Pattern intentCommandResponsePattern = Pattern.compile(INTENT_COMMAND_RESPONSE);
        Matcher intentCommandResponseMatcher = intentCommandResponsePattern.matcher(intentCommandResponse);
        if (!intentCommandResponseMatcher.find()) {
            String message = "Broadcasting notification intent failed.";
            LOGGER.error(message);
            return false;
        }

        return true;
    }

    /**
     * Gets the airplane mode state of this device.<br>
     *
     * @return <code>true</code> if the airplane mode is on, <code>false</code> if it's off and <code>null</code> if
     *         getting airplane mode fails
     */
    public Boolean getAirplaneMode() {
        try {
            int airplaneMode = deviceSettingsManager.getInt(AndroidGlobalSettings.AIRPLANE_MODE_ON);
            return airplaneMode == 1;
        } catch (SettingsParsingException e) {
            LOGGER.error("Getting the Airplane mode of the device failed.", e);
            return null;
        }
    }

    /**
     * Sets new screen orientation for this device.<br>
     * Implicitly turns off screen auto rotation.
     *
     * @param screenOrientation
     *        - new {@link ScreenOrientation ScreenOrientation} to be set
     * @return <code>true</code> if the screen orientation setting is successful, <code>false</code> if it fails
     */
    public boolean setScreenOrientation(ScreenOrientation screenOrientation) {
        if (!disableScreenAutoRotation()) {
            String message = "Screen orientation was not set due to setting auto rotation failure.";
            LOGGER.error(message);
            return false;
        }

        boolean success = deviceSettingsManager.putInt(AndroidSystemSettings.USER_ROTATION,
                                                       screenOrientation.getOrientationNumber());

        return success;
    }

    /**
     * Enables the screen auto rotation on this device.
     *
     * @return <code>true</code> if the auto rotation setting is successful, and <code>false</code> if it fails
     */
    public boolean enableScreenAutoRotation() {
        return deviceSettingsManager.putInt(AndroidSystemSettings.ACCELEROMETER_ROTATION, 1);
    }

    /**
     * Disables the screen auto rotation on this device.
     *
     * @return <code>true</code> if the auto rotation setting is successful, and <code>false</code> if it fails
     */
    public boolean disableScreenAutoRotation() {
        return deviceSettingsManager.putInt(AndroidSystemSettings.ACCELEROMETER_ROTATION, 0);
    }

    /**
     * Sets the timeout in the system settings, after which the screen is turned off.
     * <p>
     * Note: On emulators the screen is only dimmed.
     * </p>
     *
     * @param screenOffTimeout
     *        - timeout in milliseconds, after which the screen is turned off
     * @return true if the given screen off timeout is successfully set
     *
     */
    public boolean setScreenOffTimeout(long screenOffTimeout) {
        return deviceSettingsManager.putLong(AndroidSystemSettings.SCREEN_OFF_TIMEOUT, screenOffTimeout);
    }

    /**
     * Gets the timeout from the system settings, after which the screen is turned off.
     *
     * @return timeout in milliseconds, after which the screen is turned off
     */
    public long getScreenOffTimeout() {
        return deviceSettingsManager.getLong(AndroidSystemSettings.SCREEN_OFF_TIMEOUT, 0);
    }

    /**
     * Gets the {@link DeviceSettingsManager settings manager} of the current device, that allows getting and inserting
     * device settings.
     *
     * @return {@link DeviceSettingsManager} instance for this device
     */
    public DeviceSettingsManager getDeviceSettingsManager() {
        return deviceSettingsManager;
    }
}