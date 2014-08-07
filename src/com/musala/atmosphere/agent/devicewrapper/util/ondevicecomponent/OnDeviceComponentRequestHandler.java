package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.service.ServiceConstants;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Handles requests sent to an ATMOSPHERE on-device component.
 * 
 * @author yordan.petrov
 * 
 */
public abstract class OnDeviceComponentRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(OnDeviceComponentRequestHandler.class);

    private static final int REQUEST_RETRY_TIMEOUT = 1000;

    private static final String HOST_NAME = "localhost";

    private static final int CONNECTION_RETRY_LIMIT = AgentPropertiesLoader.getOnDeviceComponentConnectionRetryLimit();

    private Socket socketClient;

    private OutputStream socketClientOutputStream;

    private InputStream socketClientInputStream;

    private final int socketPort;

    protected PortForwardingService portForwardingService;

    public OnDeviceComponentRequestHandler(PortForwardingService portForwarder, int socketPort) {
        portForwardingService = portForwarder;
        this.socketPort = socketPort;

        validateRemoteServer();
    }

    /**
     * Forwards a local port to the port used by the ATMOSPHERE on-device component socket server.
     */
    protected abstract void forwardComponentPort();

    /**
     * Validates that the remote server is an ATMOSPHERE on-device component socket server.
     */
    protected abstract void validateRemoteServer();

    /**
     * Establishes connection to an ATMOSPHERE on-device component.
     * 
     * @throws IOException
     */
    private void connect() throws IOException {
        forwardComponentPort();

        int retries = 0;

        while (true) {
            try {
                closeClientConnection();
                socketClient = new Socket(HOST_NAME, socketPort);
                socketClientInputStream = socketClient.getInputStream();
                socketClientOutputStream = socketClient.getOutputStream();
                break;
            } catch (IOException e) {
                disconnect();

                if (retries < CONNECTION_RETRY_LIMIT) {
                    retries++;

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        // Should not get here.
                        e1.printStackTrace();
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Closes the socket connection with client, if such is present.
     * 
     * @throws IOException
     *         if an I/O error occurs when closing this socket.
     */
    private void closeClientConnection() throws IOException {
        if (socketClient != null) {
            socketClient.close();
        }
    }

    /**
     * Sends a {@link ServiceRequest} request to an ATMOSPHERE on-device component and returns the response.
     * 
     * @param socketServerRequest
     *        - request that will be send to the ATMOSPHERE on-device component.
     * @return the response from the ATMOSPHERE service.
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws UnknownHostException
     * @throws CommandFailedException
     */
    public Object request(Request socketServerRequest)
        throws ClassNotFoundException,
            UnknownHostException,
            IOException,
            CommandFailedException {

        Object readRequest = ServiceConstants.UNRECOGNIZED_SERVICE_REQUEST;

        for (int i = 0; i < CONNECTION_RETRY_LIMIT; i++) {
            connect();

            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socketClientOutputStream);
                objectOutputStream.flush();
                objectOutputStream.writeObject(socketServerRequest);
                objectOutputStream.flush();

                ObjectInputStream objectInputStream = new ObjectInputStream(socketClientInputStream);
                Object inputObject = objectInputStream.readObject();

                objectInputStream.close();
                objectOutputStream.close();

                readRequest = inputObject;
                break;
            } catch (SocketException e) {
                LOGGER.error(e);
            }

            disconnect();
            waitBeforeNextOperation(REQUEST_RETRY_TIMEOUT);
        }

        if (readRequest != ServiceConstants.UNRECOGNIZED_SERVICE_REQUEST) {
            return readRequest;
        }

        final String fatalMessage = String.format("Could not establish stable connection with device after %d attempts.",
                                                  CONNECTION_RETRY_LIMIT);
        LOGGER.fatal(fatalMessage);
        throw new CommandFailedException(fatalMessage);
    }

    /**
     * Sleeps the invoker thread for the given time.
     * 
     * @param timeout
     *        - time in ms for which the thread should stay inactive.
     */
    private void waitBeforeNextOperation(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Disconnects from the ATMOSPHERE on-device component if a connection is present.
     * 
     * @throws IOException
     */
    private void disconnect() throws IOException {
        if (socketClientInputStream != null) {
            socketClientInputStream.close();
            socketClientInputStream = null;
        }
        if (socketClientOutputStream != null) {
            socketClientOutputStream.close();
            socketClientOutputStream = null;
        }
        if (socketClient != null) {
            socketClient.close();
            socketClient = null;
        }
    }
}
