package com.musala.atmosphere.agent.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            if (connectionManager.isAvailableForTransfer() && !isEmptyQueue()) {
                // retrieves and removes the head of this queue
                String filePath = getNextTransferFilePath();
                File fileToTransfer = new File(filePath);
                connectionManager.transferData(fileToTransfer);
                // TODO: Find a way to transfer an error message (if occurs) to the client
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to read from the \"%s\" file", pendingTransferData.getName()), e);
        }
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

    private synchronized boolean isEmptyQueue() throws IOException {
        pendingTransferList = FileUtils.readLines(pendingTransferData);
        return pendingTransferList.isEmpty();
    }

}
