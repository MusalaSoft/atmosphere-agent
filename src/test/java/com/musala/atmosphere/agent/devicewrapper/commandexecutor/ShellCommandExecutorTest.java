package com.musala.atmosphere.agent.devicewrapper.commandexecutor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.Client;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.FileListingService;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.ScreenRecorderOptions;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.log.LogReceiver;
import com.android.sdklib.AndroidVersion;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;

public class ShellCommandExecutorTest {

    private static final int DEFAULT_TIMEOUT = AgentPropertiesLoader.getCommandExecutionTimeout();

    private static final int TIMEOUT_GREATER_THAN_DEFAULT = AgentPropertiesLoader.getCommandExecutionTimeout() + 10000;

    private static final int TIMEOUT_LESS_THAN_DEFAULT = AgentPropertiesLoader.getCommandExecutionTimeout() - 10000;

    private static final String COMMAND = "logcat";

    private ShellCommandExecutor shellCommandExecutor;

    private IDevice device;

    private CollectingOutputReceiver outputReceiver;

    private class MockedDevice implements IDevice {
        private int expectedCommandExecutionTimeout;

        public MockedDevice(int expectedCommandExecutionTimeout) {
            this.expectedCommandExecutionTimeout = expectedCommandExecutionTimeout;
        }

        @Override
        public boolean arePropertiesSet() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void createForward(int arg0, int arg1) throws TimeoutException, AdbCommandRejectedException, IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void createForward(int arg0, String arg1, DeviceUnixSocketNamespace arg2)
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void executeShellCommand(String arg0, IShellOutputReceiver arg1)
            throws TimeoutException,
                AdbCommandRejectedException,
                ShellCommandUnresponsiveException,
                IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void executeShellCommand(String arg0, IShellOutputReceiver arg1, int timeout)
            throws TimeoutException,
                AdbCommandRejectedException,
                ShellCommandUnresponsiveException,
                IOException {
            assertEquals("Failed, timeout is different from the expected command execution timeout.",
                         timeout,
                         expectedCommandExecutionTimeout);

        }

        @Override
        public String getAvdName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Integer getBatteryLevel()
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException,
                ShellCommandUnresponsiveException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Integer getBatteryLevel(long arg0)
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException,
                ShellCommandUnresponsiveException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Client getClient(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getClientName(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Client[] getClients() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public FileListingService getFileListingService() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getMountPoint(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map<String, String> getProperties() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getProperty(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getPropertyCacheOrSync(String arg0)
            throws TimeoutException,
                AdbCommandRejectedException,
                ShellCommandUnresponsiveException,
                IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getPropertyCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getPropertySync(String arg0)
            throws TimeoutException,
                AdbCommandRejectedException,
                ShellCommandUnresponsiveException,
                IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public RawImage getScreenshot() throws TimeoutException, AdbCommandRejectedException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getSerialNumber() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DeviceState getState() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public SyncService getSyncService() throws TimeoutException, AdbCommandRejectedException, IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean hasClients() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isBootLoader() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isEmulator() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isOffline() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isOnline() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void pullFile(String arg0, String arg1)
            throws IOException,
                AdbCommandRejectedException,
                TimeoutException,
                SyncException {
            // TODO Auto-generated method stub

        }

        @Override
        public void pushFile(String arg0, String arg1)
            throws IOException,
                AdbCommandRejectedException,
                TimeoutException,
                SyncException {
            // TODO Auto-generated method stub

        }

        @Override
        public void reboot(String arg0) throws TimeoutException, AdbCommandRejectedException, IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void removeForward(int arg0, int arg1) throws TimeoutException, AdbCommandRejectedException, IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void removeForward(int arg0, String arg1, DeviceUnixSocketNamespace arg2)
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void removeRemotePackage(String arg0) throws InstallException {
            // TODO Auto-generated method stub

        }

        @Override
        public void runEventLogService(LogReceiver arg0)
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public void runLogService(String arg0, LogReceiver arg1)
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public String syncPackageToDevice(String arg0)
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException,
                SyncException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String uninstallPackage(String arg0) throws InstallException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void executeShellCommand(String command,
                                        IShellOutputReceiver receiver,
                                        long maxTimeToOutputResponse,
                                        TimeUnit maxTimeUnits)
            throws TimeoutException,
                AdbCommandRejectedException,
                ShellCommandUnresponsiveException,
                IOException {
            // TODO Auto-generated method stub

        }

        @Override
        public Future<String> getSystemProperty(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean supportsFeature(Feature feature) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean supportsFeature(HardwareFeature feature) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public RawImage getScreenshot(long timeout, TimeUnit unit)
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void startScreenRecorder(String remoteFilePath,
                                        ScreenRecorderOptions options,
                                        IShellOutputReceiver receiver)
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException,
                ShellCommandUnresponsiveException {
            // TODO Auto-generated method stub

        }

        @Override
        public void installPackage(String packageFilePath, boolean reinstall, String... extraArgs)
            throws InstallException {
            // TODO Auto-generated method stub

        }

        @Override
        public void installPackages(List<File> apks,
                                    boolean reinstall,
                                    List<String> installOptions,
                                    long timeout,
                                    TimeUnit timeoutUnit)
            throws InstallException {
            // TODO Auto-generated method stub

        }

        @Override
        public void installRemotePackage(String remoteFilePath, boolean reinstall, String... extraArgs)
            throws InstallException {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean root()
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException,
                ShellCommandUnresponsiveException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isRoot()
            throws TimeoutException,
                AdbCommandRejectedException,
                IOException,
                ShellCommandUnresponsiveException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Future<Integer> getBattery() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Future<Integer> getBattery(long freshnessTime, TimeUnit timeUnit) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<String> getAbis() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getDensity() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getLanguage() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRegion() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AndroidVersion getVersion() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Test
    public void testSetCommandExecutionTimeoutGraterThanDefault() throws Exception {
        device = new MockedDevice(TIMEOUT_GREATER_THAN_DEFAULT);
        shellCommandExecutor = new ShellCommandExecutor(device);
        shellCommandExecutor.execute(COMMAND, TIMEOUT_GREATER_THAN_DEFAULT);
    }

    @Test
    public void testSetCommandExecutionTimeoutLessThanDefault() throws Exception {
        device = new MockedDevice(DEFAULT_TIMEOUT);
        shellCommandExecutor = new ShellCommandExecutor(device);
        shellCommandExecutor.execute(COMMAND, TIMEOUT_LESS_THAN_DEFAULT);
    }
}
