package com.musala.atmosphere.agent.util;

/**
 * Enumeration class containing all possible agent properties.
 * 
 * @author valyo.yolovski
 * 
 */
public enum AgentProperties {
    ADB_CONNECTION_TIMEOUT("adb.connection.timeout"),
    AGENT_RMI_PORT("agent.rmi.port"),
    EMULATOR_CREATION_WAIT_TIMEOUT("emulator.creation.wait.timeout"),
    EMULATOR_ABI("emulator.abi"),
    EMULATOR_EXECUTABLE_PATH("emulator.executable.path"),
    ANDROID_EXECUTABLE_PATH("android.executable.path"),
    COMMAND_EXECUTION_TIMEOUT("command.execution.timeout"),
    ADB_MIN_FORWARD_PORT("adb.min.forward.port"),
    ADB_MAX_FORWARD_PORT("adb.max.forward.port"),
    ON_DEVICE_COMPONENT_CONNECTION_RETRY_LIMIT("ondevicecomponent.connection.retry.limit"),
    DEVICE_AUTOMATIC_SETUP("device.automatic.setup"),
    ON_DEVICE_COMPONENT_FILES_PATH("ondevicecomponent.files.path"),
    SDK_DIR("sdk.dir"),
    CHROME_DRIVER_EXECUTABLE_PATH("chromedriver.executable.path");

    private String value;

    private AgentProperties(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
};
