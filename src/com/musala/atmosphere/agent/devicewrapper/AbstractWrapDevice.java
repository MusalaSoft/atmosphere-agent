package com.musala.atmosphere.agent.devicewrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
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
import com.musala.atmosphere.agent.devicewrapper.util.DeviceProfiler;
import com.musala.atmosphere.agent.devicewrapper.util.FileTransferService;
import com.musala.atmosphere.agent.devicewrapper.util.ImeManager;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.agent.exception.OnDeviceServiceTerminationException;
import com.musala.atmosphere.agent.util.DeviceScreenResolutionParser;
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
import com.musala.atmosphere.commons.ui.selector.UiElementSelector;
import com.musala.atmosphere.commons.ui.tree.AccessibilityElement;
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

    private static final String INTERRUPT_BACKGROUND_PROCESS_FORMAT = "kill -SIGINT $(%s)";

    private static final String GET_PID_FORMAT = "ps %s %s";

    private static final String GET_PID_PATTERN = "| grep -Eo [0-9]+ | grep -m 1 -Eo [0-9]+";

    private static final String SCREENSHOT_LOCAL_FILE_NAME = "local_screen.png";

    private static final String SCREEN_RECORD_COMPONENT_PATH = "/data/local/tmp";

    private static final String RECORDS_DIRECTORY_NAME = "AtmosphereScreenRecords";

    private static final String START_SCREENRECORD_SCRIPT_NAME = "start_screenrecord.sh";

    private static final String STOP_SCREENRECORD_SCRIPT_NAME = "stop_screenrecord.sh";

    private static final String START_SCREEN_RECORD_COMMAND = String.format("sh %s/%s",
                                                                            SCREEN_RECORD_COMPONENT_PATH,
                                                                            START_SCREENRECORD_SCRIPT_NAME);

    private static final String STOP_SCREEN_RECORD_COMMAND = String.format("sh %s/%s",
                                                                           SCREEN_RECORD_COMPONENT_PATH,
                                                                           STOP_SCREENRECORD_SCRIPT_NAME);

    private static final String SCREEN_RECORDS_REMOTE_PATH = String.format("%s/%s",
                                                                           SCREEN_RECORD_COMPONENT_PATH,
                                                                           RECORDS_DIRECTORY_NAME);

    private static final String SCREEN_RECORDS_LOCAL_DIR = System.getProperty("user.dir");

    private static final String MERGED_RECORDS_DIR_NAME = "ScreenRecords";

    private static final String RECORDS_FILENAMES_DELIMITER = "\r\n";

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd-HH-mm-ss";

    private static final String DEVICE_TYPE = "tablet";

    private static final String CLEAR_APP_DATA_COMMAND = "pm clear";

    private ExecutorService executor;

    private CompletionService<Boolean> pullFileCompletionService;

    protected final ServiceCommunicator serviceCommunicator;

    protected final UIAutomatorCommunicator automatorCommunicator;

    protected final FileTransferService transferService;

    protected final BackgroundShellCommandExecutor shellCommandExecutor;

    protected final IDevice wrappedDevice;

    private final ApkInstaller apkInstaller;

    private final ImeManager imeManager;

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
     * @throws RemoteException
     *         - required when implementing {@link UnicastRemoteObject}
     */
    public AbstractWrapDevice(IDevice deviceToWrap,
            ExecutorService executor,
            BackgroundShellCommandExecutor shellCommandExecutor,
            ServiceCommunicator serviceCommunicator,
            UIAutomatorCommunicator automatorCommunicator) throws RemoteException {
        // TODO: Use a dependency injection mechanism here.
        this.wrappedDevice = deviceToWrap;
        this.executor = executor;
        this.shellCommandExecutor = shellCommandExecutor;
        this.serviceCommunicator = serviceCommunicator;
        this.automatorCommunicator = automatorCommunicator;
        transferService = new FileTransferService(wrappedDevice);
        apkInstaller = new ApkInstaller(wrappedDevice);
        imeManager = new ImeManager(shellCommandExecutor);
        pullFileCompletionService = new ExecutorCompletionService<Boolean>(executor);
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
                returnValue = serviceCommunicator.getProcessRunning(args);
                break;
            case GET_RUNNING_TASK_IDS:
                returnValue = serviceCommunicator.getRunningTaskIds(args);
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
            case CHECK_ELEMENT_PRESENCE:
                returnValue = automatorCommunicator.isElementPresent((AccessibilityElement) args[0], (Boolean) args[1]);
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
                returnValue = automatorCommunicator.waitForExists((UiElementDescriptor) args[0], (Integer) args[1]);
                break;
            case WAIT_UNTIL_GONE:
                returnValue = automatorCommunicator.waitUntilGone((UiElementDescriptor) args[0], (Integer) args[1]);
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
                automatorCommunicator.clearField((UiElementDescriptor) args[0]);
                break;
            case ELEMENT_SWIPE:
                automatorCommunicator.swipeElement((UiElementDescriptor) args[0], (SwipeDirection) args[1]);
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
            case CLEAR_APP_DATA:
                clearApplicationData((String) args[0]);
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
                                                                      (UiElementDescriptor) args[1],
                                                                      (Integer) args[2],
                                                                      (Integer) args[3],
                                                                      (Boolean) args[4]);
                break;

            // Screen recording related
            case START_RECORDING:
                startScreenRecording();
                break;
            case STOP_RECORDING:
                stopScreenRecording();
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
     * Uninstalls an application by a given package name.
     *
     * @param args
     *        - containing the package of the process.
     * @throws CommandFailedException
     */
    private void uninstallApplication(String args) throws CommandFailedException {
        shellCommandExecutor.execute(UNINSTALL_APP_COMMAND + args);
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

    private void combineVideoFiles(String directoryPath) throws IOException {
        File file = new File(directoryPath);
        String[] fileNames = file.list();

        int filesCount = fileNames.length;
        Movie[] movies = new Movie[filesCount];

        for (int index = 0; index < filesCount; index++) {
            String currentFilePath = String.format("%s%s%s", directoryPath, File.separator, fileNames[index]);
            movies[index] = MovieCreator.build(currentFilePath);
        }

        List<Track> videoTracks = new LinkedList<Track>();

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
        RandomAccessFile randomAccessFile = new RandomAccessFile(String.format("%s%s%s%s%s_%s_screen_record.mp4",
                                                                               SCREEN_RECORDS_LOCAL_DIR,
                                                                               File.separator,
                                                                               MERGED_RECORDS_DIR_NAME,
                                                                               File.separator,
                                                                               timestamp,
                                                                               wrappedDevice.getSerialNumber()), "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        combinedMovieContainer.writeContainer(fileChannel);

        fileChannel.close();
        randomAccessFile.close();
    }

    private void startScreenRecording() {
        shellCommandExecutor.executeInBackground(START_SCREEN_RECORD_COMMAND);
    }

    private void stopScreenRecording() throws CommandFailedException {
        String output = shellCommandExecutor.execute(STOP_SCREEN_RECORD_COMMAND);

        if (output.trim().length() <= 0) {
            return;
        }

        String[] screenRecordFilenames = output.split(RECORDS_FILENAMES_DELIMITER);

        if (!screenRecordFilenames[0].equals("1.mp4")) {
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
            String currentRecordRemotePath = String.format("%s/%s", SCREEN_RECORDS_REMOTE_PATH, filename);
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
            combineVideoFiles(separatedVideosDirectoryPath);
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to merge video records pulled from device with serial number %s.",
                                       wrappedDevice.getSerialNumber()), e);
        }
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
    public void unbindWrapper() throws RemoteException {
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
