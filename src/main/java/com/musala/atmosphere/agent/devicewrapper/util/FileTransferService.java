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
     * @param fileName
     *        - the file to upload.
     * @return the absolute remote path of the uploaded file.
     * @throws CommandFailedException
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
