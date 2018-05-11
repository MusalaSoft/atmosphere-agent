// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

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
