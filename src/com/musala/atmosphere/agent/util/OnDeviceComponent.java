package com.musala.atmosphere.agent.util;

/**
 * Contains information for all on-device components.
 * 
 * @author yordan.petrov
 * 
 */
public enum OnDeviceComponent {
    SERVICE("Atmosphere Service", "AtmosphereService-release.apk", "com.musala.atmosphere.service", null),
    UI_AUTOMATOR_BRIDGE("Atmosphere UiAutomator Bridge", "AtmosphereUIAutomatorBridge.jar", null, null),
    UI_AUTOMATOR_BRIDGE_LIBS("Atmosphere UiAutomator Bridge Libraries", "AtmosphereUIAutomatorBridgeLibs.jar", null, null),
    IME("Atmosphere Input Method Engine", "AtmosphereIME-release.apk", "com.musala.atmosphere.ime", "com.musala.atmosphere.ime/.AtmosphereIME");

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
