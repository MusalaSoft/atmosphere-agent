package com.musala.atmosphere.agent.exception;

/**
 * Thrown when the ATMOSPHERE service stop command fails.
 * 
 * @author yordan.petrov
 * 
 */
public class StopAtmosphereServiceFailedException extends RuntimeException
{
	private static final long serialVersionUID = -8414514520282900457L;

	public StopAtmosphereServiceFailedException()
	{
	}

	public StopAtmosphereServiceFailedException(String message)
	{
		super(message);
	}

	public StopAtmosphereServiceFailedException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
