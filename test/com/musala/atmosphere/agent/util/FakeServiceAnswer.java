package com.musala.atmosphere.agent.util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.musala.atmosphere.commons.as.ServiceRequestProtocol;

/**
 * Fakes calls to the ATMOSPHERE service.
 * 
 * @author yordan.petrov
 * 
 */
public class FakeServiceAnswer implements Answer<Void>
{
	public final static int FAKE_BATTERY_LEVEL = 69;

	@Override
	public Void answer(InvocationOnMock invocation) throws Throwable
	{
		final int port = (int) invocation.getArguments()[0];

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
					serverSocketChannel.configureBlocking(true);
					serverSocketChannel.socket().bind(new InetSocketAddress(port));

					SocketChannel socketChannel = serverSocketChannel.accept();

					ObjectOutputStream socketServerOutputStream = new ObjectOutputStream(socketChannel.socket()
																										.getOutputStream());
					ObjectInputStream socketServerInputStream = new ObjectInputStream(socketChannel.socket()
																									.getInputStream());

					ServiceRequestProtocol request = (ServiceRequestProtocol) socketServerInputStream.readObject();

					socketServerOutputStream.writeObject(handleRequest(request));

					socketServerInputStream.close();
					socketServerOutputStream.close();
					socketChannel.close();
					serverSocketChannel.close();
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
			}

		});

		thread.start();
		return null;
	}

	private Object handleRequest(ServiceRequestProtocol request)
	{// TODO implement other requests logic here.
		switch (request)
		{
			case VALIDATION:
				return request;
			case GET_BATTERY_LEVEL:
				return FAKE_BATTERY_LEVEL;
			default:
				return null;
		}
	}
}
