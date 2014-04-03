package com.musala.atmosphere.agent.exception;

public class IllegalPortException extends RuntimeException
{
	private static final long serialVersionUID = -5316605982125815278L;

	public IllegalPortException(String message)
	{
		super(message);
	}

	public IllegalPortException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
