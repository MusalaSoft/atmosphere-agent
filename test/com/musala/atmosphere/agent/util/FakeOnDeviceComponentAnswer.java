package com.musala.atmosphere.agent.util;

import java.io.IOException;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.RequestHandler;
import com.musala.atmosphere.commons.ad.RequestType;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;
import com.musala.atmosphere.commons.ad.socket.OnDeviceSocketServer;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorRequest;

public class FakeOnDeviceComponentAnswer implements Answer<Void>, RequestHandler<RequestType> {

    private Integer port;

    private FakeServiceAnswer fakeServiceRequestHandler;

    private FakeGesturePlayerAnswer fakeGesturePlayerRequestHandler;

    private OnDeviceSocketServer<RequestType> socketServer;

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

        if (socketServer != null && port == newPort) {
            return null;
        }

        stop();

        port = newPort;
        try {
            socketServer = new OnDeviceSocketServer<RequestType>(this, newPort);
            socketServer.start();
        } catch (IOException e) {
        }
        return null;
    }

    public void stop() {
        if (socketServer != null) {
            synchronized (socketServer) {
                if (socketServer != null) {
                    socketServer.terminate();
                }
            }
        }
    }

    @Override
    public Object handle(Request<RequestType> request) {
        Object response = null;
        Object requestType = request.getType();
        if (requestType instanceof ServiceRequest) {
            response = fakeServiceRequestHandler.handleRequest(request);
        } else if (requestType instanceof UIAutomatorRequest) {
            response = fakeGesturePlayerRequestHandler.handleRequest(request);
        } else {
            System.out.println("Fake On-Device components Answer: WARNING: request could not be recognized as a known request type!");
        }

        return response;
    }
}
