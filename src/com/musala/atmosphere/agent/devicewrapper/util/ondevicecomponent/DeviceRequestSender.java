package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.EOFException;
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
import com.musala.atmosphere.agent.exception.PortForwardingRemovalException;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.RequestType;
import com.musala.atmosphere.commons.ad.service.ConnectionConstants;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Handles requests sent to an ATMOSPHERE on-device component.
 * 
 * @author yordan.petrov
 * 
 */
public abstract class DeviceRequestSender<T extends RequestType> {

    private static final Logger LOGGER = Logger.getLogger(DeviceRequestSender.class);

    private static final int REQUEST_RETRY_TIMEOUT = 1000;

    private static final String HOST_NAME = "localhost";

    private static final int CONNECTION_RETRY_LIMIT = AgentPropertiesLoader.getOnDeviceComponentConnectionRetryLimit();

    private Socket socketClient;

    private OutputStream socketClientOutputStream;

    private InputStream socketClientInputStream;

    protected PortForwardingService portForwardingService;

    /**
     * Creates an {@link DeviceRequestSender on-device request sender} instance, that sends requests to an ATMOSPHERE
     * on-device component and retrieves the response.
     * 
     * @param portForwarder
     *        - a port forwarding service, that will be used to forward a local port to the remote port of the on-device
     *        component
     */
    public DeviceRequestSender(PortForwardingService portForwarder) {
        portForwardingService = portForwarder;
    }

    /**
     * Establishes connection to an ATMOSPHERE on-device component.
     * 
     * @throws IOException
     *         when an I/O error occurs when closing this socket.
     */
    private void connect() throws IOException {
        portForwardingService.forwardPort();

        int retries = 0;

        while (true) {
            try {
                closeClientConnection();
                int localForwardedPort = portForwardingService.getLocalForwardedPort();
                socketClient = new Socket(HOST_NAME, localForwardedPort);
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
                        // Nothing to do here.
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
     *         when an I/O error occurs when closing this socket.
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
     *        - request that will be send to the ATMOSPHERE on-device component
     * @return the response from the ATMOSPHERE service
     * @throws ClassNotFoundException
     *         when the response is not of the correct class
     * @throws IOException
     *         when connection or request execution results in an I/O exception
     * @throws CommandFailedException
     *         when the response is invalid
     */
    public Object request(Request<T> socketServerRequest)
        throws ClassNotFoundException,
            UnknownHostException,
            IOException,
            CommandFailedException {

        Object readRequest = ConnectionConstants.UNRECOGNIZED_SERVICE_REQUEST;

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
            } catch (SocketException | EOFException e) {
                LOGGER.error(e);
            }

            disconnect();
            wait(REQUEST_RETRY_TIMEOUT);
        }

        if (readRequest != ConnectionConstants.UNRECOGNIZED_SERVICE_REQUEST) {
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
     *        - time in milliseconds for which the thread should stay inactive.
     */
    private void wait(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            LOGGER.debug("Thread sleep interrupted.", e);
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

    /**
     * Disconnects and releases allocated ports. The request sender becomes unusable.
     */
    public void stop() {
        try {
            disconnect();
        } catch (IOException e) {
            LOGGER.warn("Could not ensure socket connection is closed when stopping request sender.", e);
        }

        try {
            portForwardingService.stop();
        } catch (PortForwardingRemovalException e) {
            String loggerPortRemovalFailedMessage = String.format("Removing Remote Forwarded Port failed.");
            LOGGER.warn(loggerPortRemovalFailedMessage, e);
        }
    }
}
