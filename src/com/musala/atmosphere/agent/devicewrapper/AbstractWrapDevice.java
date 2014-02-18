package com.musala.atmosphere.agent.devicewrapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.DevicePropertyStringConstants;
import com.musala.atmosphere.agent.devicewrapper.util.BackgroundShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.DeviceProfiler;
import com.musala.atmosphere.agent.devicewrapper.util.ForwardingPortFailedException;
import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorBridgeCommunicator;
import com.musala.atmosphere.agent.exception.OnDeviceComponentCommunicationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentInitializationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentStartingException;
import com.musala.atmosphere.agent.exception.OnDeviceServiceTerminationException;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.agent.util.DeviceScreenResolutionParser;
import com.musala.atmosphere.agent.util.MemoryUnitConverter;
import com.musala.atmosphere.commons.ConnectionType;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.PowerProperties;
import com.musala.atmosphere.commons.TelephonyInformation;
import com.musala.atmosphere.commons.beans.DeviceAcceleration;
import com.musala.atmosphere.commons.beans.DeviceOrientation;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.gesture.Gesture;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.util.Pair;

public abstract class AbstractWrapDevice extends UnicastRemoteObject implements IWrapDevice {
    /**
     * auto generated serialization id
     */
    private static final long serialVersionUID = -9122701818928360023L;

    private static final int COMMAND_EXECUTION_TIMEOUT = AgentPropertiesLoader.getCommandExecutionTimeout();

    // WARNING : do not change the remote folder unless you really know what you are doing.
    private static final String XMLDUMP_REMOTE_FILE_NAME = "/data/local/tmp/uidump.xml";

    private static final String XMLDUMP_LOCAL_FILE_NAME = "uidump.xml";

    private static final String SCREENSHOT_REMOTE_FILE_NAME = "/data/local/tmp/screen.png";

    private static final String SCREENSHOT_LOCAL_FILE_NAME = "screen.png";

    private static final String TEMP_APK_FILE_SUFFIX = ".apk";

    private File tempApkFile;

    private OutputStream tempApkFileOutputStream;

    protected ServiceCommunicator serviceCommunicator;

    protected UIAutomatorBridgeCommunicator uiAutomatorBridgeCommunicator;

    protected IDevice wrappedDevice;

    private final static Logger LOGGER = Logger.getLogger(AbstractWrapDevice.class.getCanonicalName());

    private Map<String, BackgroundShellCommandExecutor> backgroundShellCommandsMap = new HashMap<String, BackgroundShellCommandExecutor>();

    public AbstractWrapDevice(IDevice deviceToWrap) throws RemoteException {
        wrappedDevice = deviceToWrap;

        PortForwardingService forwardingService = new PortForwardingService(wrappedDevice);
        try {
            serviceCommunicator = new ServiceCommunicator(forwardingService, this);
            uiAutomatorBridgeCommunicator = new UIAutomatorBridgeCommunicator(forwardingService, this);
        }
        catch (ForwardingPortFailedException | OnDeviceComponentStartingException
                | OnDeviceComponentInitializationException e) {
            // TODO throw a new exception here when the preconditions are implemented.

            String errorMessage = String.format("Could not initialize communication to a on-device component for %s.",
                                                wrappedDevice.getSerialNumber());
            throw new OnDeviceComponentCommunicationException(errorMessage, e);
        }
    }

    @Override
    public Pair<Integer, Integer> getNetworkSpeed() throws RemoteException {
        // TODO get network speed for abstract devices
        return null;
    }

    @Override
    public long getFreeRAM() throws RemoteException, CommandFailedException {
        DeviceProfiler profiler = new DeviceProfiler(wrappedDevice);
        try {
            Map<String, Long> memUsage = profiler.getMeminfoDataset();
            long freeMemory = memUsage.get(DeviceProfiler.FREE_MEMORY_ID);
            return freeMemory;
        }
        catch (IOException | TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException e) {
            LOGGER.warn("Getting device '" + wrappedDevice.getSerialNumber() + "' memory usage resulted in exception.",
                        e);
            throw new CommandFailedException("Getting device memory usage resulted in exception.", e);
        }
    }

    @Override
    public String executeShellCommand(String command) throws RemoteException, CommandFailedException {
        String response = "";

        try {
            CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
            wrappedDevice.executeShellCommand(command, outputReceiver, COMMAND_EXECUTION_TIMEOUT);

            response = outputReceiver.getOutput();
        }
        catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            // Redirect the exception to the server
            throw new CommandFailedException("Shell command execution failed. See the enclosed exception for more information.",
                                             e);
        }

