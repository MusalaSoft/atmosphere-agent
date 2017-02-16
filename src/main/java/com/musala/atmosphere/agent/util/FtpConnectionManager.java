package com.musala.atmosphere.agent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
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

    private static String ftpServerName = FtpServerPropertiesLoader.getFtpName();

    private static String username = FtpServerPropertiesLoader.getUsername();

    private static String password = FtpServerPropertiesLoader.getPassword();

    private static int port = FtpServerPropertiesLoader.getFtpPort();

    private static boolean isAvailableForTransfer;

    private static String ftpHomeDirectory = FtpServerPropertiesLoader.getFtpHomeDirectory();

    public FtpConnectionManager(boolean isSecured) {
        this.ftpClient = isSecured ? new FTPSClient() : new FTPClient();
        isAvailableForTransfer = true;
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
            ftpClient.changeWorkingDirectory(ftpHomeDirectory);

            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                throw new IOException("FTP server refused connection.");
            }

            LOGGER.info(String.format("Connected to an FTP Server with IP (%s:%s).", ftpServerName, port));
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to connect to an FTP Server with IP (%s:%s).", ftpServerName, port), e);
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
     * @return <code>true</code> if the data transfer is successful, otherwise returns <code>false</code>
     */
    public boolean transferData(File fileToTransfer, String remoteFileName) {
        isAvailableForTransfer = false;
        boolean isSuccessful = true;

        try (InputStream inputStream = new FileInputStream(fileToTransfer)) {
            ftpClient.storeFile(remoteFileName, inputStream);
        } catch (FTPConnectionClosedException e) {
            // TODO: Find why sometimes this exception is thrown but the transfer is successful
        } catch (IOException e) {
            isSuccessful = false;
            LOGGER.error("File transfer FAILED.", e);
        }

        if (isSuccessful) {
            LOGGER.info("File transfer finished SUCCESSFULLY.");
        }

        isAvailableForTransfer = true;

        return isSuccessful;
    }

    /**
     * Creates a remote directory on the FTP server with a given name if not exists.
     *
     * @param directoryName
     *        - the name of the remote directory on the FTP server that will be created
     * @return <code>true</code> if the creation is successful or the directory already exists, otherwise returns
     *         <code>false</code>
     */
    public boolean createDirectoryIfNotExists(String directoryName) {
        if (isRemoteDirectoryExists(directoryName)) {
            return true;
        }

        boolean isSuccessful = false;
        try {
            isSuccessful = ftpClient.makeDirectory(directoryName);
        } catch (IOException e) {
            LOGGER.error("Failed to create a directory on the FTP server. Directory name: " + directoryName, e);
        }

        return isSuccessful;
    }

    private boolean isRemoteDirectoryExists(String dirName) {
        boolean exist = false;
        try {
            exist = ftpClient.changeWorkingDirectory(dirName);

            if (exist) {
                ftpClient.changeToParentDirectory();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return exist;
    }

    /**
     * Connect to the FTP if the socket connection is closed from the server side due an idle timeout.
     *
     */
    public void reconnect() {
        String reconnectMessage = "Reconnecting to the FTP server...";

        try {
            boolean ready = ftpClient.sendNoOp();
            if (!ready) {
                LOGGER.info(reconnectMessage);
                connectToFtpServer();
            }
        } catch (FTPConnectionClosedException e) {
            disconnect();
            LOGGER.info(reconnectMessage);
            connectToFtpServer();
        } catch (IOException e) {
            LOGGER.error("Cannot wake up the FTP connection.", e);
        }
    }

    /**
     * Returns whether the FTP connection manager is available for transfer
     *
     * @return whether the FTP connection manager is available for transfer
     */
    public boolean isAvailableForTransfer() {
        return isAvailableForTransfer;
    }

}
