package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;

/**
 * Class that communicates with the ATMOSPHERE service on the wrapped device.
 * 
 * @author yordan.petrov
 * 
 */
public class ServiceRequestSender extends DeviceRequestSender<ServiceRequest> {

    /**
     * Creates an {@link ServiceRequestSender service request sender} instance, that sends requests to an ATMOSPHERE
     * service component and retrieves the response.
     * 
     * @param portForwarder
     *        - a port forwarding service, that will be used to forward a local port to the remote port of the
     *        ATMOSPHERE service component
     */
    public ServiceRequestSender(PortForwardingService portForwarder) {
        super(portForwarder);
    }
}
