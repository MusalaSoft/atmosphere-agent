package com.musala.atmosphere.agent;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.command.AgentCommand;
import com.musala.atmosphere.agent.command.AgentCommandFactory;
import com.musala.atmosphere.agent.command.AgentConsoleCommands;
import com.musala.atmosphere.agent.commandline.AgentCommandLine;
import com.musala.atmosphere.agent.state.AgentState;
import com.musala.atmosphere.agent.state.DisconnectedAgent;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.exceptions.ArgumentParseException;
import com.musala.atmosphere.commons.exceptions.CommandLineParseException;
import com.musala.atmosphere.commons.exceptions.OptionNotPresentException;
import com.musala.atmosphere.commons.sa.ConsoleControl;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;
import com.musala.atmosphere.commons.util.Pair;

/**
 * Class that instantiates the Agent ATMOSPHERE component.
 * 
 * @author vladimir.vladimirov, nikola.taushanov
 * 
 */
public class Agent {
    private static final Logger LOGGER = Logger.getLogger(Agent.class.getCanonicalName());

    private static final int SYSTEM_EXIT_CODE_ERROR = -1;

    private AndroidDebugBridgeManager androidDebugBridgeManager;

    private AgentManager agentManager;

    private DeviceManager deviceManager;

    private ConsoleControl agentConsole;

    private AgentState currentAgentState;

    private int agentRmiPort;

    private Date startDate;

    private boolean isRunning;

    /**
     * Creates an Agent component bound on the specified in <i>agent.properties</i> file port.
     */
    public Agent() {
        this(AgentPropertiesLoader.getAgentRmiPort());
    }

    /**
     * Creates an Agent component bound on the given port.
     * 
     * @param agentRmiPort
     *        - RMI port number, on which agent is to be created.
     */
    public Agent(int agentRmiPort) {
        isRunning = true;
        this.agentRmiPort = agentRmiPort;

        try {
            // Registry registry = LocateRegistry.createRegistry(agentRmiPort);
            String pathToAdb = AgentPropertiesLoader.getADBPath();
            androidDebugBridgeManager = new AndroidDebugBridgeManager();
            androidDebugBridgeManager.setAndroidDebugBridgePath(pathToAdb);
            androidDebugBridgeManager.startAndroidDebugBridge();
            agentManager = new AgentManager(agentRmiPort);
            deviceManager = new DeviceManager(agentRmiPort);

            agentConsole = new ConsoleControl();
            startDate = new Date();
            currentAgentState = new DisconnectedAgent(this, agentManager, agentConsole);

            LOGGER.info("Agent created on port: " + agentRmiPort);
        } catch (RemoteException | ADBridgeFailException e) {
            LOGGER.fatal("Could not create agent manager.", e);
            throw new RuntimeException("Creation of agent manager failed.", e);
        }
    }

    /**
     * 
     * @return - the moment in time, rounded to nanosecond, when the Agent was initialized.
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * 
     * @return - the number of the port under which the agent is registered in RMI.
     */
    public int getAgentRmiPort() {
        return agentRmiPort;
    }

    /**
     * Executes a passed shell command from the console.
     * 
     * @param passedShellCommand
     *        - the passed shell command.
     * @throws IllegalArgumentException
     *         - if the passed argument is <b>null</b>
     */
    public void parseAndExecuteShellCommand(String passedShellCommand) {
        if (passedShellCommand != null) {
            Pair<String, List<String>> parsedCommand = ConsoleControl.parseShellCommand(passedShellCommand);
            String command = parsedCommand.getKey();
            List<String> params = parsedCommand.getValue();

            AgentCommand commandForExecution = AgentCommandFactory.getCommandInstance(command, params);
            if (commandForExecution != null) {
                currentAgentState.executeCommand(commandForExecution);
            } else {
                String helpCommand = AgentConsoleCommands.AGENT_HELP.getCommand();
                String errorMessage = String.format("Unknown command \"%s\". Type '%s' for all available commands.",
                                                    command,
                                                    helpCommand);
                agentConsole.writeLine(errorMessage);
            }
        } else {
            LOGGER.error("Error in console: trying to execute 'null' as a command.");
            throw new IllegalArgumentException("Command passed for execution to agent is 'null'");
        }
    }

    /**
     * Sets the current Agent state.
     * 
     * @param state
     *        - the new {@link AgentState AgentState}.
     */
    public void setState(AgentState state) {
        currentAgentState = state;
    }

    /**
     * Reads a command from the agent's console.
     * 
     * @return
     */
    private String readCommand() {
        String command = null;
        try {
            command = agentConsole.readCommand();
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return command;
    }

    /**
     * Stops the agent and releases all allocated resources.
     */
    public void stop() {
        if (isRunning) {
            agentManager.close();
            isRunning = false;
        }

        LOGGER.info("Agent stopped successfully.");
    }

    /**
     * 
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Connects this Agent to a Server.
     * 
     * @param ipAddress
     *        server's IP address.
     * @param port
     *        server's RMI port.
     */
    public void connectToServer(String ip, int port) {
        List<String> params = new LinkedList<String>();
        params.add(ip);
        params.add(String.valueOf(port));
        AgentCommand connectCommand = new AgentCommand(AgentConsoleCommands.AGENT_CONNECT, params);
        currentAgentState.executeConnectCommand(connectCommand);
    }

    /**
     * Gets the unique identifier of the current Agent.
     * 
     * @return Unique identifier for the current Agent.
     * @throws RemoteException
     */
    public String getId() throws RemoteException {
        return agentManager.getAgentId();
    }

    /**
     * Starts an Agent. Either no parameters or one that specifies on which port the Agent will be started can be
     * passed. If the passed argument could not be parsed as integer an exception is thrown. If no parameters or more
     * than one parameter was passed, the Agent will be created on the port specified in the properties file.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        AgentCommandLine commandLine = new AgentCommandLine();
        try {
            commandLine.parseArguments(args);
        } catch (CommandLineParseException e) {
            String errorMessage = "Parsing of the command line arguments failed.";
            LOGGER.fatal(errorMessage, e);

            commandLine.printHelp();
            System.exit(SYSTEM_EXIT_CODE_ERROR);
        }

        boolean hasServerAddress = commandLine.hasServerConnectionOptions();
        InetAddress serverAddress = null;
        Integer serverPortNumber = null;
        if (hasServerAddress) {
            try {
                serverAddress = commandLine.getHostname();
                serverPortNumber = commandLine.getPort();
            } catch (OptionNotPresentException | ArgumentParseException e) {
                String errorMessage = "Parsing hostname or port failed.";
                LOGGER.fatal(errorMessage, e);

                commandLine.printHelp();
                System.exit(SYSTEM_EXIT_CODE_ERROR);
            }
        }

        int portToCreateAgentOn = AgentPropertiesLoader.getAgentRmiPort();

        Agent localAgent = new Agent(portToCreateAgentOn);

        if (hasServerAddress) {
            localAgent.connectToServer(serverAddress.getHostAddress(), serverPortNumber);
        }

        do {
            String passedShellCommand = localAgent.readCommand();
            localAgent.parseAndExecuteShellCommand(passedShellCommand);
        } while (localAgent.isRunning());
    }
}
