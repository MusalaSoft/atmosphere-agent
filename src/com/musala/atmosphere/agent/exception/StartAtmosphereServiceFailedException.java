package com.musala.atmosphere.agent.exception;

public class StartAtmosphereServiceFailedException extends RuntimeException
{
	private static final long serialVersionUID = -6319875898380745721L;

	public StartAtmosphereServiceFailedException()
	{
	}

	public StartAtmosphereServiceFailedException(String message)
	{
		super(message);
	}

	public StartAtmosphereServiceFailedException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
