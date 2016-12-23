package com.musala.atmosphere.agent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

/**
 * Manages the connection and the data transfers to the FTP server.
 *
 * @author dimcho.nedev
 *
 */
public class FtpConnectionManager {
    private static final Logger LOGGER = Logger.getLogger(FtpConnectionManager.class.getCanonicalName());

    private FTPClient ftpClient;

    private static FtpConnectionManager ftpConnectionManager;

    private static String ftpServerName = FtpServerPropertiesLoader.getFtpName();;

    private static String username = FtpServerPropertiesLoader.getUsername();

    private static String password = FtpServerPropertiesLoader.getPassword();

    private static int port = FtpServerPropertiesLoader.getFtpPort();

    private FtpConnectionManager() {
        this.ftpClient = new FTPClient();
    }

    public static synchronized FtpConnectionManager getInstance() {
        if (ftpConnectionManager == null) {
            ftpConnectionManager = new FtpConnectionManager();
        }
        return ftpConnectionManager;
    }

    /**
     * Connects the client to the FTP Server if is not already connected.
     */
    public void connectToFtpServer() {
        try {
            ftpClient.connect(ftpServerName, port);
            ftpClient.login(username, password);

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                throw new IOException("FTP server refused connection.");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to connect to an FTP Server", e);
        }
    }

    /**
     * Disconnects and logout the FTP Client.
     */
    public void disconnect() {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (FTPConnectionClosedException e) {

        } catch (IOException e) {
            LOGGER.error("Faild to logout/disconnect the FTP client.", e);
        }
    }

    /**
     * Transfers data from the client(the current Agent) to the FTP server.
     *
     * @param fileToTransfer
     *        the file to transfer
     * @param remoteFileName
     *        the remote file name
     * @return <code>true</code> if the data transfer is successful, otherwise returns <code>false</code>
     */
    public boolean transferData(File fileToTransfer, String remoteFileName) {
        boolean isSuccessful = true;

        try (InputStream inputStream = new FileInputStream(fileToTransfer)) {
            // Connect to the FTP if the socket connection is closed from the server side due a timeout.
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                LOGGER.info("Reconnecting to an FTP server");
                connectToFtpServer();
            }

            long startTime = System.currentTimeMillis();

            ftpClient.storeFile(remoteFileName, inputStream);

            long stopTime = System.currentTimeMillis();
            float transferDuration = ((stopTime - startTime))/1000.f;

            LOGGER.info("File transfer finished for " + transferDuration + " seconds.");
        } catch (FTPConnectionClosedException e) {
            // TODO: Find why sometimes this exception is thrown but the transfer is successful
        } catch (IOException e) {
            isSuccessful = false;
            LOGGER.error("File transfer failed.", e);
            e.printStackTrace();
        }

        return isSuccessful;
    }

}
