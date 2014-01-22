package com.musala.atmosphere.agent.util;

import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.RequestType;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;

/**
 * Fakes calls to the ATMOSPHERE service.
 * 
 * @author yordan.petrov
 * 
 */
public class FakeServiceAnswer implements FakeOnDeviceComponentRequestHandler
{
	public final static int FAKE_BATTERY_LEVEL = 69;

	public final static boolean FAKE_POWER_STATE = false;

	public final static Boolean FAKE_RESPONSE = true;

	@Override
	public Object handleRequest(Request<RequestType> request)
	{
		ServiceRequest requestType = (ServiceRequest) request.getType();

		switch (requestType)
		{
			case VALIDATION:
				return requestType;
			case GET_BATTERY_LEVEL:
				return FAKE_BATTERY_LEVEL;
			case GET_POWER_STATE:
				return FAKE_POWER_STATE;
			case SET_WIFI:
			case GET_BATTERY_STATE:
				return 3;
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
			default:
				return null;
		}
	}
}