package com.musala.atmosphere.agent.devicewrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.DevicePropertyStringConstants;
import com.musala.atmosphere.agent.devicewrapper.util.ApkInstaller;
import com.musala.atmosphere.agent.devicewrapper.util.DeviceProfiler;
import com.musala.atmosphere.agent.devicewrapper.util.FileTransferService;
import com.musala.atmosphere.agent.devicewrapper.util.ImeManager;
import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.agent.exception.ForwardingPortFailedException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentCommunicationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentInitializationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentStartingException;
import com.musala.atmosphere.agent.exception.OnDeviceServiceTerminationException;
import com.musala.atmosphere.agent.util.DeviceScreenResolutionParser;
import com.musala.atmosphere.agent.util.MemoryUnitConverter;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.PowerProperties;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.ScrollDirection;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.beans.DeviceAcceleration;
import com.musala.atmosphere.commons.beans.DeviceMagneticField;
import com.musala.atmosphere.commons.beans.DeviceOrientation;
import com.musala.atmosphere.commons.beans.MobileDataState;
import com.musala.atmosphere.commons.beans.PhoneNumber;
import com.musala.atmosphere.commons.beans.SwipeDirection;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.gesture.Gesture;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.ui.UiElementDescriptor;
import com.musala.atmosphere.commons.util.Pair;

public abstract class AbstractWrapDevice extends UnicastRemoteObject implements IWrapDevice {
    /**
     * auto generated serialization id
     */
    private static final long serialVersionUID = -9122701818928360023L;

    private static final Logger LOGGER = Logger.getLogger(AbstractWrapDevice.class.getCanonicalName());

    // WARNING : do not change the remote folder unless you really know what you
    // are doing.
    private static final String XMLDUMP_REMOTE_FILE_NAME = "/data/local/tmp/uidump-%s.xml";

    private static final String XMLDUMP_COMMAND = "uiautomator dump %s";

    private static final String XMLDUMP_LOCAL_FILE_NAME = "uidump-%s.xml";

    private static final String SCREENSHOT_REMOTE_FILE_NAME = "/data/local/tmp/remote_screen.png";

    private static final String SCREENSHOT_COMMAND = "screencap -p " + SCREENSHOT_REMOTE_FILE_NAME;

    private static final String FORCE_STOP_PROCESS_COMMAND = "am force-stop ";

    // TODO the command should be moved to a different enumeration
    private static final String UNINSTALL_APP_COMMAND = "pm uninstall ";

    private static final String STOP_BACKGROUND_PROCESS_COMMAND = "am kill ";

    private static final String SCREENSHOT_LOCAL_FILE_NAME = "local_screen.png";

    private static final String RAM_MEMORY_PATTERN = "(\\w+):(\\s+)(\\d+\\w+)";

    private static final String DEVICE_TYPE = "tablet";

    protected final ServiceCommunicator serviceCommunicator;

    protected final UIAutomatorCommunicator uiAutomatorBridgeCommunicator;

    protected final FileTransferService transferService;

    private static final String GET_RAM_MEMORY_COMMAND = "cat /proc/meminfo | grep MemTotal";

    protected final ShellCommandExecutor shellCommandExecutor;

    protected final IDevice wrappedDevice;

    private final ApkInstaller apkInstaller;

    private final ImeManager imeManager;

    public AbstractWrapDevice(IDevice deviceToWrap) throws RemoteException {
        wrappedDevice = deviceToWrap;
        transferService = new FileTransferService(wrappedDevice);
        shellCommandExecutor = new ShellCommandExecutor(wrappedDevice);
        apkInstaller = new ApkInstaller(wrappedDevice);
        imeManager = new ImeManager(shellCommandExecutor);

        uiAutomatorBridgeCommunicator = new UIAutomatorCommunicator(shellCommandExecutor, transferService);

        PortForwardingService forwardingService = new PortForwardingService(wrappedDevice);
        try {
            serviceCommunicator = new ServiceCommunicator(forwardingService,
                                                          shellCommandExecutor,
                                                          deviceToWrap.getSerialNumber());
        } catch (ForwardingPortFailedException | OnDeviceComponentStartingException
                | OnDeviceComponentInitializationException e) {
            // TODO throw a new exception here when the preconditions are
            // implemented.

            String errorMessage = String.format("Could not initialize communication to a on-device component for %s.",
                                                wrappedDevice.getSerialNumber());
            throw new OnDeviceComponentCommunicationException(errorMessage, e);
        }
    }

