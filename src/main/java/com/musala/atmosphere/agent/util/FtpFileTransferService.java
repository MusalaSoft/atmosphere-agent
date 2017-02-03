package com.musala.atmosphere.agent.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.AbstractWrapDevice;

/**
 * A service responsible for all file transfers to the FTP server. The service will add all finished screen records to a
 * persistent queue(a text file with list of queued screen record names) and will trigger a transfer if the FTP
 * connection manager is available for a new job.
 *
 * @author dimcho.nedev
 *
 */
public class FtpFileTransferService implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(AbstractWrapDevice.class.getCanonicalName());

    private File pendingTransferData;

    private List<String> pendingTransferList;

    private FtpConnectionManager connectionManager;

    public FtpFileTransferService(String pendingTransfersFileName, FtpConnectionManager connectionManager)
        throws IOException {
        this.pendingTransferData = new File(pendingTransfersFileName);

        if (!pendingTransferData.exists()) {
            pendingTransferData.createNewFile();
        }

        this.pendingTransferList = new ArrayList<>();
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        try {
            if (connectionManager.isAvailableForTransfer() && !isQueueEmpty()) {
                connectionManager.reconnect();

                // retrieves and removes the head of this queue
                String filePath = getNextTransferFilePath();
                File fileToTransfer = new File(filePath);
                String remoteFileName = fileToTransfer.getName();

                String username = getUsername(remoteFileName);

                if (username != null) {
                    boolean isCreated = connectionManager.createDirectoryIfNotExists(username);
                    if (isCreated) {
                        // System.getProperty("file.separator") does not work properly when the Agent is on Windows but
                        // the FTP runs on a Linux system
                        remoteFileName = String.format("%s/%s", username, remoteFileName);
                        connectionManager.transferData(fileToTransfer, remoteFileName);
                    } else {
                        // TODO: Find a way to transfer an error message (if occurs) to the client
                        LOGGER.error("Failed to transfer a data to a remote directory. Can't create a remote directory.");
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to read from the \"%s\" file", pendingTransferData.getName()), e);
        }
    }

    private String getUsername(String filePath) {
        String fileRegexFormat = "(.*)_[0-9a-z-]+_[0-9a-zA-Z]+_screen_record.mp4";
        Pattern pattern = Pattern.compile(fileRegexFormat);
        Matcher matcher = pattern.matcher(filePath);
        String username = null;

        if (matcher.find()) {
            username = matcher.group(1);
        }

        return username;
    }

    /**
     * Logout and disconnects from the FTP server.
     */
    public void stop() {
        connectionManager.disconnect();
    }

    /**
     * Add a transfer task to a text file.
     *
     * @param pathToFile
     *        - path to the file that will be transfered
     * @throws IOException
     *         thrown when failed to read/write from/to the queue file
     */
    public synchronized void addTransferTask(String pathToFile) throws IOException {
        pendingTransferList = FileUtils.readLines(pendingTransferData);
        pendingTransferList.add(pathToFile);
        FileUtils.writeLines(pendingTransferData, pendingTransferList);
    }

    private synchronized String getNextTransferFilePath() throws IOException {
        pendingTransferList = FileUtils.readLines(pendingTransferData);
        String filePath = pendingTransferList.remove(0);
        FileUtils.writeLines(pendingTransferData, pendingTransferList);

        return filePath;
    }

    private synchronized boolean isQueueEmpty() throws IOException {
        pendingTransferList = FileUtils.readLines(pendingTransferData);
        return pendingTransferList.isEmpty();
    }

}
