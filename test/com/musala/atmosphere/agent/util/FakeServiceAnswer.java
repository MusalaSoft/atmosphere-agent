package com.musala.atmosphere.agent.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.musala.atmosphere.commons.as.ServiceRequest;
import com.musala.atmosphere.commons.as.ServiceRequestType;

/**
 * Fakes calls to the ATMOSPHERE service.
 * 
 * @author yordan.petrov
 * 
 */
public class FakeServiceAnswer implements Answer<Void>
{
	public final static int FAKE_BATTERY_LEVEL = 69;

	public final static boolean FAKE_POWER_STATE = false;

	public final static Boolean FAKE_RESPONSE = true;

	private Integer port;

	Thread thread;

	public void setPort(int port)
	{
		this.port = port;
	}

	@Override
	public Void answer(InvocationOnMock invocation)
	{
		port = (Integer) invocation.getArguments()[0];

		thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
					serverSocketChannel.configureBlocking(true);
					serverSocketChannel.socket().bind(new InetSocketAddress(port));

					while (true)
					{
						listen(serverSocketChannel);
					}
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
			}

			public void listen(ServerSocketChannel serverSocketChannel) throws IOException
			{
				SocketChannel socketChannel = serverSocketChannel.accept();

				ObjectInputStream socketServerInputStream = null;
				ObjectOutputStream socketServerOutputStream = null;

				try
				{
					Socket baseSocket = socketChannel.socket();
					socketServerInputStream = new ObjectInputStream(baseSocket.getInputStream());
					ServiceRequest request = (ServiceRequest) socketServerInputStream.readObject();

					Object response = handleRequest(request);

					socketServerOutputStream = new ObjectOutputStream(baseSocket.getOutputStream());
					socketServerOutputStream.writeObject(response);
					socketServerOutputStream.flush();
				}
				catch (EOFException | ClassNotFoundException e)
				{
					e.printStackTrace();
				}
				finally
				{
					if (socketServerInputStream != null)
					{
						socketServerInputStream.close();
					}
					if (socketServerOutputStream != null)
					{
						socketServerOutputStream.close();
					}
					if (socketChannel != null)
					{
						socketChannel.close();
					}
				}
			}

		});

		thread.start();
		return null;
	}

	private Object handleRequest(ServiceRequest request)
	{// TODO implement other requests logic here.
		ServiceRequestType requestType = request.getType();

		switch (requestType)
		{
			case VALIDATION:
				return requestType;
			case GET_BATTERY_LEVEL:
				return FAKE_BATTERY_LEVEL;
			case GET_POWER_STATE:
				return FAKE_POWER_STATE;
			case SET_WIFI:
			case GET_BATTERY_STATE:
				return 3;
			case GET_CONNECTION_TYPE:
				return 5;
			default:
				return null;
		}
	}

	@Override
	public void finalize()
	{
		thread.interrupt();
	}
}
