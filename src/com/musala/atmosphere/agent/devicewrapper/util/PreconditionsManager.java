package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.exception.ComponentInstallationFailedException;
import com.musala.atmosphere.agent.exception.ComponentNotInstalledException;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.agent.util.AutomaticDeviceSetupFlag;
import com.musala.atmosphere.agent.util.OnDeviceComponent;
import com.musala.atmosphere.agent.util.OnDeviceComponentCommand;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Takes care of automatic on-device component setup and verification.
 * 
 * @author yordan.petrov
 * 
 */
public class PreconditionsManager {
    /**
     * The timeout for command execution from the config file.
     */
    private static final int COMMAND_EXECUTION_TIMEOUT = AgentPropertiesLoader.getCommandExecutionTimeout();

    /**
     * The path to the on-device components' files from the config file.
     */
    private static final String ON_DEVICE_COMPONENT_FILES_PATH = AgentPropertiesLoader.getOnDeviceComponentFilesPath();

    /**
     * Path to the temp folder of the device. This is where the UI Automator Bridge should be deployed.
     */
    private static final String TEMP_PATH = "/data/local/tmp/";

    /**
     * Specifies how the agent should react when new device is being connected.
     */
    private static final AutomaticDeviceSetupFlag AUTOMATIC_SETUP_FLAG = AgentPropertiesLoader.getAutomaticDeviceSetupFlag();

    /**
     * Message that is used when a component installation fails.
     */
    private static final String COMPONENT_INSTALLATION_FAILED_MESSAGE = "%s component installation failed.";

    /**
     * Message that is used when a component installation begins.
     */
    private static final String COMPONENT_INSTALLATION_MESSAGE = "Installing %s component on the device...";

    /**
     * The time in milliseconds that should pass after installation of components is completed;
     */
    private static final int POST_INSTALLATION_TIMEOUT = 3000;

    private static final Logger LOGGER = Logger.getLogger(PreconditionsManager.class.getCanonicalName());

    private IDevice wrappedDevice;

    public PreconditionsManager(IDevice wrappedDevice) {
        this.wrappedDevice = wrappedDevice;
    }

    /**
     * Checks whether a give APK is installed on the device.
     * 
     * @param packageName
     *        - the package name of the APK.
     * @return true if it's installed; false if not.
     */
    private boolean isApkInstalled(String packageName) {
        String command = String.format(OnDeviceComponentCommand.IS_APK_INSTALLED.getCommand(), packageName);
        String expectedResponse = String.format(OnDeviceComponentCommand.IS_APK_INSTALLED.getExpectedResponse(),
                                                packageName);
        String actualResponse = null;

        try {
            actualResponse = executeShellCommand(command);
        } catch (RemoteException | CommandFailedException e) {
            return false;
        }

        return actualResponse.equals(expectedResponse);
    }

    /**
     * Checks whether the Atmosphere Service is installed on the device.
     * 
     * @return true if it's installed; false if not.
     */
    private boolean isServiceInstalled() {
        return isApkInstalled(OnDeviceComponent.SERVICE.getPackageName());
    }

    /**
     * Checks whether the Atmosphere IME is installed on the device.
     * 
     * @return true if it's installed; false if not.
     */
    private boolean isImeInstalled() {
        return isApkInstalled(OnDeviceComponent.IME.getPackageName());
    }

    /**
     * Checks whether a file or folder is existing on the device by a given path.
     * 
     * @param path
     *        - path to check in for the file or folder.
     * @return true if the file or folder is existing; false if not.
     */
    private boolean isFileOrFolderExistingOnDevice(String path) {
        String command = String.format(OnDeviceComponentCommand.IS_FILE_OR_FOLDER_EXISTING.getCommand(), path, path);
        String expectedResponse = String.format(OnDeviceComponentCommand.IS_FILE_OR_FOLDER_EXISTING.getExpectedResponse(),
                                                path);
        String actualResponse = null;

        try {
            actualResponse = executeShellCommand(command);
        } catch (RemoteException | CommandFailedException e) {
            return false;
        }

        return actualResponse.equals(expectedResponse);
    }

    /**
     * Checks whether the Atmosphere UI Automator Bridge is installed on the device.
     * 
     * @return true if it's installed; false if not.
     */
    private boolean isUiAutomatorBridgeInstalled() {
        String uiAutomatorBridgePath = TEMP_PATH.concat(OnDeviceComponent.UI_AUTOMATOR_BRIDGE.getFileName());
        return isFileOrFolderExistingOnDevice(uiAutomatorBridgePath);
    }

