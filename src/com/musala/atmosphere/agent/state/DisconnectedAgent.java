package com.musala.atmosphere.agent.state;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.IllegalPortException;
import com.musala.atmosphere.agent.command.AgentCommand;
import com.musala.atmosphere.agent.command.AgentConsoleCommands;
import com.musala.atmosphere.commons.sa.ConsoleControl;

/**
 * State of the agent, used when it is not connected to any servers.
 * 
 * @author nikola.taushanov
 * 
 */
public class DisconnectedAgent extends AgentState {

    private static final Logger LOGGER = Logger.getLogger(DisconnectedAgent.class);

    public DisconnectedAgent(Agent agent, AgentManager agentManager, ConsoleControl agentConsole) {
        super(agent, agentManager, agentConsole);
    }

    @Override
    public void executeServerAdressCommand(AgentCommand commandForExecution) {
        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_SERVER_ADDRESS, commandForExecution);

            String consoleMessage = "Not connected to a Server.";
            agentConsole.writeLine(consoleMessage);
        }
        catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        }
    }

    @Override
    public void executeConnectCommand(AgentCommand commandForExecution) {
        List<String> args = commandForExecution.getCommandParameters();
        String serverPortAsString = null;
        Integer serverPort = null;
        String serverIp = "localhost";

        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_CONNECT, commandForExecution);

            switch (args.size()) {
                case 1:
                    serverPortAsString = args.get(0);
                    serverPort = Integer.parseInt(serverPortAsString);
                    break;
                case 2:
                    serverIp = args.get(0);
                    serverPortAsString = args.get(1);
                    serverPort = Integer.parseInt(serverPortAsString);
                    break;
                default:
                    LOGGER.error("Invalid number of argumens passed for CONNECT command: expected 1 or 2; got "
                            + args.size());
                    agentConsole.writeLine("Error connecting agent to server: invalid parameters passed to the \"connect\" command. Type 'help' for more information on available commands.");
                    return;
            }
            agentManager.connectToServer(serverIp, serverPort);

            AgentState connectedAgent = new ConnectedAgent(agent, agentManager, agentConsole, serverIp, serverPort);
            agent.setState(connectedAgent);
        }
        catch (NumberFormatException e) {
            LOGGER.error("Invalid port number passed as argument of CONNECT command: expected number; got \""
                    + serverPortAsString + "\"", e);
        }
        catch (RemoteException | IllegalPortException | NotBoundException e) {
            LOGGER.error("Could not establish connection to server on adress \"" + serverIp + ":" + serverPort
                    + "\". See log for information about the underlying exception.", e);
        }
        catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        }
    }
}
