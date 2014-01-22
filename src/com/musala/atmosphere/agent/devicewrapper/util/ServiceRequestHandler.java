package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;

import com.musala.atmosphere.agent.devicewrapper.PortForwardingService;
import com.musala.atmosphere.agent.exception.ServiceValidationFailedException;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;

/**
 * Class that communicates with the ATMOSPHERE service on the wrapped device.
 * 
 * @author yordan.petrov
 * 
 */
public class ServiceRequestHandler extends OnDeviceComponentRequestHandler
{
	public ServiceRequestHandler(PortForwardingService portForwarder, int socketPort)
	{
		super(portForwarder, socketPort);
	}

	@Override
	protected void forwardComponentPort()
	{
		portForwardingService.forwardServicePort();
	}

	@Override
	protected void validateRemoteServer()
	{
		forwardComponentPort();

		try
		{
			Request serviceRequest = new Request(ServiceRequest.VALIDATION);
			ServiceRequest response = (ServiceRequest) request(serviceRequest);
			if (!response.equals(ServiceRequest.VALIDATION))
			{
				throw new ServiceValidationFailedException("Service validation failed. Validation response did not match expected value.");
			}
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new ServiceValidationFailedException("Service validation failed.", e);
		}
	}
}
