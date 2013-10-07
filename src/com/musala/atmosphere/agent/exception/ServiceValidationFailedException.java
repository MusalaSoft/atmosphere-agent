package com.musala.atmosphere.agent.exception;

public class ServiceValidationFailedException extends RuntimeException
{
	private static final long serialVersionUID = -5184993117889537644L;

	public ServiceValidationFailedException()
	{
	}

	public ServiceValidationFailedException(String message)
	{
		super(message);
	}

	public ServiceValidationFailedException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
