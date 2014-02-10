package com.musala.atmosphere.agent.exception;

/**
 * Thrown when the port forwarded to the ATMOSPHERE service port can not be freed.
 * 
 * @author yordan.petrov
 * 
 */
public class PortForwardingRemovalException extends RuntimeException
{
	private static final long serialVersionUID = 1973383660575881546L;

	public PortForwardingRemovalException()
	{
	}

	public PortForwardingRemovalException(String message)
	{
		super(message);
	}

	public PortForwardingRemovalException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
