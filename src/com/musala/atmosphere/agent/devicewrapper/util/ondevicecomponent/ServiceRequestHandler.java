package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.exception.OnDeviceComponentValidationException;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Class that communicates with the ATMOSPHERE service on the wrapped device.
 * 
 * @author yordan.petrov
 * 
 */
public class ServiceRequestHandler extends OnDeviceComponentRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(ServiceRequestHandler.class);

    public ServiceRequestHandler(PortForwardingService portForwarder, int socketPort) {
        super(portForwarder, socketPort);
    }

    @Override
    protected void forwardComponentPort() {
        portForwardingService.forwardServicePort();
    }

    @Override
    protected void validateRemoteServer() {
        forwardComponentPort();

        try {
            Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.VALIDATION);
            ServiceRequest response = (ServiceRequest) request(serviceRequest);
            if (!response.equals(ServiceRequest.VALIDATION)) {
                final String fatalMessage = "Service validation failed. Validation response did not match expected value.";
                LOGGER.fatal(fatalMessage);
                throw new OnDeviceComponentValidationException(fatalMessage);
            }
        } catch (ClassNotFoundException | IOException | CommandFailedException e) {
            LOGGER.fatal("Service validation failed.", e);
            throw new OnDeviceComponentValidationException("Service validation failed.", e);
        }
    }
}
