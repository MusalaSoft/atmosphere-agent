package com.musala.atmosphere.agent.devicewrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriverService;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Mp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.musala.atmosphere.agent.DevicePropertyStringConstants;
import com.musala.atmosphere.agent.devicewrapper.util.ApkInstaller;
import com.musala.atmosphere.agent.devicewrapper.util.BackgroundPullFileTask;
import com.musala.atmosphere.agent.devicewrapper.util.BackgroundShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.Buffer;
import com.musala.atmosphere.agent.devicewrapper.util.DeviceProfiler;
import com.musala.atmosphere.agent.devicewrapper.util.FileTransferService;
import com.musala.atmosphere.agent.devicewrapper.util.ImeManager;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.agent.exception.OnDeviceServiceTerminationException;
import com.musala.atmosphere.agent.util.DeviceScreenResolutionParser;
import com.musala.atmosphere.agent.util.FileRecycler;
import com.musala.atmosphere.agent.util.FtpFileTransferService;
import com.musala.atmosphere.agent.webview.WebElementManager;
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
import com.musala.atmosphere.commons.ui.UiElementPropertiesContainer;
import com.musala.atmosphere.commons.ui.selector.UiElementSelector;
import com.musala.atmosphere.commons.ui.tree.AccessibilityElement;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.commons.webelement.action.WebElementAction;
import com.musala.atmosphere.commons.webelement.action.WebElementWaitCondition;
import com.musala.atmosphere.commons.webview.selection.WebViewSelectionCriterion;

public abstract class AbstractWrapDevice implements IWrapDevice {

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

    private static final String LIST_RUNNING_PROCESSES_COMMAND = "ps";

    private static final String FORCE_STOP_PROCESS_COMMAND = "am force-stop ";

    // TODO the command should be moved to a different enumeration
    private static final String UNINSTALL_APP_COMMAND = "pm uninstall ";

    private static final String CHANGE_APP_PERMISSION_COMMAND = "pm %s %s %s";

    private static final String INTERRUPT_BACKGROUND_PROCESS_FORMAT = "kill -SIGINT $(%s)";

    private static final String GET_PID_FORMAT = "ps %s %s";

    private static final String GET_PID_PATTERN = "| grep -Eo [0-9]+ | grep -m 1 -Eo [0-9]+";

    private static final String SCREENSHOT_LOCAL_FILE_NAME = "local_screen.png";

    private static final String FIRST_SCREEN_RECORD_NAME = "100.mp4";

    private static final String FALLBACK_COMPONENT_PATH = "/data/local/tmp";

    private static final String RECORDS_DIRECTORY_NAME = "AtmosphereScreenRecords";

    private static final String START_SCREENRECORD_SCRIPT_NAME = "start_screenrecord.sh";

    private static final String STOP_SCREENRECORD_SCRIPT_NAME = "stop_screenrecord.sh";

    private static final String START_SCREEN_RECORD_COMMAND = String.format("sh %s/%s ",
                                                                            FALLBACK_COMPONENT_PATH,
                                                                            START_SCREENRECORD_SCRIPT_NAME);

    private static final String STOP_SCREEN_RECORD_COMMAND = String.format("sh %s/%s ",
                                                                           FALLBACK_COMPONENT_PATH,
                                                                           STOP_SCREENRECORD_SCRIPT_NAME);

    private static final String SCREEN_RECORDS_LOCAL_DIR = System.getProperty("user.dir");

    private static final String MERGED_RECORDS_DIR_NAME = "ScreenRecords";

    private static final String RECORDS_FILENAMES_DELIMITER = "\r\n";

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd-HH-mm-ss";

    private static final String DEVICE_TYPE = "tablet";

    private static final String CLEAR_APP_DATA_COMMAND = "pm clear";

    private static final String GET_DEVICE_LOGCAT = "logcat -d -f ";

    private static final String CLEAR_DEVICE_LOGCAT = "logcat -c";

    // TODO: Consider to remove field because is not used directly
    private ExecutorService executor;

