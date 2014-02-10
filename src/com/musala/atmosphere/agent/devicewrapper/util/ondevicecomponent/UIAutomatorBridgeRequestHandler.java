package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;

import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.exception.OnDeviceComponentValidationException;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorBridgeRequest;

/**
 * Handles requests sent to the ATMOSPHERE UIAutomator bridge player.
 * 
 * @author yordan.petrov
 * 
 */
public class UIAutomatorBridgeRequestHandler extends OnDeviceComponentRequestHandler
{
	public UIAutomatorBridgeRequestHandler(PortForwardingService portForwarder, int socketPort)
	{
		super(portForwarder, socketPort);
	}

	@Override
	protected void forwardComponentPort()
	{
		portForwardingService.forwardUIAutomatorBridgePort();
	}

	@Override
	protected void validateRemoteServer()
	{
		forwardComponentPort();

		try
		{
			Request uiAutomatorBridgeRequest = new Request(UIAutomatorBridgeRequest.VALIDATION);
			UIAutomatorBridgeRequest response = (UIAutomatorBridgeRequest) request(uiAutomatorBridgeRequest);
			if (!response.equals(UIAutomatorBridgeRequest.VALIDATION))
			{
				throw new OnDeviceComponentValidationException("UIAutomator bridge validation failed. Validation response did not match expected value.");
			}
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new OnDeviceComponentValidationException("UIAutomator bridge validation failed.", e);
		}
	}
}
