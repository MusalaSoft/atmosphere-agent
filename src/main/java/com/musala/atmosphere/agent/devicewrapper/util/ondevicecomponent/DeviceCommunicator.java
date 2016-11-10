package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.BackgroundShellCommandExecutor;
import com.musala.atmosphere.agent.exception.OnDeviceComponentValidationException;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.RequestType;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Abstract class that communicates with an on-device component.
 *
 * @author yordan.petrov
 *
 * @param <T>
 *        - the {@link RequestType request type} used for communication
 */
public abstract class DeviceCommunicator<T extends RequestType> {
    private static final Logger LOGGER = Logger.getLogger(DeviceCommunicator.class);

    protected DeviceRequestSender<T> requestSender;

    protected String deviceSerialNumber;

    protected BackgroundShellCommandExecutor shellCommandExecutor;

    /**
     * Creates an {@link DeviceCommunicator on-device communicator} instance that communicate with an on-device
     * component.
     *
     * @param requestSender
     *        - a request to an ATMOSPHERE on-device component.
     * @param commandExecutor
     *        - a shell command executor or the device
     * @param serialNumber
     *        - serial number of the device
     */
    public DeviceCommunicator(DeviceRequestSender<T> requestSender,
            BackgroundShellCommandExecutor commandExecutor,
            String serialNumber) {
        this.requestSender = requestSender;
        this.shellCommandExecutor = commandExecutor;
        this.deviceSerialNumber = serialNumber;
    }

    /**
     * Starts the on-device component on the device.
     */
    public abstract void startComponent();

    /**
     * Starts the on-device component on the device.
     */
    public abstract void stopComponent();

    /**
     * Validates that the remote server is an ATMOSPHERE on-device component socket server.
     */
    public abstract void validateRemoteServer();

    /**
     * Validates that the remote server is an ATMOSPHERE on-device component socket server.<br>
     * <b>Note:</b> This is the default implementation for validation logic that can be reused
     *
     * @param validationRequest
     *        - the request that will be used for validation
     */
    protected void validateRemoteServer(Request<T> validationRequest) {
        try {
            Object response = requestSender.request(validationRequest);

            if (!validationRequest.getType().equals(response)) {
                final String fatalMessage = "On-device component validation failed. Validation response did not match expected value.";
                LOGGER.fatal(fatalMessage);
                throw new OnDeviceComponentValidationException(fatalMessage);
            }
        } catch (ClassNotFoundException | IOException | CommandFailedException e) {
            String message = "On-device component validation failed.";
            LOGGER.fatal(message, e);
            throw new OnDeviceComponentValidationException(message, e);
        }
    }

    /**
     * Releases allocated ports.
     */
    public void stop() {
        requestSender.stop();
    }
}
