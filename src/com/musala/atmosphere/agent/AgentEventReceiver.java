package com.musala.atmosphere.agent;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.musala.atmosphere.commons.sa.IServerEventSender;

/**
 * Common class, which receives events from the Server.
 * 
 * @author filareta.yordanova
 * 
 */

public class AgentEventReceiver extends UnicastRemoteObject implements IServerEventSender {
    private static final long serialVersionUID = 4016119363812433655L;

    public AgentEventReceiver() throws RemoteException {
        super();
    }

    @Override
    public void pingAgent() throws RemoteException {
        // Tests if connection between Server and Agent still exists.
    }

}
