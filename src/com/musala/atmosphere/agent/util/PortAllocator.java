package com.musala.atmosphere.agent.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Stack;

import com.musala.atmosphere.agent.exception.NoFreePortAvailableException;

/**
 * Allocates free ports.
 * 
 * @author yordan.petrov
 * 
 */

public class PortAllocator
{
	private final static int MIN_FORWARD_PORT = AgentPropertiesLoader.getADBMinForwardPort();

	private final static int MAX_FORWARD_PORT = AgentPropertiesLoader.getADBMaxForwardPort();

	private static final Stack<Integer> freePorts = new Stack<Integer>();

	/**
	 * Registers a port that has recently been freed by a testing device.
	 * 
	 * @param port
	 *        - the free port identifier
	 */
	public static void registerFreePort(int port)
	{
		freePorts.add(port);
	}

	/**
	 * Gets a free port from the list of freed ports by the released devices.
	 * 
	 * @return a free port identifier.
	 * @throws NoFreePortAvailableException
	 */
	private static int getFreePortFromList() throws NoFreePortAvailableException
	{
		while (!freePorts.isEmpty())
		{
			int currentPort = freePorts.pop();

			if (isPortAvailable(currentPort))
			{
				return currentPort;
			}
		}

		throw new NoFreePortAvailableException("Colud not find a free port in the list of freed ports.");
	}

	/**
	 * Gets a free port from the port range specified in the agent.properties file.
	 * 
	 * @return a free port identifier.
	 * @throws NoFreePortAvailableException
	 */
	private static int getFreePortFromRange() throws NoFreePortAvailableException
	{
		for (int currentPort = MIN_FORWARD_PORT; currentPort <= MAX_FORWARD_PORT; currentPort++)
		{
			if (isPortAvailable(currentPort))
			{
				return currentPort;
			}
		}

		throw new NoFreePortAvailableException("Colud not find a free port in the port range.");
	}

	/**
	 * Returns a free port identifier.
	 * 
	 * @return a free port identifier.
	 * @throws NoFreePortAvailableException
	 */
	public static int getFreePort() throws NoFreePortAvailableException
	{
		try
		{
			return getFreePortFromList();
		}
		catch (NoFreePortAvailableException e)
		{
		}

		try
		{
			return getFreePortFromRange();
		}
		catch (NoFreePortAvailableException e)
		{
		}

		throw new NoFreePortAvailableException("Colud not find a free port.");
	}

	/**
	 * Checks whether a given port is available.
	 * 
	 * @param port
	 *        - port identifier.
	 * @return - true if the port is available; false otherwise.
	 */
	private static boolean isPortAvailable(int port)
	{
		try
		{
			ServerSocket serverSocket = new ServerSocket(port);
			serverSocket.close();
		}
		catch (IOException e)
		{
			return false;
		}

		return true;
	}
}