    /**
     * Checks whether the Atmosphere UI Automator Bridge Libs is installed on the device.
     * 
     * @return true if it's installed; false if not.
     */
    private boolean isUiAutomatorBridgeLibsInstalled() {
        String uiAutomatorBridgeLibsPath = TEMP_PATH.concat(OnDeviceComponent.UI_AUTOMATOR_BRIDGE_LIBS.getFileName());
        return isFileOrFolderExistingOnDevice(uiAutomatorBridgeLibsPath);
    }

    /**
     * Sets the Atmosphere IME as the default input method on the device.
     * 
     * @return true if setting it was successful; false if not.
     */
    private boolean setIme() {
        String statusMessage = String.format("Setting %s as default input method...",
                                             OnDeviceComponent.IME.getHumanReadableName());
        LOGGER.info(statusMessage);

        String imeId = OnDeviceComponent.IME.getImeId();
        String command = String.format(OnDeviceComponentCommand.SET_IME.getCommand(), imeId);
        String expectedResponse = String.format(OnDeviceComponentCommand.SET_IME.getExpectedResponse(), imeId);
        String actualResponse = null;

        try {
            actualResponse = executeShellCommand(command);
        } catch (RemoteException | CommandFailedException e) {
            String errorMessage = String.format("%s could not be set as the default input method.",
                                                OnDeviceComponent.IME.getHumanReadableName());
            LOGGER.warn(errorMessage, e);
            return false;
        }

        return actualResponse.equals(expectedResponse);
    }

    /**
     * Gets the installed status of all on-device components on the device.
     * 
     * @return map representing component-installed status relation.
     */
    private Map<OnDeviceComponent, Boolean> getCurrentComponentInstalledStatus() {
        Map<OnDeviceComponent, Boolean> componentInstalledStatus = new HashMap<OnDeviceComponent, Boolean>();
        componentInstalledStatus.put(OnDeviceComponent.SERVICE, isServiceInstalled());
        componentInstalledStatus.put(OnDeviceComponent.IME, isImeInstalled());
        componentInstalledStatus.put(OnDeviceComponent.UI_AUTOMATOR_BRIDGE, isUiAutomatorBridgeInstalled());
        componentInstalledStatus.put(OnDeviceComponent.UI_AUTOMATOR_BRIDGE_LIBS, isUiAutomatorBridgeLibsInstalled());
        return componentInstalledStatus;
    }

    /**
     * Installs a component APK file on the device.
     * 
     * @param onDeviceComponent
     *        - the component that should be installed.
     */
    private void installComponentApk(OnDeviceComponent onDeviceComponent) {
        String statusMessage = String.format(COMPONENT_INSTALLATION_MESSAGE, onDeviceComponent.getHumanReadableName());
        LOGGER.info(statusMessage);

        String componentPath = ON_DEVICE_COMPONENT_FILES_PATH.concat(onDeviceComponent.getFileName());

        try {
            wrappedDevice.installPackage(componentPath, true, new String());
        } catch (InstallException e) {
            String errorMessage = String.format(COMPONENT_INSTALLATION_FAILED_MESSAGE,
                                                onDeviceComponent.getHumanReadableName());
            LOGGER.fatal(errorMessage, e);
            throw new ComponentInstallationFailedException(errorMessage, e);
        }
    }