    private CompletionService<Boolean> pullFileCompletionService;

    protected final ServiceCommunicator serviceCommunicator;

    protected final UIAutomatorCommunicator automatorCommunicator;

    protected final FileTransferService transferService;

    protected final BackgroundShellCommandExecutor shellCommandExecutor;

    protected final IDevice wrappedDevice;

    protected FileRecycler fileRecycler;

    private final ApkInstaller apkInstaller;

    private final ImeManager imeManager;

    private WebElementManager webElementManager;

    private Buffer<String, Pair<Integer, String>> logcatBuffer;

    private FtpFileTransferService ftpFileTransferService;

    /**
     * Creates an abstract wrapper of the given {@link IDevice device}.
     *
     * @param deviceToWrap
     *        - device to be wrapped
     * @param executor
     *        - an {@link ExecutorService} used for async tasks
     * @param shellCommandExecutor
     *        - an executor of shell commands for the wrapped device
     * @param serviceCommunicator
     *        - a communicator to the service component on the device
     * @param automatorCommunicator
     *        - a communicator to the UI automator component on the device
     * @param chromeDriverService
     *        - the service component of the ChromeDriver
     * @param fileRecycler
     *        - responsible for removing obsolete files
     * @param ftpFileTransferService
     *        - responsible for file transfers to the FTP server
     */
    public AbstractWrapDevice(IDevice deviceToWrap,
            ExecutorService executor,
            BackgroundShellCommandExecutor shellCommandExecutor,
            ServiceCommunicator serviceCommunicator,
            UIAutomatorCommunicator automatorCommunicator,
            ChromeDriverService chromeDriverService,
            FileRecycler fileRecycler,
            FtpFileTransferService ftpFileTransferService) {
        // TODO: Use a dependency injection mechanism here.
        this.wrappedDevice = deviceToWrap;
        this.executor = executor;
        this.shellCommandExecutor = shellCommandExecutor;
        this.serviceCommunicator = serviceCommunicator;
        this.automatorCommunicator = automatorCommunicator;
        this.fileRecycler = fileRecycler;
        this.logcatBuffer = new Buffer<String, Pair<Integer, String>>();
        this.ftpFileTransferService = ftpFileTransferService;

        transferService = new FileTransferService(wrappedDevice);
        apkInstaller = new ApkInstaller(wrappedDevice);
        imeManager = new ImeManager(shellCommandExecutor);
        pullFileCompletionService = new ExecutorCompletionService<>(executor);

        webElementManager = new WebElementManager(chromeDriverService, deviceToWrap.getSerialNumber());
    }

