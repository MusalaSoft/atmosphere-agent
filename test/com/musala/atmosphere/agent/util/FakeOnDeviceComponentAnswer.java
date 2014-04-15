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
import com.musala.atmosphere.commons.ad.service.ServiceRequest;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorRequest;

public class FakeOnDeviceComponentAnswer implements Answer<Void> {
    public final static int FAKE_BATTERY_LEVEL = 69;

    public final static boolean FAKE_POWER_STATE = false;

    public final static Boolean FAKE_RESPONSE = true;

    private Integer port;

    private Thread thread;

    private FakeServiceAnswer fakeServiceRequestHandler;

    private FakeGesturePlayerAnswer fakeGesturePlayerRequestHandler;

    public FakeOnDeviceComponentAnswer() {
        super();
        fakeGesturePlayerRequestHandler = new FakeGesturePlayerAnswer();
        fakeServiceRequestHandler = new FakeServiceAnswer();
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
        int newPort = (Integer) invocation.getArguments()[0];
        if (thread != null && port == newPort) {
            return null;
        }
        if (thread != null) {
            thread.stop();
        }
        port = newPort;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                    serverSocketChannel.configureBlocking(true);
                    try {
                        serverSocketChannel.socket().bind(new InetSocketAddress(port));
                    } catch (BindException e) {
                        e.printStackTrace();
                        return;
                    }

                    while (true) {
                        listen(serverSocketChannel);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            private void listen(ServerSocketChannel serverSocketChannel) throws IOException {
                SocketChannel socketChannel = serverSocketChannel.accept();

                ObjectInputStream socketServerInputStream = null;
                ObjectOutputStream socketServerOutputStream = null;

                try {
                    Socket baseSocket = socketChannel.socket();
                    socketServerInputStream = new ObjectInputStream(baseSocket.getInputStream());
                    socketServerOutputStream = new ObjectOutputStream(baseSocket.getOutputStream());
                    Object response = null;
                    Request<?> request = (Request<?>) socketServerInputStream.readObject();
                    Object requestType = request.getType();
                    if (requestType instanceof ServiceRequest) {
                        response = fakeServiceRequestHandler.handleRequest(request);
                    } else if (requestType instanceof UIAutomatorRequest) {
                        response = fakeGesturePlayerRequestHandler.handleRequest(request);
                    } else {
                        System.out.println("Fake On-Device components Answer: WARNING: request could not be recognized as a known request type!");
                    }

                    socketServerOutputStream.writeObject(response);
                    socketServerOutputStream.flush();
                } catch (EOFException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (socketServerInputStream != null) {
                        socketServerInputStream.close();
                    }
                    if (socketServerOutputStream != null) {
                        socketServerOutputStream.close();
                    }
                    if (socketChannel != null) {
                        socketChannel.close();
                    }
                }
            }

        });

        thread.start();
        return null;
    }

    @Override
    public void finalize() {
        thread.interrupt();
    }
}
