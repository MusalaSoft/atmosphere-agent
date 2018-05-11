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

/**
 * System settings, containing miscellaneous system preferences. We can get or change these settings using the 'content'
 * command utility provided by Android's shell.
 *
 * @see <a href="http://developer.android.com/reference/android/provider/Settings.System.html">developer.android.com</a>
 *
 * @author nikola.taushanov
 *
 */
public enum AndroidSystemSettings implements IAndroidSettings {
    /**
     * WARNING: Moved to {@link AndroidGlobalSettings} since Android 4.2.2. Whether Airplane Mode is on.
     */
    AIRPLANE_MODE_ON("airplane_mode_on"),

    /**
     * Determines whether remote devices may discover and/or connect to this device.
     * <P>
     * Type: INT
     * </P>
     * 2 -- discoverable and connectable 1 -- connectable but not discoverable 0 -- neither connectable nor discoverable
     */
    BLUETOOTH_DISCOVERABILITY("bluetooth_discoverability"),

    /**
     * Bluetooth discoverability timeout. If this value is nonzero, then Bluetooth becomes discoverable for a certain
     * number of seconds, after which is becomes simply connectable. The value is in seconds.
     */
    BLUETOOTH_DISCOVERABILITY_TIMEOUT("bluetooth_discoverability_timeout"),

    /**
     * A formatted string of the next alarm that is set, or the empty string if there is no alarm set.
     */
    NEXT_ALARM_FORMATTED("next_alarm_formatted"),

    /**
     * Scaling factor for fonts, float.
     */
    FONT_SCALE("font_scale"),

    /**
     * The timeout before the screen turns off.
     */
    SCREEN_OFF_TIMEOUT("screen_off_timeout"),

    /**
     * The screen backlight brightness between 0 and 255.
     */
    SCREEN_BRIGHTNESS("screen_brightness"),

    /**
     * Control whether to enable automatic brightness mode.
     */
    SCREEN_BRIGHTNESS_MODE("screen_brightness_mode"),

    /**
     * Determines which streams are affected by ringer mode changes. The stream type's bit should be set to 1 if it
     * should be muted when going into an inaudible ringer mode.
     */
    MODE_RINGER_STREAMS_AFFECTED("mode_ringer_streams_affected"),

    /**
     * Determines which streams are affected by mute. The stream type's bit should be set to 1 if it should be muted
     * when a mute request is received.
     */
    MUTE_STREAMS_AFFECTED("mute_streams_affected"),

    /**
     * Whether vibrate is on for different events. This is used internally, changing this value will not change the
     * vibrate. See AudioManager.
     */
    VIBRATE_ON("vibrate_on"),

    /**
     * Ringer volume. This is used internally, changing this value will not change the volume. See AudioManager.
     */
    VOLUME_RING("volume_ring"),

    /**
     * System/notifications volume. This is used internally, changing this value will not change the volume. See
     * AudioManager.
     */
    VOLUME_SYSTEM("volume_system"),

    /**
     * Voice call volume. This is used internally, changing this value will not change the volume. See AudioManager.
     */
    VOLUME_VOICE("volume_voice"),

    /**
     * Music/media/gaming volume. This is used internally, changing this value will not change the volume. See
     * AudioManager.
     */
    VOLUME_MUSIC("volume_music"),

    /**
     * Alarm volume. This is used internally, changing this value will not change the volume. See AudioManager.
     */
    VOLUME_ALARM("volume_alarm"),

    /**
     * Notification volume. This is used internally, changing this value will not change the volume. See AudioManager.
     */
    VOLUME_NOTIFICATION("volume_notification"),

    /**
     * Bluetooth Headset volume. This is used internally, changing this value will not change the volume. See
     * AudioManager.
     */
    VOLUME_BLUETOOTH_SCO("volume_bluetooth_sco"),

    /**
     * Appended to various volume related settings to record the previous values before they the settings were affected
     * by a silent/vibrate ringer mode change.
     */
    APPEND_FOR_LAST_AUDIBLE("_last_audible"),

