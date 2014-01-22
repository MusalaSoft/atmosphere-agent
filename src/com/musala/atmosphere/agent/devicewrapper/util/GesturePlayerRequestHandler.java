package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;

import com.musala.atmosphere.agent.devicewrapper.PortForwardingService;
import com.musala.atmosphere.agent.exception.ServiceValidationFailedException;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.gestureplayer.GesturePlayerRequest;

/**
 * Handles requests sent to the ATMOSPHERE gesture player.
 * 
 * @author yordan.petrov
 * 
 */
public class GesturePlayerRequestHandler extends OnDeviceComponentRequestHandler
{
	public GesturePlayerRequestHandler(PortForwardingService portForwarder, int socketPort)
	{
		super(portForwarder, socketPort);
	}

	@Override
	protected void forwardComponentPort()
	{
		portForwardingService.forwardGesturePlayerPort();
	}

	@Override
	protected void validateRemoteServer()
	{
		forwardComponentPort();

		try
		{
			Request gesturePlayerRequest = new Request(GesturePlayerRequest.VALIDATION);
			GesturePlayerRequest response = (GesturePlayerRequest) request(gesturePlayerRequest);
			if (!response.equals(GesturePlayerRequest.VALIDATION))
			{
				throw new ServiceValidationFailedException("Gesture player validation failed. Validation response did not match expected value.");
			}
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new ServiceValidationFailedException("Gesture player validation failed.", e);
		}
	}
}
