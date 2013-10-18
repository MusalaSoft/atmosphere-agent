package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

	private OutputStream socketClientOutputStream;

	private InputStream socketClientInputStream;

	private final int socketPort;

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
		int retries = 0;

		while (true)
		{
			try
			{
				socketClient = new Socket(HOST_NAME, socketPort);
				socketClientInputStream = socketClient.getInputStream();
				socketClientOutputStream = socketClient.getOutputStream();
				break;
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
	 * @return the response from the ATMOSPHERE service.
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

		try
		{
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socketClientOutputStream);
			objectOutputStream.flush();
			objectOutputStream.writeObject(socketServerRequest);
			objectOutputStream.flush();

			ObjectInputStream objectInputStream = new ObjectInputStream(socketClientInputStream);
			Object inputObject = objectInputStream.readObject();

			objectInputStream.close();
			objectOutputStream.close();

			return inputObject;
		}
		finally
		{
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
		if (socketClientInputStream != null)
		{
			socketClientInputStream.close();
			socketClientInputStream = null;
		}
		if (socketClientOutputStream != null)
		{
			socketClientOutputStream.close();
			socketClientOutputStream = null;
		}
		if (socketClient != null)
		{
			socketClient.close();
			socketClient = null;
		}
	}
}
