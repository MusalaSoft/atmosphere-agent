package com.musala.atmosphere.agent.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Stack;

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

	private static int currentPort = MIN_FORWARD_PORT;

	private static final Stack<Integer> FREE_PORTS = new Stack<Integer>();

	/**
	 * Registers a port that has recently been freed by a testing device.
	 * 
	 * @param port
	 *        - the free port identifier
	 */
	public static void registerFreePort(int port)
	{
		FREE_PORTS.add(port);
	}

	/**
	 * Returns a free port identifier.
	 * 
	 * @return - a free port identifier.
	 */
	public static int getFreePort()
	{
		int freePort;

		if (FREE_PORTS.isEmpty())
		{
			freePort = currentPort;
			currentPort++;
		}
		else
		{
			freePort = FREE_PORTS.pop();
		}

		if (freePort > MAX_FORWARD_PORT)
		{
			currentPort = MIN_FORWARD_PORT;
		}

		if (isPortAvailable(freePort))
		{
			return freePort;
		}
		else
		{
			return getFreePort();
		}
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