    @Override
    public Object route(RoutingAction action, Object... args) throws CommandFailedException {
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
            case EXECUTE_SHELL_COMMAND_IN_BACKGROUND:
                shellCommandExecutor.executeInBackground((String) args[0]);
                break;
            case INTERRUPT_BACKGROUND_SHELL_PROCESS:
                interruptBackgroundShellProcess((String) args[0]);
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
            case GET_UI_TREE:
                returnValue = automatorCommunicator.getUiTree((boolean) args[0]);
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
                returnValue = isProcessRunning((String) args[0]);
                break;
            case GET_RUNNING_TASK_IDS:
                returnValue = serviceCommunicator.getRunningTaskIds(args);
                break;
            case GET_LAST_TOAST:
                returnValue = automatorCommunicator.getLastToast();
                break;
            case GET_UI_ELEMENTS:
                returnValue = automatorCommunicator.getUiElements((UiElementSelector) args[0], (Boolean) args[1]);
                break;
            case GET_CHILDREN:
                returnValue = automatorCommunicator.getChildren((AccessibilityElement) args[0],
                                                                (UiElementSelector) args[1],
                                                                (Boolean) args[2],
                                                                (Boolean) args[3]);
                break;
            case EXECUTE_XPATH_QUERY_ON_LOCAL_ROOT:
                returnValue = automatorCommunicator.executeXpathQueryOnLocalRoot((String) args[0],
                                                                                 (Boolean) args[1],
                                                                                 (AccessibilityElement) args[2]);
                break;
            case EXECUTE_XPATH_QUERY:
                returnValue = automatorCommunicator.executeXpathQuery((String) args[0], (Boolean) args[1]);
                break;
            case CHECK_ELEMENT_PRESENCE:
                returnValue = automatorCommunicator.isElementPresent((AccessibilityElement) args[0], (Boolean) args[1]);
                break;
            // Logcat related
            case GET_DEVICE_LOGCAT:
                returnValue = getDeviceLogcat((String) args[0]);
                break;
            case CLEAR_LOGCAT:
                clearDeviceLogcat(CLEAR_DEVICE_LOGCAT);
                break;
            case START_DEVICE_LOGCAT:
                startDeviceLogcat((String) args[0], (String) args[1]);
                break;
            case GET_LOGCAT_BUFFER:
                returnValue = getNewOutputFromLogcatBuffer((String) args[0]);
                break;
            case STOP_LOGCAT:
                stopLogcat((String) args[0]);
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
                returnValue = automatorCommunicator.waitForExists((UiElementPropertiesContainer) args[0],
                                                                  (Integer) args[1]);
                break;
            case WAIT_UNTIL_GONE:
                returnValue = automatorCommunicator.waitUntilGone((UiElementPropertiesContainer) args[0],
                                                                  (Integer) args[1]);
                break;
            case WAIT_FOR_WINDOW_UPDATE:
                returnValue = automatorCommunicator.waitForWindowUpdate((String) args[0], (int) args[1]);
                break;
            case OPEN_NOTIFICATION_BAR:
                returnValue = automatorCommunicator.openNotificationBar();
                break;
            case OPEN_QUICK_SETTINGS:
                returnValue = automatorCommunicator.openQuickSettings();
                break;
            case PLAY_GESTURE:
                automatorCommunicator.playGesture((Gesture) args[0]);
                break;
            case CLEAR_FIELD:
                automatorCommunicator.clearField((UiElementPropertiesContainer) args[0]);
                break;
            case ELEMENT_SWIPE:
                automatorCommunicator.swipeElement((UiElementPropertiesContainer) args[0], (SwipeDirection) args[1]);
                break;
            case START_APP:
                returnValue = serviceCommunicator.startApplication(args);
                break;
            case UNINSTALL_APP:
                uninstallApplication((String) args[0]);
                break;
            case GRANT_APP_PERMISSION:
                returnValue = setApplicationPermission(true, (String) args[0], (String) args[1]);
                break;
            case REVOKE_APP_PERMISSION:
                returnValue = setApplicationPermission(false, (String) args[0], (String) args[1]);
                break;
            case FORCE_STOP_PROCESS:
                forceStopProcess((String) args[0]);
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
            case PULL_FILE:
                pullFile((String) args[0], (String) args[1]);
                break;
            case IS_LOCKED:
                returnValue = serviceCommunicator.isLocked();
                break;
            case OPEN_LOCATION_SETTINGS:
                serviceCommunicator.openLocationSettings();
                break;
            case IS_GPS_LOCATION_ENABLED:
                returnValue = serviceCommunicator.isGpsLocationEnabled();
                break;
            case SHOW_TAP_LOCATION:
                serviceCommunicator.showTapLocation(args);
                break;
            case IS_AUDIO_PLAYING:
                returnValue = serviceCommunicator.isAudioPlaying();
                break;
            case CLEAR_APP_DATA:
                clearApplicationData((String) args[0]);
                break;
            case STOP_BACKGROUND_PROCESS:
                serviceCommunicator.stopBackgroundProcess(args);
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
                returnValue = automatorCommunicator.scrollToDirection((ScrollDirection) args[0],
                                                                      (UiElementPropertiesContainer) args[1],
                                                                      (Integer) args[2],
                                                                      (Integer) args[3],
                                                                      (Boolean) args[4]);
                break;

            // Screen recording related
            case START_RECORDING:
                startScreenRecording((Integer) args[0], (Boolean) args[1]);
                break;
            case STOP_RECORDING:
                stopScreenRecording((String) args[0]);
                break;

            // WiFi connection properties related
            case SHAPE_DEVICE:
                returnValue = serviceCommunicator.shapeDevice(args);
                break;
            case UNSHAPE_DEVICE:
                returnValue = serviceCommunicator.unshapeDevice();
                break;

            // WebView Related
            case GET_WEB_VIEW:
                webElementManager.initDriver((String) args[0]);
                break;
            case SET_WEB_VIEW_IMPLICIT_WAIT:
                webElementManager.setImplicitWait((Integer) args[0]);
                break;
            case GET_WEB_VIEWS:
                returnValue = webElementManager.getWindowHandlers();
                break;
            case SWITCH_TO_WEBVIEW:
                webElementManager.switchToWebViewBy((WebViewSelectionCriterion) args[0], (String) args[1]);
                break;
            case SWITCH_TO_WEBVIEW_BY_CHILD:
                webElementManager.switchToWebViewByXpathQuery((String) args[0]);
                break;
            case GET_WEBVIEW_TITLE:
                returnValue = webElementManager.getWebViewTitle();
                break;
            case GET_WEBVIEW_URL:
                returnValue = webElementManager.getWebViewCurrentUrl();
                break;
            case FIND_WEB_ELEMENT:
                returnValue = webElementManager.findElement((String) args[0]);
                break;
            case FIND_WEB_ELEMENTS:
                returnValue = webElementManager.findElements((String) args[0]);
                break;
            case CLOSE_CHROME_DRIVER:
                webElementManager.closeDriver();
                break;
            case WEB_ELEMENT_ACTION:
                returnValue = webElementManager.executeAction((WebElementAction) args[0], (String) args[1]);
                break;
            case GET_CSS_VALUE:
                returnValue = webElementManager.getCssValue((String) args[0], (String) args[1]);
                break;
            case WAIT_FOR_WEB_ELEMENT:
                returnValue = webElementManager.waitForCondition((String) args[0],
                                                                 (WebElementWaitCondition) args[1],
                                                                 (Integer) args[2]);
                break;
            case GET_AVAILABLE_DISK_SPACE:
                returnValue = serviceCommunicator.getAvailableDiskSpace();
                break;
        }

