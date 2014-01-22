package com.musala.atmosphere.agent.util;

import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.RequestType;

public interface FakeOnDeviceComponentRequestHandler
{
	public Object handleRequest(Request<RequestType> request);
}