        return response;
    }

    /**
     * Executes a shell command in the background. Returns immediately. Usage should be limited to commands which do
     * will not return for a long time (because of thread related performance issues).
     * 
     * @param command
     *        - shell command that should be executed in the background.
     */
    public void executeBackgroundShellCommand(String command) {
        if (backgroundShellCommandsMap.containsKey(command)) {
            terminateBackgroundShellCommand(command);
        }

        BackgroundShellCommandExecutor commandExecutor = new BackgroundShellCommandExecutor(command, wrappedDevice);
        Thread executorThread = new Thread(commandExecutor);
        executorThread.start();

        backgroundShellCommandsMap.put(command, commandExecutor);
    }

    /**
     * Returns the execution exception that was thrown when a background shell command was executed (null if no
     * exception was thrown).
     * 
     * @param command
     *        - the executed command for which we want the thrown exception.
     * @return the exception itself.
     */
    public Exception getBackgroundShellCommandExecutionException(String command) {
        if (!backgroundShellCommandsMap.containsKey(command)) {
            throw new NoSuchElementException("No command '" + command + "' was found to be running or done executing.");
        }
        BackgroundShellCommandExecutor executor = backgroundShellCommandsMap.get(command);
        Exception executionException = executor.getExecutionException();
        return executionException;
    }

    /**
     * Terminates a background executing command.
     * 
     * @param command
     *        - the command to be terminated.
     */
    public void terminateBackgroundShellCommand(String command) {
        if (!backgroundShellCommandsMap.containsKey(command)) {
            throw new NoSuchElementException("No command '" + command + "' was found to be running or done executing.");
        }
        BackgroundShellCommandExecutor executor = backgroundShellCommandsMap.get(command);
        Thread executorThread = executor.getExecutorThread();
        if (executorThread.isAlive()) {
            executorThread.stop();
        }
        backgroundShellCommandsMap.remove(command);
    }

    @Override
    public List<String> executeSequenceOfShellCommands(List<String> commandsList)
        throws RemoteException,
            CommandFailedException {
        List<String> responses = new ArrayList<String>(commandsList.size());

        for (String commandForExecution : commandsList) {
            String responseFromCommandExecution = executeShellCommand(commandForExecution);
            responses.add(responseFromCommandExecution);
        }

        return responses;
    }

    @Override
    public DeviceInformation getDeviceInformation() throws RemoteException {
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
        }
        else {
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
        String pattern = "(\\w+):(\\s+)(\\d+\\w+)";

        try {
            ramMemoryString = executeShellCommand("cat /proc/meminfo | grep MemTotal");
        }
        catch (CommandFailedException e) {
            LOGGER.warn("Getting device RAM failed.", e);
        }

        ramMemoryString.replaceAll(pattern, "$3");

        deviceInformation.setRam(MemoryUnitConverter.convertMemoryToMB(ramMemoryString));

        // Resolution
        try {
            CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
            wrappedDevice.executeShellCommand("dumpsys window policy", outputReceiver);

            String shellResponse = outputReceiver.getOutput();
            Pair<Integer, Integer> screenResolution = DeviceScreenResolutionParser.parseScreenResolutionFromShell(shellResponse);
            deviceInformation.setResolution(screenResolution);

        }
        catch (ShellCommandUnresponsiveException | TimeoutException | AdbCommandRejectedException | IOException e) {
            // Shell command execution failed.
            LOGGER.error("Shell command execution failed.", e);
        }
        catch (StringIndexOutOfBoundsException e) {
            LOGGER.warn("Parsing shell response failed when attempting to get device screen size.");
        }

        return deviceInformation;
    }

    @Override
    public byte[] getScreenshot() throws RemoteException, CommandFailedException {
        String screenshotCommand = "screencap -p " + SCREENSHOT_REMOTE_FILE_NAME;
        executeShellCommand(screenshotCommand);

        try {
            wrappedDevice.pullFile(SCREENSHOT_REMOTE_FILE_NAME, SCREENSHOT_LOCAL_FILE_NAME);

            Path screenshotPath = Paths.get(SCREENSHOT_LOCAL_FILE_NAME);
            byte[] screenshotData = Files.readAllBytes(screenshotPath);
            return screenshotData;
        }
        catch (IOException | AdbCommandRejectedException | TimeoutException | SyncException e) {
            LOGGER.error("Screenshot fetching failed.", e);
            throw new CommandFailedException("Screenshot fetching failed.", e);
        }
    }

    @Override
    public void initAPKInstall() throws RemoteException, IOException {
        discardAPK();

        String tempApkFilePrefix = wrappedDevice.getSerialNumber();
        // replaces everything that is not a letter,
        // number or underscore with an underscore
        tempApkFilePrefix = tempApkFilePrefix.replaceAll("\\W+", "_");

        tempApkFile = File.createTempFile(tempApkFilePrefix, TEMP_APK_FILE_SUFFIX);
        tempApkFileOutputStream = new BufferedOutputStream(new FileOutputStream(tempApkFile));
    }

    @Override
    public void appendToAPK(byte[] bytes, int length) throws RemoteException, IOException {
        if (tempApkFile == null || tempApkFileOutputStream == null) {
            throw new IllegalStateException("Temp .apk file should be created (by calling initAPKInstall()) before any calls to appendToAPK() and buildAndInstallAPK().");
        }
        tempApkFileOutputStream.write(bytes, 0, length);
    }

    @Override
    public void buildAndInstallAPK() throws RemoteException, IOException, CommandFailedException {
        if (tempApkFile == null || tempApkFileOutputStream == null) {
            throw new IllegalStateException("Temp .apk file should be created (by calling initAPKInstall()) before any calls to appendToAPK() and buildAndInstallAPK().");
        }

        try {
            tempApkFileOutputStream.flush();
            tempApkFileOutputStream.close();
            tempApkFileOutputStream = null;
            String absolutePathToApk = tempApkFile.getAbsolutePath();

            String installResult = wrappedDevice.installPackage(absolutePathToApk, true /* force reinstall */);
            discardAPK();

            if (installResult != null) {
                LOGGER.error("PacketManager installation returned error code '" + installResult + "'.");
                throw new CommandFailedException("PacketManager installation returned error code '" + installResult
                        + "'.");
            }
        }
        catch (InstallException e) {
            LOGGER.error("Installing apk failed.", e);
            throw new CommandFailedException("Installing .apk file failed. See the enclosed exception for more information.",
                                             e);
        }
    }

    @Override
    public void discardAPK() throws RemoteException {
        if (tempApkFileOutputStream != null) {
            try {
                tempApkFileOutputStream.close();
            }
            catch (IOException e) {
                // closing failed, it was never functional. nothing to do here.
            }
            tempApkFileOutputStream = null;
        }

        if (tempApkFile != null) {
            if (tempApkFile.exists()) {
                tempApkFile.delete();
            }
            tempApkFile = null;
        }
    }

    @Override
    public String getUiXml() throws RemoteException, CommandFailedException {
        String dumpCommand = "uiautomator dump " + XMLDUMP_REMOTE_FILE_NAME;
        executeShellCommand(dumpCommand);

        try {
            wrappedDevice.pullFile(XMLDUMP_REMOTE_FILE_NAME, XMLDUMP_LOCAL_FILE_NAME);

            File xmlDumpFile = new File(XMLDUMP_LOCAL_FILE_NAME);
            Scanner xmlDumpFileScanner = new Scanner(xmlDumpFile, "UTF-8");
            xmlDumpFileScanner.useDelimiter("\\Z");
            String uiDumpContents = xmlDumpFileScanner.next();
            xmlDumpFileScanner.close();
            return uiDumpContents;
        }
        catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
            LOGGER.error("UI dump failed.", e);
            throw new CommandFailedException("UI dump failed. See the enclosed exception for more information.", e);
        }
    }

    @Override
    public int getNetworkLatency() throws RemoteException {
        // TODO implement get network latency
        return 0;
    }

    @Override
    public PowerProperties getPowerProperties() throws RemoteException, CommandFailedException {
        try {
            return serviceCommunicator.getPowerProperties();
        }
        catch (CommandFailedException e) {
            LOGGER.fatal("Getting power related environment information failed.", e);
            throw e;
        }

    }

    @Override
    public DeviceOrientation getDeviceOrientation() throws RemoteException, CommandFailedException {
        try {
            DeviceOrientation result = serviceCommunicator.getDeviceOrientation();
            return result;
        }
        catch (CommandFailedException e) {
            LOGGER.fatal("Getting device orientation failed.", e);
            throw e;
        }
    }

    @Override
    public DeviceAcceleration getDeviceAcceleration() throws RemoteException, CommandFailedException {
        try {
            return serviceCommunicator.getAcceleration();
        }
        catch (CommandFailedException e) {
            LOGGER.fatal("Getting acceleation failed.", e);
            throw e;
        }

    }

    @Override
    public ConnectionType getConnectionType() throws RemoteException, CommandFailedException {
        try {
            return serviceCommunicator.getConnectionType();
        }
        catch (CommandFailedException e) {
            LOGGER.fatal("Getting connection type failed.", e);
            throw e;
        }
    }

    @Override
    public void setWiFi(boolean state) throws CommandFailedException, RemoteException {
        try {
            serviceCommunicator.setWiFi(state);
        }
        catch (CommandFailedException e) {
            LOGGER.fatal("Setting WiFi failed.", e);
            throw e;
        }
    }

    @Override
    public void executeGesture(Gesture gesture) throws CommandFailedException, RemoteException {
        uiAutomatorBridgeCommunicator.playGesture(gesture);
    }

    @Override
    public TelephonyInformation getTelephonyInformation() throws CommandFailedException, RemoteException {
        try {
            return serviceCommunicator.getTelephonyInformation();
        }
        catch (CommandFailedException e) {
            LOGGER.fatal("Getting telephony information failed.", e);
            throw e;
        }
    }

    @Override
    protected void finalize() {
        for (BackgroundShellCommandExecutor commandExecutor : backgroundShellCommandsMap.values()) {
            // we cannot modify the map here!
            Thread executorThread = commandExecutor.getExecutorThread();
            executorThread.stop();
        }

        try {
            serviceCommunicator.stopAtmosphereService();
        }
        catch (OnDeviceServiceTerminationException e) {
            String loggerMessage = String.format("Stopping ATMOSPHERE service failed for %s.",
                                                 wrappedDevice.getSerialNumber());
            LOGGER.warn(loggerMessage, e);
        }
    }
}
