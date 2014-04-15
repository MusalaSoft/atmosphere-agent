package com.musala.atmosphere.agent.util;

import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorRequest;

public class FakeGesturePlayerAnswer implements FakeOnDeviceComponentRequestHandler {
    @Override
    public Object handleRequest(Request<?> request) {
        UIAutomatorRequest requestType = (UIAutomatorRequest) request.getType();

        switch (requestType) {
            case VALIDATION:
                return requestType;

            default:
                return UIAutomatorRequest.ANY_RESPONSE;
        }
    }
}
