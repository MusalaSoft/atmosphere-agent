package com.musala.atmosphere.agent.util;

import com.musala.atmosphere.commons.ad.Request;

public interface FakeOnDeviceComponentRequestHandler {
    public Object handleRequest(Request<?> request);
}
