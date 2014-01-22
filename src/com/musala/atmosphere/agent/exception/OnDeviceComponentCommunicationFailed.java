package com.musala.atmosphere.agent.exception;

/**
 * Thrown when the communication to an ATMOSPHERE on-device component fails.
 * 
 * @author yordan.petrov
 * 
 */
public class OnDeviceComponentCommunicationFailed extends RuntimeException
{
	private static final long serialVersionUID = 1310592628758559355L;

	public OnDeviceComponentCommunicationFailed()
	{
	}

	public OnDeviceComponentCommunicationFailed(String message)
	{
		super(message);
	}

	public OnDeviceComponentCommunicationFailed(String message, Throwable inner)
	{
		super(message, inner);
	}
}