    /**
     * Pushes a component file to the temp folder of the device.
     * 
     * @param onDeviceComponent
     *        - the component that should be pushed.
     */
    private void pushComponentFileToTemp(OnDeviceComponent onDeviceComponent) {
        String statusMessage = String.format(COMPONENT_INSTALLATION_MESSAGE, onDeviceComponent.getHumanReadableName());
        LOGGER.info(statusMessage);

        String componentPath = ON_DEVICE_COMPONENT_FILES_PATH.concat(onDeviceComponent.getFileName());
        String remotePath = TEMP_PATH.concat("/").concat(onDeviceComponent.getFileName());

        try {
            wrappedDevice.pushFile(componentPath, remotePath);
        } catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
            String errorMessage = String.format(COMPONENT_INSTALLATION_FAILED_MESSAGE,
                                                onDeviceComponent.getHumanReadableName());
            LOGGER.fatal(errorMessage, e);
            throw new ComponentInstallationFailedException(errorMessage, e);
        }
    }

    /**
     * Installs the Atmosphere Service on the device.
     */
    private void installService() {
        installComponentApk(OnDeviceComponent.SERVICE);
    }

    /**
     * Installs the Atmosphere IME on the device.
     */
    private void installIme() {
        installComponentApk(OnDeviceComponent.IME);
    }

    /**
     * Installs the Atmosphere UI Automator Bridge on the device.
     */
    private void installUiAutomatorBridge() {
        pushComponentFileToTemp(OnDeviceComponent.UI_AUTOMATOR_BRIDGE);
    }

    /**
     * Installs the Atmosphere UI Automator Bridge Libs on the device.
     */
    private void installUiAutomatorBridgeLibs() {
        pushComponentFileToTemp(OnDeviceComponent.UI_AUTOMATOR_BRIDGE_LIBS);
    }

    /**
     * Executes a command on the device's shell and returns the result of the execution.
     * 
     * @param command
     *        - Shell command to be executed
     * @return Shell response from the command execution.
     * @throws RemoteException
     * @throws CommandFailedException
     */
    private String executeShellCommand(String command) throws RemoteException, CommandFailedException {
        String response = "";

        try {
            CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
            wrappedDevice.executeShellCommand(command, outputReceiver, COMMAND_EXECUTION_TIMEOUT);

            response = outputReceiver.getOutput();
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            throw new CommandFailedException("Shell command execution failed.", e);
        }

        return response;
    }

    /**
     * Checks whether all on-device components are installed on the device.
     * 
     * @param currentComponentInstalledStatus
     *        - map object representing component-installed status relation.
     */
    private void checkComponentsAreInstalled(Map<OnDeviceComponent, Boolean> currentComponentInstalledStatus) {
        LOGGER.info("Checking if all on-device components are installed on the device...");

        for (OnDeviceComponent currentComponent : currentComponentInstalledStatus.keySet()) {
            if (!currentComponentInstalledStatus.get(currentComponent)) {
                String message = "%s component not found on the device.";
                String formattedMessage = String.format(message, currentComponent.getHumanReadableName());
                LOGGER.fatal(formattedMessage);
                throw new ComponentNotInstalledException(formattedMessage);
            }
        }
    }

    /**
     * Installs the missing on-device components.
     * 
     * @param currentComponentInstalledStatus
     *        - map object representing component-installed status relation.
     */
    private void installMissingComponents(Map<OnDeviceComponent, Boolean> currentComponentInstalledStatus) {
        boolean areAnyComponentsInstalled = false;

        if (!currentComponentInstalledStatus.get(OnDeviceComponent.SERVICE)) {
            installService();
            areAnyComponentsInstalled = true;
        }
        if (!currentComponentInstalledStatus.get(OnDeviceComponent.IME)) {
            installIme();
            setIme();
            areAnyComponentsInstalled = true;
        }
        if (!currentComponentInstalledStatus.get(OnDeviceComponent.UI_AUTOMATOR_BRIDGE)) {
            installUiAutomatorBridge();
            areAnyComponentsInstalled = true;
        }
        if (!currentComponentInstalledStatus.get(OnDeviceComponent.UI_AUTOMATOR_BRIDGE_LIBS)) {
            installUiAutomatorBridgeLibs();
            areAnyComponentsInstalled = true;
        }

        if (areAnyComponentsInstalled) {
            try {
                Thread.sleep(POST_INSTALLATION_TIMEOUT);
            } catch (InterruptedException e) {
                // Nothing to do here.
            }
        }
    }

    /**
     * Installs all components.
     */

    private void installAllComponents() {

        installService();

        installIme();
        setIme();

        installUiAutomatorBridge();
        installUiAutomatorBridgeLibs();

        try {
            Thread.sleep(POST_INSTALLATION_TIMEOUT);
        } catch (InterruptedException e) {
            // Nothing to do here.
        }
    }

    /**
     * Takes care of automatic on-device component setup and verification.
     */
    public void manageOnDeviceComponents() {
        Map<OnDeviceComponent, Boolean> currentComponentInstalledStatus = getCurrentComponentInstalledStatus();

        if (wrappedDevice.isEmulator()) {
            installMissingComponents(currentComponentInstalledStatus);
            return;
        }

        switch (AUTOMATIC_SETUP_FLAG) {
            case ON:
                installMissingComponents(currentComponentInstalledStatus);
                break;

            case ASK:
                // TODO: Implement ASK flag logic.
                // Checking components will be used as temporary behavior.
                checkComponentsAreInstalled(currentComponentInstalledStatus);
                break;

            case OFF:
                checkComponentsAreInstalled(currentComponentInstalledStatus);
                break;

            case FORCE_INSTALL:
                installAllComponents();
                break;

            default:
                checkComponentsAreInstalled(currentComponentInstalledStatus);
                break;
        }
    }
}
