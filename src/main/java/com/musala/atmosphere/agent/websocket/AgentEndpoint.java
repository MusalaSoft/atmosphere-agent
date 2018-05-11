// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

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
