package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;

import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.exception.OnDeviceComponentValidationException;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;

/**
 * Class that communicates with the ATMOSPHERE service on the wrapped device.
 * 
 * @author yordan.petrov
 * 
 */
public class ServiceRequestHandler extends OnDeviceComponentRequestHandler {
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
                throw new OnDeviceComponentValidationException("Service validation failed. Validation response did not match expected value.");
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new OnDeviceComponentValidationException("Service validation failed.", e);
        }
    }
}
