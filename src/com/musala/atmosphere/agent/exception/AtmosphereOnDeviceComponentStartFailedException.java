package com.musala.atmosphere.agent.exception;

/**
 * Thrown when an ATMOSPHERE on-device component start command fails.
 * 
 * @author yordan.petrov
 * 
 */
public class AtmosphereOnDeviceComponentStartFailedException extends RuntimeException
{
	private static final long serialVersionUID = -6319875898380745721L;

	public AtmosphereOnDeviceComponentStartFailedException()
	{
	}

	public AtmosphereOnDeviceComponentStartFailedException(String message)
	{
		super(message);
	}

	public AtmosphereOnDeviceComponentStartFailedException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
