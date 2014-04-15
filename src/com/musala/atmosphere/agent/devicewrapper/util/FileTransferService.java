package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.File;
import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Class responsible for pushing and pulling files to and from an {@link IDevice} instance.
 * 
 * @author georgi.gaydarov
 * 
 */
public class FileTransferService {
    // WARNING : do not change the remote folder unless you really know what you are doing.
    private static final String REMOTE_FILE_PATH = "/data/local/tmp/";

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
        String remoteFileName = REMOTE_FILE_PATH + isolatedFileName;
        try {
            device.pushFile(localFileName, remoteFileName);
        } catch (SyncException | IOException | AdbCommandRejectedException | TimeoutException e) {
            throw new CommandFailedException("Pushing file failed.", e);
        }
        return remoteFileName;
    }
}
