package com.musala.atmosphere.agent.exception;

/**
 * Thrown when the validation of an ATMOSPHERE on-device component fails.
 * 
 * @author yordan.petrov
 * 
 */
public class OnDeviceComponentValidationException extends RuntimeException
{
	private static final long serialVersionUID = -5184993117889537644L;

	public OnDeviceComponentValidationException()
	{
	}

	public OnDeviceComponentValidationException(String message)
	{
		super(message);
	}

	public OnDeviceComponentValidationException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
