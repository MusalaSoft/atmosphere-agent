package com.musala.atmosphere.agent.state;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.command.AgentCommand;
import com.musala.atmosphere.agent.command.AgentConsoleCommands;
import com.musala.atmosphere.commons.sa.ConsoleControl;

/**
 * State of the agent, used when it is connected to a server.
 * 
 * @author nikola.taushanov
 * 
 */
public class ConnectedAgent extends AgentState {
    private static final Logger LOGGER = Logger.getLogger(ConnectedAgent.class.getCanonicalName());

    private String serverIp;

    private int serverPort;

    public ConnectedAgent(Agent agent,
            AgentManager agentManager,
            ConsoleControl agentConsole,
            String serverIp,
            int serverPort) {
        super(agent, agentManager, agentConsole);
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    @Override
    public void executeServerAdressCommand(AgentCommand commandForExecution) {
        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_SERVER_ADDRESS, commandForExecution);

            String consoleMessage = String.format("Server address - \"%s:%d\"", serverIp, serverPort);
            agentConsole.writeLine(consoleMessage);
        }
        catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        }
    }

    @Override
    public void executeConnectCommand(AgentCommand commandForExecution) {
        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_CONNECT, commandForExecution);

            String serverAdress = String.format("\"%s:%d\"", serverIp, serverPort);
            agentConsole.writeLine("Agent already connected to " + serverAdress);

            LOGGER.info("Error connecting agent to server: agent is already connected to " + serverAdress);
        }
        catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        }
    }

}
