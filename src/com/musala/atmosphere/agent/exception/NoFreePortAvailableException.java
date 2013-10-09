package com.musala.atmosphere.agent.exception;

/**
 * Thrown when a free port can not be found.
 * 
 * @author yordan.petrov
 * 
 */
public class NoFreePortAvailableException extends RuntimeException
{
	private static final long serialVersionUID = 3921800536966534367L;

	public NoFreePortAvailableException()
	{
	}

	public NoFreePortAvailableException(String message)
	{
		super(message);
	}

	public NoFreePortAvailableException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
