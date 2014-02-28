package com.musala.atmosphere.agent.util;

import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorBridgeRequest;

public class FakeGesturePlayerAnswer implements FakeOnDeviceComponentRequestHandler {
    @Override
    public Object handleRequest(Request<?> request) {
        UIAutomatorBridgeRequest requestType = (UIAutomatorBridgeRequest) request.getType();

        switch (requestType) {
            case VALIDATION:
                return requestType;

            default:
                return UIAutomatorBridgeRequest.ANY_RESPONSE;
        }
    }
}
