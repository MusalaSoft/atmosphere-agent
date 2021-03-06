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

import java.io.File;
import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.commons.ad.FileTransferConstants;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Class responsible for pushing and pulling files to and from an {@link IDevice} instance.
 *
 * @author georgi.gaydarov
 *
 */
public class FileTransferService {
    // WARNING : do not change the remote folder unless you really know what you are doing.

    private IDevice device;

    public FileTransferService(IDevice device) {
        this.device = device;
    }

    /**
     * Uploads a file to the device. The file will be stored at the temporary files folder under the same name.
     *
     * @param localFileName
     *        - the file to upload.
     * @return the absolute remote path of the uploaded file.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public String pushFile(String localFileName) throws CommandFailedException {
        String isolatedFileName = new File(localFileName).getName();
        String remoteFileName = FileTransferConstants.DEVICE_TMP_PATH + isolatedFileName;
        try {
            device.pushFile(localFileName, remoteFileName);
        } catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
            throw new CommandFailedException("Pushing file failed.", e);
        }
        return remoteFileName;
    }

    /**
     * Download a file from the temporary files of the device.
     *
     * @param remoteFileName
     *        - the name of the file to download
     * @param localFileName
     *        - the local path to the destination file
     * @throws CommandFailedException
     *         thrown when pulling file failed
     */
    public void pullFile(String remoteFileName, String localFileName) throws CommandFailedException {
        String remoteFileNameWithPath = FileTransferConstants.DEVICE_TMP_PATH + remoteFileName;

        try {
            device.pullFile(remoteFileNameWithPath, localFileName);
        } catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
            String message = String.format("Pulling remote file %s failed.", remoteFileNameWithPath);
            throw new CommandFailedException(message);
        }
    }
}
