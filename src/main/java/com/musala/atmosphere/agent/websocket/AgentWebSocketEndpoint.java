package com.musala.atmosphere.agent.websocket;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.websocket.message.MessageAction;
import com.musala.atmosphere.commons.websocket.message.RequestMessage;
import com.musala.atmosphere.commons.websocket.util.GsonUtil;
import com.musala.atmosphere.commons.websocket.util.IJsonUtil;

/**
 * Represents a client endpoint for all incoming messages.
 *
 * @author dimcho.nedev
 *
 */
@ClientEndpoint
public class AgentWebSocketEndpoint {
    private static final Logger LOGGER = Logger.getLogger(AgentWebSocketEndpoint.class.getCanonicalName());

    private AgentWebSocketDispatcher dispatcher = AgentWebSocketDispatcher.getInstance();

    private IJsonUtil jsonUtil = new GsonUtil();

    public AgentWebSocketEndpoint() {
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.debug("OnOpen: Agent");
    }

    @OnMessage
    public void onPingMessage(PongMessage message, Session session) {
        LOGGER.debug("Ping request received " + message);
    }

    @OnMessage
    public void onJsonMessage(String jsonMessage, Session session) {
        LOGGER.debug("<<< Agent message. >>> " + jsonMessage);

        // MessageAction action = (MessageAction) jsonUtil.getProperty(jsonMessage, "messageAction",
        // MessageAction.class);

        RequestMessage request = jsonUtil.deserializeRequest(jsonMessage);
        MessageAction messageAction = request.getMessageAction();

        switch (messageAction) {
            case ROUTING_ACTION:
                dispatcher.executeRoutingActionRequest(request);
                break;
            case ERROR:
                // TODO: handle this case.
                break;
            default:
                LOGGER.error("Invalid message action.");
                break;
        }
    }

}
