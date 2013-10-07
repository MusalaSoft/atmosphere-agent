package com.musala.atmosphere.agent.exception;

public class InitializeServiceCommunicatorFailedException extends RuntimeException
{
	private static final long serialVersionUID = -194901596410158197L;

	public InitializeServiceCommunicatorFailedException()
	{
	}

	public InitializeServiceCommunicatorFailedException(String message)
	{
		super(message);
	}

	public InitializeServiceCommunicatorFailedException(String message, Throwable inner)
	{
		super(message, inner);
	}
}