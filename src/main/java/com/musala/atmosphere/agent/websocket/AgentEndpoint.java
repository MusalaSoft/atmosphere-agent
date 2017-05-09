package com.musala.atmosphere.agent.websocket;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.PongMessage;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.websocket.message.MessageAction;
import com.musala.atmosphere.commons.websocket.message.RequestMessage;
import com.musala.atmosphere.commons.websocket.message.ResponseMessage;
import com.musala.atmosphere.commons.websocket.util.GsonUtil;
import com.musala.atmosphere.commons.websocket.util.IJsonUtil;
import com.musala.atmosphere.commons.websocket.util.JsonConst;

/**
 * Represents a client endpoint for all incoming messages.
 *
 * @author dimcho.nedev
 *
 */
@ClientEndpoint
public class AgentEndpoint {
    private static final Logger LOGGER = Logger.getLogger(AgentEndpoint.class.getCanonicalName());

    private AgentDispatcher dispatcher = AgentDispatcher.getInstance();

    private static final IJsonUtil jsonUtil = new GsonUtil();

    public AgentEndpoint() {
    }

    @OnMessage
    public void onPongMessage(PongMessage message, Session session) {
        // nothing to do here
    }

    @OnMessage
    public void onJsonMessage(String jsonMessage, Session session) {
        MessageAction messageAction = jsonUtil.getProperty(jsonMessage, JsonConst.MESSAGE_ACTION, MessageAction.class);

        switch (messageAction) {
            case ROUTING_ACTION:
                RequestMessage request = jsonUtil.deserializeRequest(jsonMessage);
                dispatcher.executeRoutingActionRequest(request);
                break;
            case ERROR:
                ResponseMessage response = jsonUtil.deserializeResponse(jsonMessage);
                LOGGER.error("Server error", response.getException());
                break;
            default:
                LOGGER.error("Invalid message action.");
                break;
        }
    }

}
