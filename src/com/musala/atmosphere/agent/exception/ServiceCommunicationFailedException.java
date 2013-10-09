package com.musala.atmosphere.agent.exception;

/**
 * Thrown when the communication to the ATMOSPHERE service fails.
 * 
 * @author yordan.petrov
 * 
 */
public class ServiceCommunicationFailedException extends RuntimeException
{
	private static final long serialVersionUID = 1310592628758559355L;

	public ServiceCommunicationFailedException()
	{
	}

	public ServiceCommunicationFailedException(String message)
	{
		super(message);
	}

	public ServiceCommunicationFailedException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
