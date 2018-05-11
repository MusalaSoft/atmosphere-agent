// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

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
