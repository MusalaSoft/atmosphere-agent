package com.musala.atmosphere.agent;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.IServerEventSender;

/**
 * Common class, which receives events from the Server.
 * 
 * @author filareta.yordanova
 * 
 */

public class AgentEventReceiver extends UnicastRemoteObject implements IServerEventSender {
    private static Logger LOGGER = Logger.getLogger(AgentEventReceiver.class.getCanonicalName());

    private static final long serialVersionUID = 4016119363812433655L;

    public AgentEventReceiver() throws RemoteException {
        super();
    }

    @Override
    public void pingAgent() throws RemoteException {
        LOGGER.info("PING INVOKED!");
        // Tests if connection between Server and Agent still exists.
    }

}
