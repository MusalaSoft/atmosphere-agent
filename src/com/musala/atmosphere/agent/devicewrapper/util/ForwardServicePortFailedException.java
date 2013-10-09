package com.musala.atmosphere.agent.devicewrapper.util;

/**
 * Thrown when the forwarding of port to the ATMOSPHERE service fails.
 * 
 * @author yordan.petrov
 * 
 */
public class ForwardServicePortFailedException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1281842250823943876L;

	public ForwardServicePortFailedException()
	{
	}

	public ForwardServicePortFailedException(String message)
	{
		super(message);
	}

	public ForwardServicePortFailedException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
