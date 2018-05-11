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

package com.musala.atmosphere.agent.util;

import com.musala.atmosphere.commons.PowerProperties;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;

/**
 * Fakes calls to the ATMOSPHERE service.
 * 
 * @author yordan.petrov
 * 
 */
public class FakeServiceAnswer implements FakeOnDeviceComponentRequestHandler {
    public final static int FAKE_BATTERY_LEVEL = 69;

    public final static PowerProperties FAKE_POWER_PROPERTIES = new PowerProperties();

    public final static Boolean FAKE_RESPONSE = true;

    @Override
    public Object handleRequest(Request<?> request) {
        ServiceRequest requestType = (ServiceRequest) request.getType();

        switch (requestType) {
            case VALIDATION:
                return ServiceRequest.VALIDATION;
            case GET_POWER_PROPERTIES:
                return FAKE_POWER_PROPERTIES;
            case SET_WIFI:
            case GET_CONNECTION_TYPE:
                return 5;
            case GET_ACCELERATION_READINGS:
                Float[] acceleration = new Float[3];
                acceleration[0] = 3.7f;
                acceleration[1] = 5.2f;
                acceleration[2] = -7.1f;
                return acceleration;
            case GET_ORIENTATION_READINGS:
                return new float[] {6.0f, 1.0f, 9.0f};
            case GET_PROXIMITY_READINGS:
                return 0.0f;
            case GET_CAMERA_AVAILABILITY:
                return true;
            case GET_TOTAL_RAM:
                return 10;
            default:
                return null;
        }
    }
}
