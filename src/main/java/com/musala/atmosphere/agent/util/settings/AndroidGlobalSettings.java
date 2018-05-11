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
 * Global system settings, containing preferences that always apply identically to all defined users. Applications can
 * read these but are not allowed to write; like the "Secure" settings, these are for preferences that the user must
 * explicitly modify through the system UI or specialized APIs for those values. We can get or change these settings
 * using the 'content' command utility provided by Android's shell.
 *
 * @see <a href="http://developer.android.com/reference/android/provider/Settings.Global.html">developer.android.com</a>
 *
 * @author nikola.taushanov
 *
 */
public enum AndroidGlobalSettings implements IAndroidSettings {
    /**
     * Whether Airplane Mode is on.
     */
    AIRPLANE_MODE_ON("airplane_mode_on"),

    /**
     * Constant for use in AIRPLANE_MODE_RADIOS to specify Bluetooth radio.
     */
    RADIO_BLUETOOTH("bluetooth"),

    /**
     * Constant for use in AIRPLANE_MODE_RADIOS to specify Wi-Fi radio.
     */
    RADIO_WIFI("wifi"),

    /**
     * Constant for use in AIRPLANE_MODE_RADIOS to specify Cellular radio.
     */
    RADIO_CELL("cell"),

    /**
     * Constant for use in AIRPLANE_MODE_RADIOS to specify NFC radio.
     */
    RADIO_NFC("nfc"),

    /**
     * A comma separated list of radios that need to be disabled when airplane mode is on. This overrides WIFI_ON and
     * BLUETOOTH_ON, if Wi-Fi and bluetooth are included in the comma separated list.
     */
    AIRPLANE_MODE_RADIOS("airplane_mode_radios"),

    /**
     * The policy for deciding when Wi-Fi should go to sleep (which will in turn switch to using the mobile data as an
     * Internet connection).
     * <p>
     * Set to one of {@link #WIFI_SLEEP_POLICY_DEFAULT}, {@link #WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED}, or
     * {@link #WIFI_SLEEP_POLICY_NEVER}.
     */
    WIFI_SLEEP_POLICY("wifi_sleep_policy"),

    /**
     * Value to specify if the user prefers the date, time and time zone to be automatically fetched from the network
     * (NITZ). 1yes, 0no
     */
    AUTO_TIME("auto_time"),

    /**
     * Value to specify if the user prefers the time zone to be automatically fetched from the network (NITZ). 1yes, 0no
     */
    AUTO_TIME_ZONE("auto_time_zone"),

    /**
     * Whether we keep the device on while the device is plugged in. Supported values are:
     * <ul>
     * <li>{@code 0} to never stay on while plugged in</li>
     * <li>{@code 1} to stay on for AC charger</li>
     * <li>{@code 2} to stay on for USB charger</li>
     * <li>{@code 4} to stay on for wireless charger</li>
     * </ul>
     * These values can be OR-ed together.
     */
    STAY_ON_WHILE_PLUGGED_IN("stay_on_while_plugged_in"),

    /**
     * Whether ADB is enabled.
     */
    ADB_ENABLED("adb_enabled"),

    /**
     * Whether bluetooth is enabled/disabled 0disabled. 1enabled.
     */
    BLUETOOTH_ON("bluetooth_on"),

    /**
     * Whether or not data roaming is enabled. (0 false, 1 true)
     */
    DATA_ROAMING("data_roaming"),

    /**
     * Whether user has enabled development settings.
     */
    DEVELOPMENT_SETTINGS_ENABLED("development_settings_enabled"),

    /**
     * Whether the device has been provisioned (0 false, 1 true)
     */
    DEVICE_PROVISIONED("device_provisioned"),

    /**
     * Whether the package installer should allow installation of apps downloaded from sources other than Google Play.
     *
     * 1 allow installing from other sources 0 only allow installing from Google Play
     */
    INSTALL_NON_MARKET_APPS("install_non_market_apps"),

    /**
     * User preference for which network(s) should be used. Only the connectivity service should touch this.
     */
    NETWORK_PREFERENCE("network_preference"),

    /**
     * USB Mass Storage Enabled
     */
    USB_MASS_STORAGE_ENABLED("usb_mass_storage_enabled"),

