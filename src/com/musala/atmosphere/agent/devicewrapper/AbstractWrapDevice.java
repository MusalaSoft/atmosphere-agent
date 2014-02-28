package com.musala.atmosphere.agent.devicewrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.musala.atmosphere.agent.devicewrapper.util.ForwardingPortFailedException;
import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.devicewrapper.util.PreconditionsManager;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorBridgeCommunicator;
import com.musala.atmosphere.agent.exception.OnDeviceComponentCommunicationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentInitializationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentStartingException;
import com.musala.atmosphere.agent.exception.OnDeviceServiceTerminationException;
import com.musala.atmosphere.agent.util.DeviceScreenResolutionParser;
import com.musala.atmosphere.agent.util.MemoryUnitConverter;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.PowerProperties;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.beans.DeviceAcceleration;
import com.musala.atmosphere.commons.beans.DeviceOrientation;
import com.musala.atmosphere.commons.beans.MobileDataState;
import com.musala.atmosphere.commons.beans.PhoneNumber;
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

    // WARNING : do not change the remote folder unless you really know what you are doing.
    private static final String XMLDUMP_REMOTE_FILE_NAME = "/data/local/tmp/uidump.xml";

    private static final String XMLDUMP_COMMAND = "uiautomator dump " + XMLDUMP_REMOTE_FILE_NAME;

    private static final String XMLDUMP_LOCAL_FILE_NAME = "uidump.xml";

    private static final String SCREENSHOT_REMOTE_FILE_NAME = "/data/local/tmp/screen.png";

    private static final String SCREENSHOT_COMMAND = "screencap -p " + SCREENSHOT_REMOTE_FILE_NAME;

    private static final String SCREENSHOT_LOCAL_FILE_NAME = "screen.png";

    protected final ServiceCommunicator serviceCommunicator;

    protected final UIAutomatorBridgeCommunicator uiAutomatorBridgeCommunicator;

    private static final String GET_RAM_MEMORY_COMMAND = "cat /proc/meminfo | grep MemTotal";

    protected final ShellCommandExecutor shellCommandExecutor;

    protected final IDevice wrappedDevice;

    private final PreconditionsManager preconditionsManager;

    private final ApkInstaller apkInstaller;

    public AbstractWrapDevice(IDevice deviceToWrap) throws RemoteException {
        wrappedDevice = deviceToWrap;

        shellCommandExecutor = new ShellCommandExecutor(wrappedDevice);
        apkInstaller = new ApkInstaller(wrappedDevice);

        preconditionsManager = new PreconditionsManager(wrappedDevice);
        preconditionsManager.manageOnDeviceComponents();

        PortForwardingService forwardingService = new PortForwardingService(wrappedDevice);
        try {
            serviceCommunicator = new ServiceCommunicator(forwardingService, this);
            uiAutomatorBridgeCommunicator = new UIAutomatorBridgeCommunicator(forwardingService, this);
        } catch (ForwardingPortFailedException | OnDeviceComponentStartingException
                | OnDeviceComponentInitializationException e) {
            // TODO throw a new exception here when the preconditions are implemented.

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
                apkInstaller.buildAndInstallAPK();
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
            case GET_FREE_RAM:
                returnValue = getFreeRAM();
                break;
            case GET_MOBILE_DATA_STATE:
                returnValue = getMobileDataState();
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

            // Misc functionalities
            case PLAY_GESTURE:
                uiAutomatorBridgeCommunicator.playGesture((Gesture) args[0]);
                break;
            case CLEAR_FIELD:
                uiAutomatorBridgeCommunicator.clearField((UiElementDescriptor) args[0]);
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

        // If the device will not give us it's valid properties, return the structure with the fallback values set.
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

        // RAM
        String ramMemoryString = DeviceInformation.FALLBACK_RAM_AMOUNT.toString();
        String ramMemoryPattern = "(\\w+):(\\s+)(\\d+\\w+)";

        try {
            ramMemoryString = shellCommandExecutor.execute(GET_RAM_MEMORY_COMMAND);
        } catch (CommandFailedException e) {
            LOGGER.warn("Getting device RAM failed.", e);
        }

        String extractedRamMemoryString = ramMemoryString.replaceAll(ramMemoryPattern, "$3");

        deviceInformation.setRam(MemoryUnitConverter.convertMemoryToMB(extractedRamMemoryString));

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

        try {
            wrappedDevice.pullFile(SCREENSHOT_REMOTE_FILE_NAME, SCREENSHOT_LOCAL_FILE_NAME);

            Path screenshotPath = Paths.get(SCREENSHOT_LOCAL_FILE_NAME);
            byte[] screenshotData = Files.readAllBytes(screenshotPath);
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

        shellCommandExecutor.execute(XMLDUMP_COMMAND);

        try {
            wrappedDevice.pullFile(XMLDUMP_REMOTE_FILE_NAME, XMLDUMP_LOCAL_FILE_NAME);

            File xmlDumpFile = new File(XMLDUMP_LOCAL_FILE_NAME);
            Scanner xmlDumpFileScanner = new Scanner(xmlDumpFile, "UTF-8");
            xmlDumpFileScanner.useDelimiter("\\Z");
            String uiDumpContents = xmlDumpFileScanner.next();
            xmlDumpFileScanner.close();
            return uiDumpContents;
        } catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
            LOGGER.error("UI dump failed.", e);
            throw new CommandFailedException("UI dump failed. See the enclosed exception for more information.", e);
        }
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

    abstract protected void setMobileDataState(MobileDataState state) throws CommandFailedException;

    abstract protected MobileDataState getMobileDataState() throws CommandFailedException;

    abstract protected void setNetworkSpeed(Pair<Integer, Integer> speeds) throws CommandFailedException;
}