    /**
     * Persistent store for the system-wide default ringtone URI.
     * <p>
     * If you need to play the default ringtone at any given time, it is recommended you give
     * DEFAULT_RINGTONE_URI(checkout the android public api doc for more information) to the media player. It will
     * resolve to the set default ringtone at the time of playing.
     */
    RINGTONE("ringtone"),

    /**
     * Persistent store for the system-wide default notification sound.
     *
     * @see #RINGTONE
     */
    NOTIFICATION_SOUND("notification_sound"),

    /**
     * Persistent store for the system-wide default alarm alert.
     *
     * @see #RINGTONE
     */
    ALARM_ALERT("alarm_alert"),

    /**
     * Setting to enable Auto Replace (AutoText) in text editors. 1(On, 0(Off
     */
    TEXT_AUTO_REPLACE("auto_replace"),

    /**
     * Setting to enable Auto Caps in text editors. 1(On, 0(Off
     */
    TEXT_AUTO_CAPS("auto_caps"),

    /**
     * Setting to enable Auto Punctuate in text editors. 1(On, 0(Off. This feature converts two spaces to a "." and
     * space.
     */
    TEXT_AUTO_PUNCTUATE("auto_punctuate"),

    /**
     * Setting to showing password characters in text editors. 1(On, 0(Off
     */
    TEXT_SHOW_PASSWORD("show_password"),

    /**
     * Setting to showing the current Gtalk service status.
     */
    SHOW_GTALK_SERVICE_STATUS("SHOW_GTALK_SERVICE_STATUS"),

    /**
     * Display times as 12 or 24 hours 12 24
     */
    TIME_12_24("time_12_24"),

    /**
     * Date format string mm/dd/yyyy dd/mm/yyyy yyyy/mm/dd
     */
    DATE_FORMAT("date_format"),

    /**
     * Whether the setup wizard has been run before (on first boot), or if it still needs to be run.
     *
     * nonzero(it has been run in the past 0(it has not been run in the past
     */
    SETUP_WIZARD_HAS_RUN("setup_wizard_has_run"),

    /**
     * Control whether the accelerometer will be used to change screen orientation. If 0, it will not be used unless
     * explicitly requested by the application; if 1, it will be used by default unless explicitly disabled by the
     * application.
     */
    ACCELEROMETER_ROTATION("accelerometer_rotation"),

    /**
     * Default screen rotation when no other policy applies. When {@link #ACCELEROMETER_ROTATION} is zero and no
     * on-screen Activity expresses a preference, this rotation value will be used. Must be one of the Surface rotation
     * constants}.
     */
    USER_ROTATION("user_rotation"),

    /**
     * Whether the audible DTMF tones are played by the dialer when dialing. The value is boolean (1 or 0).
     */
    DTMF_TONE_WHEN_DIALING("dtmf_tone"),

    /**
     * Whether the sounds effects (key clicks, lid open ...) are enabled. The value is boolean (1 or 0).
     */
    SOUND_EFFECTS_ENABLED("sound_effects_enabled"),

    /**
     * Whether the haptic feedback (long presses, ...) are enabled. The value is boolean (1 or 0).
     */
    HAPTIC_FEEDBACK_ENABLED("haptic_feedback_enabled"),

    /**
     * <p>
     * What happens when the user presses the end call button if they're not on a call.
     * </p>
     * <b>Values:</b>
     * <p>
     * 0 - The end button does nothing 1 - The end button goes to the home screen. 2 - The end button puts the device to
     * sleep and locks the keyguard. 3 - The end button goes to the home screen. If the user is already on the home
     * screen, it puts the device to sleep.
     * </p>
     */
    END_BUTTON_BEHAVIOR("end_button_behavior");

    private static final String CONTENT_URI = "content://settings/system";

    private String settingName;

    AndroidSystemSettings(String settingName) {
        this.settingName = settingName;
    }

    /**
     * @return the name of the enum constant
     */
    @Override
    public String toString() {
        return settingName;
    }

    /**
     * @return the uniform resource identifier
     */
    @Override
    public String getContentUri() {
        return CONTENT_URI;
    }
}
