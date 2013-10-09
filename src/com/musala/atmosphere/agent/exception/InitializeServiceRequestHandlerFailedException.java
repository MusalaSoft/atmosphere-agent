package com.musala.atmosphere.agent.exception;

/**
 * Thrown when the initialization of communication to the ATMOSPHERE service fails.
 * 
 * @author yordan.petrov
 * 
 */
public class InitializeServiceRequestHandlerFailedException extends RuntimeException
{
	private static final long serialVersionUID = -194901596410158197L;

	public InitializeServiceRequestHandlerFailedException()
	{
	}

	public InitializeServiceRequestHandlerFailedException(String message)
	{
		super(message);
	}

	public InitializeServiceRequestHandlerFailedException(String message, Throwable inner)
	{
		super(message, inner);
	}
}