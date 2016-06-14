package com.musala.atmosphere.agent.util;

/**
 * Contains information for all on-device components.
 *
 * @author yordan.petrov
 *
 */
public enum OnDeviceComponent {
    SERVICE(
            "Atmosphere Service", "atmosphere-service.apk", "com.musala.atmosphere.service", null),
    UI_AUTOMATOR_BRIDGE(
            "Atmosphere UiAutomator Bridge", "atmosphere-uiautomator-bridge.jar", "com.musala.atmosphere.uiautomator.ActionDispatcher", null),
    IME(
            "Atmosphere Input Method Engine", "atmosphere-ime.apk", "com.musala.atmosphere.ime", "com.musala.atmosphere.ime/.AtmosphereIME"),
    START_SCREENRECORD_SCRIPT(
            "Start Screenrecord Script", "start_screenrecord.sh", null, null),
    STOP_SCREENRECORD_SCRIPT(
            "Stop Screenrecord Script", "stop_screenrecord.sh", null, null);

    private OnDeviceComponent(String humanReadableName, String fileName, String packageName, String imeId) {
        this.humanReadableName = humanReadableName;
        this.fileName = fileName;
        this.packageName = packageName;
        this.imeId = imeId;
    }

    private String humanReadableName;

    private String fileName;

    private String packageName;

    private String imeId;

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getImeId() {
        return imeId;
    }
}
