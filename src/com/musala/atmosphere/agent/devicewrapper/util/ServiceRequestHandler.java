package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.musala.atmosphere.agent.exception.ServiceValidationFailedException;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.as.ServiceRequest;
import com.musala.atmosphere.commons.as.ServiceRequestType;

/**
 * Class that communicates with the ATMOSPHERE service on the wrapped device.
 * 
 * @author yordan.petrov
 * 
 */
public class ServiceRequestHandler
{
	private static final String HOST_NAME = "localhost";

	private static final int CONNECTION_RETRY_LIMIT = AgentPropertiesLoader.getServiceConnectionRetryLimit();

	private Socket socketClient;

	private int socketPort;

	public ServiceRequestHandler(int socketPort)
	{
		this.socketPort = socketPort;

		validateRemoteServer();
	}

	/**
	 * Validates that the remote server is the ATMOSPHERE service socket server.
	 * 
	 */
	private void validateRemoteServer()
	{
		try
		{
			ServiceRequest serviceRequest = new ServiceRequest(ServiceRequestType.VALIDATION);
			ServiceRequestType response = (ServiceRequestType) request(serviceRequest);
			if (!response.equals(ServiceRequestType.VALIDATION))
			{
				throw new ServiceValidationFailedException("Service validation failed. Validation response did not match expected value.");
			}
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new ServiceValidationFailedException("Service validation failed.", e);
		}
	}

	/**
	 * Establishes connection to the ATMOSPHERE service.
	 * 
	 * @throws IOException
	 */
	private void connect() throws IOException
	{
		boolean isConnected = false;
		int retries = 0;

		while (!isConnected)
		{
			try
			{
				socketClient = new Socket(HOST_NAME, socketPort);
				isConnected = true;
			}
			catch (IOException e)
			{
				disconnect();

				if (retries < CONNECTION_RETRY_LIMIT)
				{
					retries++;

					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e1)
					{
						// Should not get here.
						e1.printStackTrace();
					}
				}
				else
				{
					throw e;
				}
			}
		}
	}

	/**
	 * Sends a {@link ServiceRequestType} request to the ATMOSPHERE service and returns the response.
	 * 
	 * @param socketServerRequest
	 *        - request that will be send to the ATMOSPHERE service.
	 * @return - the response from the ATMOSPHERE service.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public Object request(ServiceRequest socketServerRequest)
		throws ClassNotFoundException,
			UnknownHostException,
			IOException
	{
		connect();

		ObjectOutputStream socketClientObjectOutputStream = null;
		ObjectInputStream socketClientObjectInputStream = null;

		try
		{
			socketClientObjectOutputStream = new ObjectOutputStream(socketClient.getOutputStream());
			socketClientObjectOutputStream.writeObject(socketServerRequest);
			socketClientObjectOutputStream.flush();

			socketClientObjectInputStream = new ObjectInputStream(socketClient.getInputStream());
			Object inputObject = socketClientObjectInputStream.readObject();
			return inputObject;
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (socketClientObjectInputStream != null)
			{
				socketClientObjectInputStream.close();
			}
			if (socketClientObjectOutputStream != null)
			{
				socketClientObjectOutputStream.close();
			}
			disconnect();
		}
	}

	/**
	 * Disconnects from the ATMOSPHERE service if a connection is present.
	 * 
	 * @throws IOException
	 */
	private void disconnect() throws IOException
	{
		if (socketClient != null)
		{
			socketClient.close();
		}
	}
}