    @Override
    public Object route(RoutingAction action, Object... args) throws RemoteException, CommandFailedException {
        try {
            action.validateArguments(args);
        } catch (IllegalArgumentException e) {
            throw new CommandFailedException("Command arguments are not valid.", e);
        }

        Object returnValue = null;

        switch (action) {
        // Shell command related
            case EXECUTE_SHELL_COMMAND:
                returnValue = shellCommandExecutor.execute((String) args[0]);
                break;
            case EXECUTE_SHELL_COMMAND_SEQUENCE:
                returnValue = shellCommandExecutor.executeSequence((List<String>) args[0]);
                break;

            // APK file installation related
            case APK_INIT_INSTALL:
                apkInstaller.initAPKInstall();
                break;
            case APK_APPEND_DATA:
                apkInstaller.appendToAPK((byte[]) args[0], (int) args[1]);
                break;
            case APK_BUILD_AND_INSTALL:
                apkInstaller.buildAndInstallAPK((boolean) args[0]);
                break;
            case APK_DISCARD:
                apkInstaller.discardAPK();
                break;

            // Getters
            case GET_DEVICE_INFORMATION:
                returnValue = getDeviceInformation();
                break;
            case GET_SCREENSHOT:
                returnValue = getScreenshot();
                break;
            case GET_UI_XML_DUMP:
                returnValue = getUiXml();
                break;
            case GET_POWER_PROPERTIES:
                returnValue = serviceCommunicator.getPowerProperties();
                break;
            case GET_TELEPHONY_INFO:
                returnValue = serviceCommunicator.getTelephonyInformation();
                break;
            case GET_CONNECTION_TYPE:
                returnValue = serviceCommunicator.getConnectionType();
                break;
            case GET_DEVICE_ORIENTATION:
                returnValue = serviceCommunicator.getDeviceOrientation();
                break;
            case GET_DEVICE_ACCELERATION:
                returnValue = serviceCommunicator.getAcceleration();
                break;
            case GET_DEVICE_PROXIMITY:
                returnValue = serviceCommunicator.getProximity();
                break;
            case GET_FREE_RAM:
                returnValue = getFreeRAM();
                break;
            case GET_MOBILE_DATA_STATE:
                returnValue = getMobileDataState();
                break;
            case GET_AWAKE_STATUS:
                returnValue = serviceCommunicator.getAwakeStatus();
                break;
            case GET_PROCESS_RUNNING:
                returnValue = serviceCommunicator.getProcessRunning(args);
                break;
            case GET_RUNNING_TASK_IDS:
                returnValue = serviceCommunicator.getRunningTaskIds(args);
                break;

            // Setters
            case SET_POWER_PROPERTIES:
                setPowerProperties((PowerProperties) args[0]);
                break;
            case SET_WIFI_STATE:
                serviceCommunicator.setWiFi((boolean) args[0]);
                break;
            case SET_MOBILE_DATA_STATE:
                setMobileDataState((MobileDataState) args[0]);
                break;
            case SET_ACCELERATION:
                setAcceleration((DeviceAcceleration) args[0]);
                break;
            case SET_ORIENTATION:
                setOrientation((DeviceOrientation) args[0]);
                break;
            case SET_NETWORK_SPEED:
                setNetworkSpeed((Pair<Integer, Integer>) args[0]);
                break;
            case SET_MAGNETIC_FIELD:
                setMagneticField((DeviceMagneticField) args[0]);
                break;
            case SET_PROXIMITY:
                setProximity((float) args[0]);
                break;
            case SET_KEYGUARD:
                serviceCommunicator.setKeyguard(args);
                break;

            // Misc functionalities
            case WAIT_FOR_EXISTS:
                returnValue = uiAutomatorBridgeCommunicator.waitForExists((UiElementDescriptor) args[0],
                                                                          (Integer) args[1],
                                                                          wrappedDevice.getSerialNumber());
                break;
            case WAIT_UNTIL_GONE:
                returnValue = uiAutomatorBridgeCommunicator.waitUntilGone((UiElementDescriptor) args[0],
                                                                          (Integer) args[1],
                                                                          wrappedDevice.getSerialNumber());
                break;
            case WAIT_FOR_WINDOW_UPDATE:
                returnValue = uiAutomatorBridgeCommunicator.waitForWindowUpdate((String) args[0],
                                                                                (int) args[1],
                                                                                wrappedDevice.getSerialNumber());
                break;
            case OPEN_NOTIFICATION_BAR:
                returnValue = uiAutomatorBridgeCommunicator.openNotificationBar(wrappedDevice.getSerialNumber());
                break;
            case OPEN_QUICK_SETTINGS:
                returnValue = uiAutomatorBridgeCommunicator.openQuickSettings(wrappedDevice.getSerialNumber());
                break;
            case PLAY_GESTURE:
                uiAutomatorBridgeCommunicator.playGesture((Gesture) args[0]);
                break;
            case CLEAR_FIELD:
                uiAutomatorBridgeCommunicator.clearField((UiElementDescriptor) args[0]);
                break;
            case ELEMENT_SWIPE:
                uiAutomatorBridgeCommunicator.swipeElement((UiElementDescriptor) args[0], (SwipeDirection) args[1]);
                break;
            case START_APP:
                returnValue = serviceCommunicator.startApplication(args);
                break;
            case UNINSTALL_APP:
                uninstallApplication((String) args[0]);
                break;
            case FORCE_STOP_PROCESS:
                forceStopProcess((String) args[0]);
                break;
            case STOP_BACKGROUND_PROCESS:
                stopBackgroundProcess((String) args[0]);
                break;
            case SET_DEFAULT_INPUT_METHOD:
                returnValue = imeManager.setDefaultKeyboard((String) args[0]);
                break;
            case SET_ATMOSPHERE_IME_AS_DEFAULT:
                returnValue = imeManager.setAtmosphereImeAsDefault();
                break;
            case MOCK_LOCATION:
                returnValue = serviceCommunicator.mockLocation(args);
                break;
            case DISABLE_MOCK_LOCATION:
                serviceCommunicator.disableMockLocation(args);
                break;
            case BRING_TASK_TO_FRONT:
                returnValue = serviceCommunicator.bringTaskToFront(args);
                break;
            case WAIT_FOR_TASKS_UPDATE:
                returnValue = serviceCommunicator.waitForTasksUpdate(args);
                break;
            case SEND_BROADCAST:
                serviceCommunicator.sendBroadcast(args);
                break;

            // Call related
            case CALL_CANCEL:
                cancelCall((PhoneNumber) args[0]);
                break;
            case CALL_HOLD:
                holdCall((PhoneNumber) args[0]);
                break;
            case CALL_RECEIVE:
                receiveCall((PhoneNumber) args[0]);
                break;
            case CALL_ACCEPT:
                acceptCall((PhoneNumber) args[0]);
                break;

            // SMS related
            case SMS_RECEIVE:
                receiveSms((SmsMessage) args[0]);
                break;

            // Scrollable View related
            case SCROLL_TO_DIRECTION:
                returnValue = uiAutomatorBridgeCommunicator.scrollToDirection((ScrollDirection) args[0],
                                                                              (UiElementDescriptor) args[1],
                                                                              (Integer) args[2],
                                                                              (Integer) args[3],
                                                                              (Boolean) args[4],
                                                                              wrappedDevice.getSerialNumber());
                break;
        }

        return returnValue;
    }