        return returnValue;
    }

    /**
     * Stops the LogCat buffering.
     *
     * @param deviceSerialNumber
     *        - the serial number of the device
     */
    private void stopLogcat(String deviceSerialNumber) {
        logcatBuffer.remove(deviceSerialNumber);
    }

    /**
     * Gets the newly added lines to the LogCat {@link Buffer}.
     *
     * @param deviceSerialNumber
     *        - the serial number of the target device
     * @return a list of string lines
     */
    public List<Pair<Integer, String>> getNewOutputFromLogcatBuffer(String deviceSerialNumber) {
        return logcatBuffer.getBuffer(deviceSerialNumber);
    }

    /**
     * Starts a LogCat for a specific device on the agent and add the output to the {@link Buffer}.
     *
     * @param deviceSerialNumber
     *        - the serial number of the target device
     * @param startLogcatCommand
     *        - an ADB for starting a LogCat.
     * @throws CommandFailedException
     *         thrown when fails to execute the command
     */
    private void startDeviceLogcat(String deviceSerialNumber, final String startLogcatCommand)
        throws CommandFailedException {
        logcatBuffer.addKey(deviceSerialNumber);

        Runtime runtime = Runtime.getRuntime();
        Process adbProcess = null;

        try {
            int controlLineId = 0;
            adbProcess = runtime.exec(startLogcatCommand);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(adbProcess.getInputStream()));
            String currentLine = null;

            while (logcatBuffer.contains(deviceSerialNumber)) {
                currentLine = bufferedReader.readLine();
                if (!currentLine.isEmpty()) {
                    Pair<Integer, String> idToLogLine = new Pair<Integer, String>(controlLineId, currentLine);
                    logcatBuffer.addValue(deviceSerialNumber, idToLogLine);
                    controlLineId++;
                }
            }
        } catch (IOException e) {
            throw new CommandFailedException();
        } finally {
            if (adbProcess != null) {
                adbProcess.destroy();
            }
        }
    }

    /**
     * Clears a LogCat from a device with a specific serial number.
     *
     * @param clearLogcatCommand
     *        - an ADB command that clears a LogCat
     * @throws CommandFailedException
     *         thrown when fails to clear a LogCat
     */
    private void clearDeviceLogcat(String clearLogcatCommand) throws CommandFailedException {
        shellCommandExecutor.execute(clearLogcatCommand);
    }

    /**
     * Gets the information retrieved from the device LogCat as a sequence of bytes applying the given filter.
     *
     * @param logFilter
     *        - String representing the filter to be applied when retrieving the log
     * @return array of bytes containing the information retrieved from the device LogCat
     * @throws CommandFailedException
     *         if LogCat command fails
     */
    private byte[] getDeviceLogcat(String logFilter) throws CommandFailedException {
        String deviceLogcatFileName = String.format("device_%s.log", this.getDeviceInformation().getSerialNumber());
        String externalStorage = serviceCommunicator.getExternalStorage();
        String remoteLogParentDir = externalStorage != null ? externalStorage : FALLBACK_COMPONENT_PATH;
        String remoteLogDir = String.format("%s/%s", remoteLogParentDir, deviceLogcatFileName);

        shellCommandExecutor.execute(GET_DEVICE_LOGCAT + remoteLogDir + logFilter);

        try {
            wrappedDevice.pullFile(remoteLogDir, deviceLogcatFileName);
            File localLogFile = new File(deviceLogcatFileName);
            long fileLenght = localLogFile.length();
            byte[] logData = new byte[(int) fileLenght];
            FileInputStream fileInputStream = new FileInputStream(localLogFile);
            fileInputStream.read(logData);
            fileInputStream.close();

            // clears the LogCat file from the agent
            localLogFile.delete();
            // clears the LogCat file from the device
            String removeFileCommand = String.format("rm %s", remoteLogDir);
            shellCommandExecutor.execute(removeFileCommand);

            return logData;
        } catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
            String errorMessage = String.format("Getting log for device %s failed.", wrappedDevice.getSerialNumber());
            LOGGER.error(errorMessage, e);
            throw new CommandFailedException(errorMessage, e);
        }
    }

    /**
     * Gets the amount of free RAM on the device.
     *
     * @return Memory amount in MB.
     * @throws CommandFailedException
     *         In case of an error in the execution
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
        try {
            int ramMemory = serviceCommunicator.getTatalRamMemory();
            deviceInformation.setRam(ramMemory);
        } catch (CommandFailedException e) {
            String gettingRamFailedMessage = String.format("Getting total RAM of device [%s] failed.",
                                                           wrappedDevice.getSerialNumber());
            LOGGER.error(gettingRamFailedMessage, e);
        }

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
     *         In case of an error in the execution
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
     * @return UI XML file dump in a string
     * @throws CommandFailedException
     *         when UI XML dump fails
     */
    private String getUiXml() throws CommandFailedException {
        String remoteFileName = String.format(XMLDUMP_REMOTE_FILE_NAME, wrappedDevice.getSerialNumber());
        String localFileName = String.format(XMLDUMP_LOCAL_FILE_NAME, wrappedDevice.getSerialNumber());
        automatorCommunicator.getUiDumpXml(remoteFileName);

        File xmlDumpFile = null;
        Scanner xmlDumpFileScanner = null;

        try {
            wrappedDevice.pullFile(remoteFileName, localFileName);

            xmlDumpFile = new File(localFileName);
            xmlDumpFileScanner = new Scanner(xmlDumpFile, "UTF-8");
            xmlDumpFileScanner.useDelimiter("\\Z");

            if (!xmlDumpFileScanner.hasNext()) {
                throw new CommandFailedException("Error obtaining UI hierarchy.");
            }

            return xmlDumpFileScanner.next();
        } catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
            LOGGER.error("UI dump failed.", e);
            throw new CommandFailedException("UI dump failed. See the enclosed exception for more information.", e);
        } finally {
            if (xmlDumpFile != null && xmlDumpFileScanner != null) {
                xmlDumpFileScanner.close();
                xmlDumpFile.delete();
            }
        }
    }

    /**
     * Checks if there are running processes on the device with the given package name.
     *
     * @param packageName
     *        - the package name to look for
     * @return <code>true</code> if there are such running processes, <code>false</code> otherwise
     * @throws CommandFailedException
     *         when the shell command to retrieve the running processes fails
     */
    private boolean isProcessRunning(String packageName) throws CommandFailedException {
        // Note: if this command is run on a device directly, not all running processes will be returned, so we use adb.
        String output = shellCommandExecutor.execute(LIST_RUNNING_PROCESSES_COMMAND);
        return output.contains(packageName);
    }

    /**
     * Executes a force-stop process command
     *
     * @param args
     *        - containing the package of the force-stopped process
     * @throws CommandFailedException
     *         In case of an error in the force stop process
     */
    private void forceStopProcess(String args) throws CommandFailedException {
        shellCommandExecutor.execute(FORCE_STOP_PROCESS_COMMAND + args);
    }

    /**
     * Uninstalls an application by a given package name.
     *
     * @param args
     *        - containing the package of the process.
     * @throws CommandFailedException
     *         In case of an error in the uninstallApplication command
     */
    private void uninstallApplication(String args) throws CommandFailedException {
        shellCommandExecutor.execute(UNINSTALL_APP_COMMAND + args);
    }


    /**
     * Grants or revokes an application permission during runtime.
     *
     * @param grantPermission
     *        - <code>true</code> if the permission should be granted or <code>false</code> if it should be revoked
     * @param packageName
     *        - the package name of the application
     * @param permission
     *        - the permission to be granted or revoked
     * @return <code>true</code> if the permission was set successfully, <code>false</code> if such permission is not available
     * @throws CommandFailedException
     *         if the shell command fails
     */
    private boolean setApplicationPermission(boolean grantPermission, String packageName, String permission) throws CommandFailedException {
        if (getDeviceInformation().getApiLevel() < 23) { // Runtime permissions were added in Android 6.0
            serviceCommunicator.showToast("Runtime permissions are not supported on this device.");
            return false;
        }

        String setPermissionAction = grantPermission ? "grant" : "revoke";
        String result = shellCommandExecutor.execute(String.format(CHANGE_APP_PERMISSION_COMMAND,
                                                                   setPermissionAction,
                                                                   packageName,
                                                                   permission));
        if (result.length() == 0) {
            String setPermissionMessage = grantPermission ? "Granted" : "Revoked";
            serviceCommunicator.showToast(String.format("%s permission %s",
                                                        setPermissionMessage,
                                                        permission));
            return true;
        } else {
            serviceCommunicator.showToast(String.format("Failed to %s permission %s",
                                                        setPermissionAction,
                                                        permission));
            return false;
        }
    }

    /**
     * Pulls a single file from the device and save it locally.
     *
     * @param remoteFilePath
     *        - full path to the file which should be pulled
     * @param localFilePath
     *        - full local path to the destination file
     */
    private void pullFile(String remoteFilePath, String localFilePath) {
        // FIXME: Implement sending the file to the client after pulling it locally.
        BackgroundPullFileTask pullFileTask = new BackgroundPullFileTask(wrappedDevice, remoteFilePath, localFilePath);
        pullFileCompletionService.submit(pullFileTask);
    }

    /**
     * Attempts to kill a process running in background with <code>SIGINT</code>.
     *
     * @param processName
     *        - the name of the process to be killed
     * @throws CommandFailedException
     *         - in case of failing to kill the process
     */
    private void interruptBackgroundShellProcess(String processName) throws CommandFailedException {
        String getPidCommand = String.format(GET_PID_FORMAT, processName, GET_PID_PATTERN);
        String killProcessCommand = String.format(INTERRUPT_BACKGROUND_PROCESS_FORMAT, getPidCommand);

        shellCommandExecutor.execute(killProcessCommand);

    }

    public String combineVideoFiles(String directoryPath, String uplaodDirectoryName) throws IOException {
        File file = new File(directoryPath);
        String[] fileNames = file.list();

        Arrays.sort(fileNames);

        int filesCount = fileNames.length;
        Movie[] movies = new Movie[filesCount];

        for (int index = 0; index < filesCount; index++) {
            String currentFilePath = String.format("%s%s%s", directoryPath, File.separator, fileNames[index]);
            movies[index] = MovieCreator.build(currentFilePath);
        }

        List<Track> videoTracks = new LinkedList<>();

        for (Movie movie : movies) {
            for (Track track : movie.getTracks()) {
                if (track.getHandler().equals("vide")) {
                    videoTracks.add(track);
                }
            }
        }

        Movie combinedMovie = new Movie();
        Track[] tracksToCombine = videoTracks.toArray(new Track[videoTracks.size()]);
        AppendTrack appendTrack = new AppendTrack(tracksToCombine);

        combinedMovie.addTrack(appendTrack);

        Mp4Builder videoBuilder = new DefaultMp4Builder();
        Container combinedMovieContainer = videoBuilder.build(combinedMovie);

        File mergedRecordsDirectory = new File(MERGED_RECORDS_DIR_NAME);

        if (!mergedRecordsDirectory.exists()) {
            mergedRecordsDirectory.mkdirs();
        }

        String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());

        uplaodDirectoryName = !uplaodDirectoryName.isEmpty() ? uplaodDirectoryName + "_" : uplaodDirectoryName;
        String screenRecordFileName = String.format("%s%s%s%s%s%s_%s_screen_record.mp4",
                                                    SCREEN_RECORDS_LOCAL_DIR,
                                                    File.separator,
                                                    MERGED_RECORDS_DIR_NAME,
                                                    File.separator,
                                                    uplaodDirectoryName,
                                                    timestamp,
                                                    wrappedDevice.getSerialNumber());

        RandomAccessFile randomAccessFile = new RandomAccessFile(screenRecordFileName, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        combinedMovieContainer.writeContainer(fileChannel);

        fileChannel.close();
        randomAccessFile.close();

        return screenRecordFileName;
    }

    private void startScreenRecording(int timeLimit, boolean forceLandscape) throws CommandFailedException {
        String externalStorage = serviceCommunicator.getExternalStorage();
        String recordsParentDir = externalStorage != null ? externalStorage : FALLBACK_COMPONENT_PATH;

        Pair<Integer, Integer> resolution = getDeviceInformation().getResolution();
        int width = Math.max(resolution.getKey(), resolution.getValue());
        int height = Math.min(resolution.getKey(), resolution.getValue());

        String screenResoloution = !forceLandscape ? String.format("%sx%s", height, width)
                : String.format("%sx%s", width, height);
        int timeLimitInSeconds = timeLimit * 60;

        String screenRecordCommand = String.format("%s%s %d %s",
                                                   START_SCREEN_RECORD_COMMAND,
                                                   recordsParentDir,
                                                   timeLimitInSeconds,
                                                   screenResoloution);

        shellCommandExecutor.executeInBackground(screenRecordCommand);
    }

    private void stopScreenRecording(String uplaodDirectoryName) throws CommandFailedException {
        String screenRecordFileName = null;

        String externalStorage = serviceCommunicator.getExternalStorage();
        String recordsParentDir = externalStorage != null ? externalStorage : FALLBACK_COMPONENT_PATH;

        String processNamePrefix = "";
        if (getDeviceInformation().getApiLevel() >= 23) { // Android M
            processNamePrefix = "/system/bin/";
        }

        String command = String.format("%s%s %s", STOP_SCREEN_RECORD_COMMAND, recordsParentDir, processNamePrefix);
        String output = shellCommandExecutor.execute(command);

        if (output.trim().length() <= 0) {
            return;
        }

        String[] screenRecordFilenames = output.split(RECORDS_FILENAMES_DELIMITER);

        if (!screenRecordFilenames[0].equals(FIRST_SCREEN_RECORD_NAME)) {
            return;
        }

        File recordsDirectory = new File(SCREEN_RECORDS_LOCAL_DIR);

        if (!recordsDirectory.exists()) {
            recordsDirectory.mkdirs();
        }

        String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
        String separatedVideosDirectoryPath = String.format("%s%s%s_%s_VideoRecords",
                                                            SCREEN_RECORDS_LOCAL_DIR,
                                                            File.separator,
                                                            timestamp,
                                                            wrappedDevice.getSerialNumber());

        File separateVideosDirectory = new File(separatedVideosDirectoryPath);

        if (!separateVideosDirectory.exists()) {
            separateVideosDirectory.mkdirs();
        }

        for (String filename : screenRecordFilenames) {
            String currentRecordRemotePath = String.format("%s/%s/%s",
                                                           recordsParentDir,
                                                           RECORDS_DIRECTORY_NAME,
                                                           filename);
            String currentRecordLocalPath = String.format("%s%s%s",
                                                          separatedVideosDirectoryPath,
                                                          File.separator,
                                                          filename);

            pullFile(currentRecordRemotePath, currentRecordLocalPath);
        }

        for (int i = 0; i < screenRecordFilenames.length; i++) {
            Future<Boolean> futureTask = null;

            try {
                futureTask = pullFileCompletionService.take();
                Boolean isFilePulled = futureTask.get();

                if (!isFilePulled) {
                    LOGGER.warn(String.format("Pulling some of the video records from device %s failed.",
                                              wrappedDevice.getSerialNumber()));
                }
            } catch (InterruptedException | ExecutionException e) {

            } finally {
                if (futureTask != null) {
                    futureTask.cancel(true);
                }
            }
        }

        try {
            screenRecordFileName = combineVideoFiles(separatedVideosDirectoryPath, uplaodDirectoryName);
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to merge video records pulled from device with serial number %s.",
                                       wrappedDevice.getSerialNumber()),
                         e);
        }

        if (ftpFileTransferService != null && !uplaodDirectoryName.isEmpty()) {
            try {
                ftpFileTransferService.addTransferTask(screenRecordFileName);
            } catch (IOException e) {
                LOGGER.error(String.format("Failed to add \"%s\" to the queue of the pending transfers.",
                                           screenRecordFileName));
            }
        }

        // TODO: Since we don't know when the FileRecycler will remove the files, there is a chance to not remove
        // some of the recorded files, due to Agent.stop(). Find a way to handle this problem.
        fileRecycler.addFile(separatedVideosDirectoryPath);
    }

    /**
     * Clears the data of a given application.
     *
     * @param packageName
     *        - the package name of the given application
     * @throws CommandFailedException
     *         when fails to clear the application data
     */
    private void clearApplicationData(String packageName) throws CommandFailedException {
        String clearApplicationDataCommand = String.format("%s %s", CLEAR_APP_DATA_COMMAND, packageName);

        shellCommandExecutor.execute(clearApplicationDataCommand);
    }

    @Override
    public void unbindWrapper() {
        try {
            serviceCommunicator.stopComponent();
            automatorCommunicator.stopComponent();
        } catch (OnDeviceServiceTerminationException e) {
            String loggerMessage = String.format("Stopping ATMOSPHERE on-device component failed for %s.",
                                                 wrappedDevice.getSerialNumber());
            LOGGER.warn(loggerMessage, e);
        }

        serviceCommunicator.stop();
        automatorCommunicator.stop();
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
