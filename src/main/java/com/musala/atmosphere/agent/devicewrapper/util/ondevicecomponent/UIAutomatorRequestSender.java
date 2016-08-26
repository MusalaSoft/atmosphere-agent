package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorRequest;

/**
 * Class that communicates with the ATMOSPHERE gesture player on the wrapped device.
 * 
 * @author yordan.petrov
 * 
 */
public class UIAutomatorRequestSender extends DeviceRequestSender<UIAutomatorRequest> {
    private static final Logger LOGGER = Logger.getLogger(UIAutomatorRequest.class);

    /**
     * Creates an {@link UIAutomatorRequestSender uiautomator request sender} instance, that sends requests to the
     * ATMOSPHERE uiautomator component and retrieves the response.
     * 
     * @param portForwarder
     *        - a port forwarding service, that will be used to forward a local port to the remote port of the
     *        ATMOSPHERE uiautomator component
     */
    public UIAutomatorRequestSender(PortForwardingService portForwarder) {
        super(portForwarder);
    }
}