    /**
     * Gets the amount of free RAM on the device.
     * 
     * @return Memory amount in MB.
     * @throws CommandFailedException
     */
    private long getFreeRAM() throws CommandFailedException {
        DeviceProfiler profiler = new DeviceProfiler(wrappedDevice);
        try {
            Map<String, Long> memUsage = profiler.getMeminfoDataset();
            long freeMemory = memUsage.get(DeviceProfiler.FREE_MEMORY_ID);
            return freeMemory;
        } catch (IOException | TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException e) {
            LOGGER.warn("Getting device '" + wrappedDevice.getSerialNumber() + "' memory usage resulted in exception.",
                        e);
            throw new CommandFailedException("Getting device memory usage resulted in exception.", e);
        }
    }

    /**
     * Gets a {@link DeviceInformation} descriptor structure for the {@link IDevice} in this wrapper.
     * 
     * @return The populated {@link DeviceInformation} instance.
     */
    public DeviceInformation getDeviceInformation() {
        DeviceInformation deviceInformation = new DeviceInformation();

        // Serial number
        deviceInformation.setSerialNumber(wrappedDevice.getSerialNumber());

        // isEmulator
        deviceInformation.setEmulator(wrappedDevice.isEmulator());

        // If the device will not give us it's valid properties, return the
        // structure with the fallback values set.
        if (wrappedDevice.isOffline() || !wrappedDevice.arePropertiesSet()) {
            return deviceInformation;
        }

        // Attempt to get the device properties only if the device is online.
        Map<String, String> devicePropertiesMap = wrappedDevice.getProperties();

        // CPU
        if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_CPU_TYPE.toString())) {
            String cpu = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_CPU_TYPE.toString());
            deviceInformation.setCpu(cpu);
        }

        // Density
        String lcdDensityString = DeviceInformation.FALLBACK_DISPLAY_DENSITY.toString();
        if (wrappedDevice.isEmulator()) {
            if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_EMUDEVICE_LCD_DENSITY.toString())) {
                lcdDensityString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_EMUDEVICE_LCD_DENSITY.toString());
            }
        } else {
            if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_REALDEVICE_LCD_DENSITY.toString())) {
                lcdDensityString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_REALDEVICE_LCD_DENSITY.toString());
            }
        }
        deviceInformation.setDpi(Integer.parseInt(lcdDensityString));

        // Model
        if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_PRODUCT_MODEL.toString())) {
            String productModel = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_PRODUCT_MODEL.toString());
            deviceInformation.setModel(productModel);
        }

        // OS
        if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_OS_VERSION.toString())) {
            String deviceOs = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_OS_VERSION.toString());
            deviceInformation.setOs(deviceOs);
        }

        // API level
        if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_API_LEVEL.toString())) {
            String apiLevelString = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_API_LEVEL.toString());
            int deviceApiLevel = Integer.parseInt(apiLevelString);
            deviceInformation.setApiLevel(deviceApiLevel);
        }

        // Manufacturer
        if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_MANUFACTURER_NAME.toString())) {
            String manufacturerName = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_MANUFACTURER_NAME.toString());
            deviceInformation.setManufacturer(manufacturerName);
        }

        // RAM
        String ramMemoryString = DeviceInformation.FALLBACK_RAM_AMOUNT.toString();

        try {
            ramMemoryString = shellCommandExecutor.execute(GET_RAM_MEMORY_COMMAND);
        } catch (CommandFailedException e) {
            LOGGER.warn("Getting device RAM failed.", e);
        }

        String extractedRamMemoryString = ramMemoryString.replaceAll(RAM_MEMORY_PATTERN, "$3");

        deviceInformation.setRam(MemoryUnitConverter.convertMemoryToMB(extractedRamMemoryString));

        // isTablet
        if (devicePropertiesMap.containsKey(DevicePropertyStringConstants.PROPERTY_CHARACTERISTICS.toString())) {
            String deviceCharacteristics = devicePropertiesMap.get(DevicePropertyStringConstants.PROPERTY_CHARACTERISTICS.toString());
            boolean isTablet = deviceCharacteristics.contains(DEVICE_TYPE);
            deviceInformation.setTablet(isTablet);
        }

        // Camera
        try {
            boolean hasCamera = serviceCommunicator.getCameraAvailability();
            deviceInformation.setCamera(hasCamera);
        } catch (CommandFailedException e) {
            LOGGER.error("Checking device camera availability failed.", e);
        }

        // Resolution
        try {
            CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
            wrappedDevice.executeShellCommand("dumpsys window policy", outputReceiver);

            String shellResponse = outputReceiver.getOutput();
            Pair<Integer, Integer> screenResolution = DeviceScreenResolutionParser.parseScreenResolutionFromShell(shellResponse);
            deviceInformation.setResolution(screenResolution);

        } catch (ShellCommandUnresponsiveException | TimeoutException | AdbCommandRejectedException | IOException e) {
            // Shell command execution failed.
            LOGGER.error("Shell command execution failed.", e);
        } catch (StringIndexOutOfBoundsException e) {
            LOGGER.warn("Parsing shell response failed when attempting to get device screen size.");
        }

        return deviceInformation;
    }

    /**
     * Returns a JPEG compressed display screenshot.
     * 
     * @return Image in an array of bytes that, when dumped to a file, shows the device display.
     * @throws CommandFailedException
     */
    private byte[] getScreenshot() throws CommandFailedException {
        shellCommandExecutor.execute(SCREENSHOT_COMMAND);

        FileInputStream fileReader = null;

        try {
            wrappedDevice.pullFile(SCREENSHOT_REMOTE_FILE_NAME, SCREENSHOT_LOCAL_FILE_NAME);

            File localScreenshotFile = new File(SCREENSHOT_LOCAL_FILE_NAME);
            fileReader = new FileInputStream(localScreenshotFile);

            final long sizeOfScreenshotFile = localScreenshotFile.length();
            byte[] screenshotData = new byte[(int) sizeOfScreenshotFile];
            fileReader.read(screenshotData);
            fileReader.close();

            return screenshotData;
        } catch (IOException | AdbCommandRejectedException | TimeoutException | SyncException e) {
            LOGGER.error("Screenshot fetching failed.", e);
            throw new CommandFailedException("Screenshot fetching failed.", e);
        }
    }

    /**
     * Gets the UIAutomator UI XML dump.
     * 
     * @return UI XML file dump in a string.
     * @throws CommandFailedException
     */
    private String getUiXml() throws CommandFailedException {

        String remoteFileName = String.format(XMLDUMP_REMOTE_FILE_NAME, wrappedDevice.getSerialNumber());
        String localFileName = String.format(XMLDUMP_LOCAL_FILE_NAME, wrappedDevice.getSerialNumber());
        String xmlDumpCommand = String.format(XMLDUMP_COMMAND, remoteFileName);
        shellCommandExecutor.execute(xmlDumpCommand);

        try {
            wrappedDevice.pullFile(remoteFileName, localFileName);

            File xmlDumpFile = new File(localFileName);
            Scanner xmlDumpFileScanner = new Scanner(xmlDumpFile, "UTF-8");
            xmlDumpFileScanner.useDelimiter("\\Z");
            String uiDumpContents = xmlDumpFileScanner.next();

            xmlDumpFileScanner.close();
            xmlDumpFile.delete();

            return uiDumpContents;
        } catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
            LOGGER.error("UI dump failed.", e);
            throw new CommandFailedException("UI dump failed. See the enclosed exception for more information.", e);
        }
    }

    /**
     * Executes a force-stop process command
     * 
     * @param args
     *        - containing the package of the force-stopped process
     * @throws CommandFailedException
     */
    private void forceStopProcess(String args) throws CommandFailedException {
        shellCommandExecutor.execute(FORCE_STOP_PROCESS_COMMAND + args);
    }

    /**
     * Stops a background process by a given package name.
     * 
     * @param args
     *        - containing the package of the process.
     * @throws CommandFailedException
     */
    private void stopBackgroundProcess(String args) throws CommandFailedException {
        shellCommandExecutor.execute(STOP_BACKGROUND_PROCESS_COMMAND + args);
    }

    /**
     * Uninstalls an application by a given package name.
     * 
     * @param args
     *        - containing the package of the process.
     * @throws CommandFailedException
     */
    private void uninstallApplication(String args) throws CommandFailedException {
        shellCommandExecutor.execute(UNINSTALL_APP_COMMAND + args);
    }

    @Override
    protected void finalize() {
        shellCommandExecutor.terminateAllInBackground();

        try {
            serviceCommunicator.stopAtmosphereService();
        } catch (OnDeviceServiceTerminationException e) {
            String loggerMessage = String.format("Stopping ATMOSPHERE service failed for %s.",
                                                 wrappedDevice.getSerialNumber());
            LOGGER.warn(loggerMessage, e);
        }
    }

    /**
     * @return the {@link ShellCommandExecutor} associated with the wrapped {@link IDevice} in this instance.
     */
    public ShellCommandExecutor getShellCommandExecutor() {
        return shellCommandExecutor;
    }

    abstract protected void setPowerProperties(PowerProperties properties) throws CommandFailedException;

    abstract protected void cancelCall(PhoneNumber n) throws CommandFailedException;

    abstract protected void receiveCall(PhoneNumber phoneNumber) throws CommandFailedException;

    abstract protected void acceptCall(PhoneNumber phoneNumber) throws CommandFailedException;

    abstract protected void holdCall(PhoneNumber phoneNumber) throws CommandFailedException;

    abstract protected void receiveSms(SmsMessage smsMessage) throws CommandFailedException;

    abstract protected void setOrientation(DeviceOrientation deviceOrientation) throws CommandFailedException;

    abstract protected void setAcceleration(DeviceAcceleration deviceAcceleration) throws CommandFailedException;

    abstract protected void setMagneticField(DeviceMagneticField deviceMagneticField) throws CommandFailedException;

    abstract protected void setProximity(float proximity) throws CommandFailedException;

    abstract protected void setMobileDataState(MobileDataState state) throws CommandFailedException;

    abstract protected MobileDataState getMobileDataState() throws CommandFailedException;

    abstract protected void setNetworkSpeed(Pair<Integer, Integer> speeds) throws CommandFailedException;
}