    /**
     * If this setting is set (to anything), then all references to Gmail on the device must change to Google Mail.
     */
    USE_GOOGLE_MAIL("use_google_mail"),

    /**
     * Whether to notify the user of open networks.
     * <p>
     * If not connected and the scan results have an open network, we will put this notification up. If we attempt to
     * connect to a network or the open network(s) disappear, we remove the notification. When we show the notification,
     * we will not show it again for {@link android.provider.Settings.Secure#WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY} time.
     */
    WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON("wifi_networks_available_notification_on"),

    /**
     * Delay (in seconds) before repeating the Wi-Fi networks available notification. Connecting to a network will reset
     * the timer.
     */
    WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY("wifi_networks_available_repeat_delay"),

    /**
     * When the number of open networks exceeds this number, the least-recently-used excess networks will be removed.
     */
    WIFI_NUM_OPEN_NETWORKS_KEPT("wifi_num_open_networks_kept"),

    /**
     * Whether the Wi-Fi should be on. Only the Wi-Fi service should touch this.
     */
    WIFI_ON("wifi_on"),

    /**
     * Whether the Wi-Fi watchdog is enabled.
     */
    WIFI_WATCHDOG_ON("wifi_watchdog_on"),

    /**
     * The maximum number of times we will retry a connection to an access point for which we have failed in acquiring
     * an IP address from DHCP. A value of N means that we will make N+1 connection attempts in all.
     */
    WIFI_MAX_DHCP_RETRY_COUNT("wifi_max_dhcp_retry_count"),

    /**
     * Maximum amount of time in milliseconds to hold a wakelock while waiting for mobile data connectivity to be
     * established after a disconnect from Wi-Fi.
     */
    WIFI_MOBILE_DATA_TRANSITION_WAKELOCK_TIMEOUT_MS("wifi_mobile_data_transition_wakelock_timeout_ms"),

    /**
     * Ringer mode. This is used internally, changing this value will not change the ringer mode. See AudioManager.
     */
    MODE_RINGER("mode_ringer"),

    /**
     * Host name and port for global http proxy. Uses ':' seperator for between host and port.
     */
    HTTP_PROXY("http_proxy"),

    /**
     * Scaling factor for normal window animations. Setting to 0 will disable window animations.
     */
    WINDOW_ANIMATION_SCALE("window_animation_scale"),

    /**
     * Scaling factor for activity transition animations. Setting to 0 will disable window animations.
     */
    TRANSITION_ANIMATION_SCALE("transition_animation_scale"),

    /**
     * Scaling factor for Animator-based animations. This affects both the start delay and duration of all such
     * animations. Setting to 0 will cause animations to end immediately. The default value is 1.
     */
    ANIMATOR_DURATION_SCALE("animator_duration_scale"),

    /**
     * Name of an application package to be debugged.
     */
    DEBUG_APP("debug_app"),

    /**
     * If 1, when launching DEBUG_APP it will wait for the debugger before starting user code. If 0, it will run
     * normally.
     */
    WAIT_FOR_DEBUGGER("wait_for_debugger"),

    /**
     * Control whether the process CPU usage meter should be shown.
     */
    SHOW_PROCESSES("show_processes"),

    /**
     * If 1, the activity manager will aggressively finish activities and processes as soon as they are no longer
     * needed. If 0, the normal extended lifetime is used.
     */
    ALWAYS_FINISH_ACTIVITIES("always_finish_activities");

    /**
     * Value for {@link #WIFI_SLEEP_POLICY} to use the default Wi-Fi sleep policy, which is to sleep shortly after the
     * turning off according to the {@link #STAY_ON_WHILE_PLUGGED_IN} setting.
     */
    public static final int WIFI_SLEEP_POLICY_DEFAULT = 0;

    /**
     * Value for {@link #WIFI_SLEEP_POLICY} to use the default policy when the device is on battery, and never go to
     * sleep when the device is plugged in.
     */
    public static final int WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED = 1;

    /**
     * Value for {@link #WIFI_SLEEP_POLICY} to never go to sleep.
     */
    public static final int WIFI_SLEEP_POLICY_NEVER = 2;

    private static final String CONTENT_URI = "content://settings/global";

    private String settingName;

    AndroidGlobalSettings(String settingName) {
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
