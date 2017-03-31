package com.musala.atmosphere.agent.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.DeviceManager;
import com.musala.atmosphere.agent.devicewrapper.IWrapDevice;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.websocket.message.MessageAction;
import com.musala.atmosphere.commons.websocket.message.RequestMessage;
import com.musala.atmosphere.commons.websocket.message.ResponseMessage;
import com.musala.atmosphere.commons.websocket.util.GsonUtil;
import com.musala.atmosphere.commons.websocket.util.IJsonUtil;

/**
 * Dispatches the {@link RequestMessage request} and {@link ResponseMessage response} messages.
 *
 * @author dimcho.nedev
 *
 */
public class AgentDispatcher {
    private static final Logger LOGGER = Logger.getLogger(AgentDispatcher.class.getCanonicalName());

    private static final String SERVER_URI = "ws://%s:%s/server_agent";

    private static final String ACTION_FAILD_MESSAGE = "An action execution on a device has failed.";

    private Session session;

    private DeviceManager deviceManager;

    private AgentManager agentManager;

    private final IJsonUtil jsonUtil = new GsonUtil();

    private static class DispatcherLoader {
        private static final AgentDispatcher INSTANCE = new AgentDispatcher();
    }

    public static AgentDispatcher getInstance() {
        return DispatcherLoader.INSTANCE;
    }

    /**
     * Connects the current agent to a server.
     *
     * @param serverAddress
     *        - address of the agent we want to connect to.
     * @param webSocketPort
     *        - the port number, on which the Server is listening
     * @param agentId
     *        - An identifier of the current agent
     * @throws URISyntaxException
     *         - thrown to indicate that a string could not be parsed as a URI reference
     * @throws IOException
     *         - thrown when I/O exception of some sort has occurred
     * @throws DeploymentException
     *         - failure to publish an endpoint on its server, or a failure to connect a client to its server
     */
    public void connectToServer(String serverAddress, int webSocketPort, String agentId)
        throws DeploymentException,
            IOException,
            URISyntaxException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxTextMessageBufferSize(Integer.MAX_VALUE);
        String uriAddress = String.format(SERVER_URI, serverAddress, webSocketPort);
        this.session = container.connectToServer(AgentEndpoint.class, new URI(uriAddress));

        List<DeviceInformation> connectedDevicesInformation = deviceManager.getDevicesInformation();
        DeviceInformation[] devicesInformationArray = connectedDevicesInformation.toArray(new DeviceInformation[0]);

        RequestMessage registerAgentRequest = new RequestMessage(MessageAction.REGISTER_AGENT,
                                                                 agentId,
                                                                 devicesInformationArray);

        String registerAgentJsonRequest = jsonUtil.serialize(registerAgentRequest);
        sendText(registerAgentJsonRequest, session);

        LOGGER.debug("Connected to server address: " + uriAddress);

        agentManager.registerServer();
        LOGGER.info("The Server(" + serverAddress + ":" + webSocketPort
                + ") is registered for the device changed event notifications.");
    }

    /**
     * Sends information for specific connected/disconnected device.
     *
     * @param agentId
     *        - identifier of the agent
     * @param deviceSerial
     *        - device serial number
     * @param connected
     *        - true if the device is now available, false if it became unavailable
     * @throws CommandFailedException
     *         if getting device's information fails
     */
    public void sendConnectedDeviceInformation(String agentId, String deviceSerial, boolean connected)
        throws CommandFailedException {

        IWrapDevice deviceWrapper = deviceManager.getDeviceWrapperByDeviceId(deviceSerial);

        DeviceInformation deviceInformation = null;
        if (connected) {
            deviceInformation = (DeviceInformation) deviceWrapper.route(RoutingAction.GET_DEVICE_INFORMATION);
        }

        RequestMessage deviceChangedRequest = new RequestMessage(MessageAction.DEVICE_CHANGED,
                                                                 deviceInformation,
                                                                 connected);
        deviceChangedRequest.setAgentId(agentId);
        deviceChangedRequest.setDeviceId(deviceSerial);

        String jsonRequest = jsonUtil.serialize(deviceChangedRequest);
        sendText(jsonRequest, session);

        LOGGER.debug("JSON: " + jsonRequest);
    }

    /**
     * Executes a routing action {@link RequestMessage request}.
     *
     * @param request
     *        - {@link RequestMessage request message}
     */
    void executeRoutingActionRequest(RequestMessage request) {
        Object result = null;
        RoutingAction requestAction = request.getRoutingAction();
        try {
            if (!request.isAsync()) {
                result = this.route(request);

                ResponseMessage response = new ResponseMessage(MessageAction.ROUTING_ACTION, requestAction, result);
                response.setSessionId(request.getSessionId());
                response.setDeviceId(request.getDeviceId());

                sendText(jsonUtil.serialize(response), session);
            } else {
                Object[] arguments = request.getArguments();
                IWrapDevice deviceWrapper = getWrapper(request);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            deviceWrapper.route(requestAction, arguments);
                        } catch (CommandFailedException e) {
                            sendErrorResponseMessage(e, session, request);
                            LOGGER.error(ACTION_FAILD_MESSAGE, e);
                        }
                    }
                }) {}.start();
            }
        } catch (Exception e) {
            sendErrorResponseMessage(e, session, request);
            LOGGER.error(ACTION_FAILD_MESSAGE, e);
        }
    }

    private IWrapDevice getWrapper(RequestMessage webSocketRequest) {
        String deviceId = webSocketRequest.getDeviceId();
        int startIndex = deviceId.indexOf('_');
        String deviceSerial = deviceId.substring(startIndex + 1);
        IWrapDevice deviceWrapper = deviceManager.getDeviceWrapperByDeviceId(deviceSerial);

        return deviceWrapper;
    }

    /**
     * Requests an action invocation on the device wrapper.
     *
     * @param webSocketRequest
     *        - {@link RequestMessage RequestAbortedException message}
     * @return the result of the action as Java {@link Object}
     * @throws CommandFailedException
     *         - thrown when the routing action execution fails.
     */
    private Object route(RequestMessage webSocketRequest) throws CommandFailedException {
        RoutingAction webSocketRequestAction = webSocketRequest.getRoutingAction();
        Object[] arguments = webSocketRequest.getArguments();
        IWrapDevice deviceWrapper = getWrapper(webSocketRequest);

        Object result = deviceWrapper.route(webSocketRequestAction, arguments);

        return result;
    }

    /**
     * Closes the connection with the server.
     */
    public void close() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                LOGGER.error("The connection is closed with an error.", e);
            }
        }
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    private void sendErrorResponseMessage(Exception ex, Session session, RequestMessage request) {
        ResponseMessage errorResponse = new ResponseMessage(MessageAction.ERROR, null, null);
        errorResponse.setSessionId(request.getSessionId());
        errorResponse.setDeviceId(request.getDeviceId());
        errorResponse.setException(ex);

        sendText(jsonUtil.serialize(errorResponse), session);
    }

    private void sendText(String message, Session session) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            LOGGER.error("Failed to send a JSON message.", e);
        }
    }

}
