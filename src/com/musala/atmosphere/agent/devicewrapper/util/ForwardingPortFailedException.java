package com.musala.atmosphere.agent.devicewrapper.util;

/**
 * Thrown when the forwarding of port to an ATMOSPHERE on-device application fails.
 * 
 * @author yordan.petrov
 * 
 */
public class ForwardingPortFailedException extends RuntimeException
{
	/**
	 * auto-generated serialization id
	 */
	private static final long serialVersionUID = 1281842250823943876L;

	public ForwardingPortFailedException()
	{
	}

	public ForwardingPortFailedException(String message)
	{
		super(message);
	}

	public ForwardingPortFailedException(String message, Throwable inner)
	{
		super(message, inner);
	}
}
