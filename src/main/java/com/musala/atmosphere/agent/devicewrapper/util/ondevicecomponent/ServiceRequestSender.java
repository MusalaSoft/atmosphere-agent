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
