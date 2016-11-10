package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.util.concurrent.Callable;

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
public class BackgroundPullFileTask implements Callable<Boolean> {
    private String remoteFilePath;

    private String localFilePath;

    private final IDevice wrappedDevice;

    /**
     * Creates new background task for pulling remote files from the device.
     *
     * @param wrappedDevice
     *        - device wrapper that will be used for the execution
     * @param remoteFilePath
     *        - path to the file on the device
     * @param localFilePath
     *        - local path where the file will be pulled
     */
    public BackgroundPullFileTask(IDevice wrappedDevice, String remoteFilePath, String localFilePath) {
        this.wrappedDevice = wrappedDevice;
        this.remoteFilePath = remoteFilePath;
        this.localFilePath = localFilePath;
    }

    @Override
    public Boolean call() {
        try {
            wrappedDevice.pullFile(remoteFilePath, localFilePath);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | SyncException e) {
            return false;
        }

        return true;
    }

}
