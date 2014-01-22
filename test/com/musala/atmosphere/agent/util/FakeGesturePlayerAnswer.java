package com.musala.atmosphere.agent.util;

import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.RequestType;
import com.musala.atmosphere.commons.ad.gestureplayer.GesturePlayerRequest;

public class FakeGesturePlayerAnswer implements FakeOnDeviceComponentRequestHandler
{
	@Override
	public Object handleRequest(Request<RequestType> request)
	{
		GesturePlayerRequest requestType = (GesturePlayerRequest) request.getType();

		switch (requestType)
		{
			case VALIDATION:
				return requestType;

			default:
				return GesturePlayerRequest.ANY_RESPONSE;
		}
	}
}
