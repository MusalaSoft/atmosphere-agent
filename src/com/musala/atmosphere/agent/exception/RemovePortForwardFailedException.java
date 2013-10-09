package com.musala.atmosphere.agent.exception;

/**
 * Thrown when the port forwarded to the ATMOSPHERE service port can not be freed.
 * 
 * @author yordan.petrov
 * 
 */
public class RemovePortForwardFailedException extends RuntimeException
{
	private static final long serialVersionUID = 1973383660575881546L;

	public RemovePortForwardFailedException()
	{
	}

	public RemovePortForwardFailedException(String message)
	{
		super(message);
	}

	public RemovePortForwardFailedException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
