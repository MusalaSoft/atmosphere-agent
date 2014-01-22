package com.musala.atmosphere.agent.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.RequestType;
import com.musala.atmosphere.commons.ad.gestureplayer.GesturePlayerRequest;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;

public class FakeOnDeviceComponentAnswer implements Answer<Void>
{
	public final static int FAKE_BATTERY_LEVEL = 69;

	public final static boolean FAKE_POWER_STATE = false;

	public final static Boolean FAKE_RESPONSE = true;

	private Integer port;

	Thread thread;

	FakeServiceAnswer fakeServiceRequestHandler;

	FakeGesturePlayerAnswer fakeGesturePlayerRequestHandler;

	public FakeOnDeviceComponentAnswer()
	{
		super();
		fakeGesturePlayerRequestHandler = new FakeGesturePlayerAnswer();
		fakeServiceRequestHandler = new FakeServiceAnswer();
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	@Override
	public Void answer(InvocationOnMock invocation) throws Throwable
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
					try
					{
						serverSocketChannel.socket().bind(new InetSocketAddress(port));
					}
					catch (BindException e)
					{
						return;
					}

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
					Object response = null;
					Object request = socketServerInputStream.readObject();
					Request<RequestType> requestType = (Request<RequestType>) request;
					try
					{
						Request<ServiceRequest> serviceRequest = (Request<ServiceRequest>) request;
						response = fakeServiceRequestHandler.handleRequest(requestType);
					}
					catch (ClassCastException e)
					{
						Request<GesturePlayerRequest> gesturePlayerRequest = (Request<GesturePlayerRequest>) request;
						response = fakeGesturePlayerRequestHandler.handleRequest(requestType);
					}

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

	@Override
	public void finalize()
	{
		thread.interrupt();
	}
}
