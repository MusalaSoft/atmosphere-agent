package com.musala.atmosphere.agent.exception;

/**
 * Thrown when the communication to an ATMOSPHERE on-device component fails.
 * 
 * @author yordan.petrov
 * 
 */
public class OnDeviceComponentCommunicationException extends RuntimeException
{
	private static final long serialVersionUID = 1310592628758559355L;

	public OnDeviceComponentCommunicationException()
	{
	}

	public OnDeviceComponentCommunicationException(String message)
	{
		super(message);
	}

	public OnDeviceComponentCommunicationException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
