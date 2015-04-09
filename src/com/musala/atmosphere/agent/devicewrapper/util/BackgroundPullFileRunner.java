package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;

/**
 * A class used for pulling files from the device in background.
 * 
 * @author yavor.stankov
 *
 */
public class BackgroundPullFileRunner implements Runnable {
    private Exception onExecutionException;

    private Thread executorThread;

    private String remoteFilePath;

    private String localFilePath;

    private final IDevice wrappedDevice;

    /**
     * 
     * @param command
     *        - shell command to be executed.
     * @param wrappedDevice
     *        - device wrapper that will be used for the execution.
     */
    public BackgroundPullFileRunner(IDevice wrappedDevice, String remoteFilePath, String localFilePath) {
        this.wrappedDevice = wrappedDevice;
        this.remoteFilePath = remoteFilePath;
        this.localFilePath = localFilePath;
    }

    @Override
    public void run() {
        executorThread = Thread.currentThread();
        try {
            wrappedDevice.pullFile(remoteFilePath, localFilePath);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | SyncException e) {

        }
    }

    /**
     * 
     * @return <code>true</code> if an exception was thrown when the passed shell command was executed,
     *         <code>false</code> otherwise.
     */
    public boolean isExecutionExceptionThrown() {
        return onExecutionException != null;
    }

    /**
     * 
     * @return the exception which was thrown when the passed shell command was executed (<code>null</code> if no
     *         exception was thrown).
     */
    public Exception getExecutionException() {
        return onExecutionException;
    }

    /**
     * 
     * @return the {@link Thread} object that is responsible for the shell command execution.
     */
    public Thread getExecutorThread() {
        return executorThread;
    }

}
