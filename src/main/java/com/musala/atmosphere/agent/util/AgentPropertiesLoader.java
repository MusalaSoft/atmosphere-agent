package com.musala.atmosphere.agent.util;

import java.io.File;

import com.musala.atmosphere.commons.util.PropertiesLoader;

/**
 * Reads properties from the Agent properties config file.
 *
 * @author valyo.yolovski
 *
 */

public class AgentPropertiesLoader {
    private static final String ADB_ENVIRONMENT_PATH = System.getenv("ANDROID_HOME");

    private static final char SEPARATOR_CHAR = File.separatorChar;

    private static final String ADB_PATH_FORMAT = "%s%cplatform-tools%cadb";

    private static final String ADB_SDK_TOOLS_FORMAT = "%s%ctools";

    private static final String AGENT_PROPERTIES_FILE = "./agent.properties";

    /**
     * Returns the desired property from the config file in String type. If there is no user config file, default values
     * are returned.
     *
     * @param property
     *        - the Agent property to be returned.
     * @return the desired agent property value. Returns String properties only!
     */
    private synchronized static String getPropertyString(AgentProperties property) {
        PropertiesLoader propertiesLoader = PropertiesLoader.getInstance(AGENT_PROPERTIES_FILE);
        String propertyString = property.toString();
        String resultProperty = propertiesLoader.getPropertyString(propertyString);
        return resultProperty;
    }

    /**
     * Returns the directory of the SDK. Loads the property from the Agent's configuration file. If such property is not
     * available uses ANDROID_HOME in the environment variables.
     *
     * @return the directory of the SDK
     */
    private static String getSdkDir() {
        String sdkDir = getPropertyString(AgentProperties.SDK_DIR);

        if (sdkDir == null || sdkDir.trim().isEmpty()) {
            sdkDir = ADB_ENVIRONMENT_PATH;
        }

        return sdkDir;
    }

    /**
     * Returns the path to ADB from the config file.
     *
     * @return - the path to the Android Debug Bridge.
     */
    public static String getAdbPath() {
        return String.format(ADB_PATH_FORMAT, getSdkDir(), SEPARATOR_CHAR, SEPARATOR_CHAR);
    }

    /**
     * Returns the timeout for connecting with the Android Debug Bridge from the config file.
     *
     * @return - int, the timeout in miliseconds.
     */
    public static int getAdbConnectionTimeout() {
        String returnValueString = getPropertyString(AgentProperties.ADB_CONNECTION_TIMEOUT);
        int returnValue = Integer.parseInt(returnValueString);
        return returnValue;
    }

    /**
     * Returns the Agent RMI port from the config file.
     *
     * @return - the port on which the Agent is published in RMI.
     */
    public static int getAgentRmiPort() {
        String returnValueString = getPropertyString(AgentProperties.AGENT_RMI_PORT);
        int returnValue = Integer.parseInt(returnValueString);
        return returnValue;
    }

    /**
     * Returns the timeout for the creation of the emulator form the config file.
     *
     * @return - the creation wait timeout for an emulator
     */
    public static int getEmulatorCreationWaitTimeout() {
        String returnValueString = getPropertyString(AgentProperties.EMULATOR_CREATION_WAIT_TIMEOUT);
        int returnValue = Integer.parseInt(returnValueString);
        return returnValue;
    }

    /**
     * Returns the path to the Android SDK tools directory from the config file.
     *
     * @return - android SDK tools path
     */
    public static String getAndroidSdkToolsDirPath() {
        return String.format(ADB_SDK_TOOLS_FORMAT, getSdkDir(), SEPARATOR_CHAR);
    }

    /**
     * Returns the path of the emulator executable file from the config file.
     *
     * @return - the name of the emulator executable.
     */
    public static String getEmulatorExecutable() {
        String returnValueString = getPropertyString(AgentProperties.EMULATOR_EXECUTABLE_PATH);
        return returnValueString;
    }

    /**
     * Returns the path of the android executable file from the config file.
     *
     * @return - the name of the android executable.
     */
    public static String getAndroidExecutable() {
        String androidExecutable = getPropertyString(AgentProperties.ANDROID_EXECUTABLE_PATH);
        return androidExecutable;
    }

    /**
     * Returns the {@link AbiType} set in the config file.
     *
     * @return the {@link AbiType} set in the config file.
     */
    public static AbiType getEmulatorAbiType() {
        String abiTypeString = getPropertyString(AgentProperties.EMULATOR_ABI);
        AbiType abiType = AbiType.getEnum(abiTypeString);
        return abiType;
    }

    /**
     * Returns the timeout for command execution.
     *
     * @return - int, the timeout for command execution.
     */
    public static int getCommandExecutionTimeout() {
        String returnValueString = getPropertyString(AgentProperties.COMMAND_EXECUTION_TIMEOUT);
        int returnValueInt = Integer.parseInt(returnValueString);
        return returnValueInt;
    }

    /**
     * Returns the minimum port identifier that will be used for connection to an ATMOSPHERE on-device component.
     *
     * @return - the minimum port identifier that will be used for connection to an ATMOSPHERE on-device component.
     */
    public static int getAdbMinForwardPort() {
        String returnValueString = getPropertyString(AgentProperties.ADB_MIN_FORWARD_PORT);
        int returnValueInt = Integer.parseInt(returnValueString);
        return returnValueInt;
    }

    /**
     * Returns the maximum port identifier that will be used for connection to the ATMOSPHERE service.
     *
     * @return - the maximum port identifier that will be used for connection to the ATMOSPHERE service.
     */
    public static int getAdbMaxForwardPort() {
        String returnValueString = getPropertyString(AgentProperties.ADB_MAX_FORWARD_PORT);
        int returnValueInt = Integer.parseInt(returnValueString);
        return returnValueInt;
    }

    /**
     * Returns the maximum number of on-device component connection retries.
     *
     * @return - the maximum number of on-device component connection retries.
     */
    public static int getOnDeviceComponentConnectionRetryLimit() {
        String returnValueString = getPropertyString(AgentProperties.ON_DEVICE_COMPONENT_CONNECTION_RETRY_LIMIT);
        int returnValueInt = Integer.parseInt(returnValueString);
        return returnValueInt;
    }

    /**
     * Returns a {@link AutomaticDeviceSetupFlag} constant specifying how the agent should react when new device is
     * being connected.
     *
     * @return {@link AutomaticDeviceSetupFlag} constant specifying how the agent should react when new device is being
     *         connected.
     */
    public static AutomaticDeviceSetupFlag getAutomaticDeviceSetupFlag() {
        String flagValue = getPropertyString(AgentProperties.DEVICE_AUTOMATIC_SETUP);
        AutomaticDeviceSetupFlag automaticSetupFlag = AutomaticDeviceSetupFlag.getByValue(flagValue);
        return automaticSetupFlag;
    }

    /**
     * Returns the path to the on-device components' files from the config file.
     *
     * @return - the path to the on-device components' files.
     */
    public static String getOnDeviceComponentFilesPath() {
        String onDeviceComponentFilesPath = getPropertyString(AgentProperties.ON_DEVICE_COMPONENT_FILES_PATH);
        return onDeviceComponentFilesPath.concat(File.separator);
    }
}
