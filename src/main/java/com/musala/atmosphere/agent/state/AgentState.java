package com.musala.atmosphere.agent.state;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.Agent;
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.DeviceManager;
import com.musala.atmosphere.agent.command.AgentCommand;
import com.musala.atmosphere.agent.command.AgentConsoleCommands;
import com.musala.atmosphere.agent.util.date.DateClockUtil;
import com.musala.atmosphere.commons.sa.ConsoleControl;

/**
 * Common abstract class for each agent state.
 * 
 * @author nikola.taushanov
 * 
 */
public abstract class AgentState {
    private static final Logger LOGGER = Logger.getLogger(AgentState.class);

    protected static final String ILLEGAL_COMMAND_MESSAGE = "Execution failed: illegal command or arguments.";

    protected ConsoleControl agentConsole;

    protected AgentManager agentManager;

    protected Agent agent;

    public AgentState(Agent agent, AgentManager agentManager, ConsoleControl agentConsole) {
        this.agent = agent;
        this.agentManager = agentManager;
        this.agentConsole = agentConsole;
    }

    /**
     * Executes the passed {@link AgentCommand command} and changes
     * 
     * @param commandForExecution
     */
    public void executeCommand(AgentCommand commandForExecution) {
        if (commandForExecution != null) {
            AgentConsoleCommands commandType = commandForExecution.getCommandType();

            switch (commandType) {
                case AGENT_CONNECT:
                    executeConnectCommand(commandForExecution);
                    break;
                case AGENT_DEVICES:
                    executeDevicesCommand(commandForExecution);
                    break;
                case AGENT_EXIT:
                    executeExitCommand(commandForExecution);
                    break;
                case AGENT_HELP:
                    executeHelpCommand(commandForExecution);
                    break;
                case AGENT_PERFORMANCE:
                    executePerformanceCommand(commandForExecution);
                    break;
                case AGENT_SERVER_ADDRESS:
                    executeServerAdressCommand(commandForExecution);
                    break;
                case AGENT_UPTIME:
                    executeUptimeCommand(commandForExecution);
                    break;
                default:
                    LOGGER.error("Command " + commandType.getCommand() + " is not recognized and cannot be executed.");
            }
        } else {
            LOGGER.error("Trying to execute null command on agent.");
        }
    }

    /**
     * Prints information about all available commands that can be executed on the agent, through the agent console.
     * 
     * @param commandForExecution
     */
    public void executeHelpCommand(AgentCommand commandForExecution) {
        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_HELP, commandForExecution);

            List<String> agentCommandsDescriptions = AgentConsoleCommands.getListOfCommandDescriptions();
            for (String commandDescription : agentCommandsDescriptions) {
                agentConsole.writeLine(commandDescription);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        }
    }

    /**
     * Prints the time for which the {@link Agent agent} has been running.
     * 
     * @param commandForExecution
     */
    public void executeUptimeCommand(AgentCommand commandForExecution) {
        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_UPTIME, commandForExecution);

            Date agentStartDate = agent.getStartDate();
            if (agentStartDate != null) {
                String formattedTime = DateClockUtil.formatDateAndTime(agentStartDate);
                agentConsole.writeLine("Agent was started on " + formattedTime);

                String formattedTimeInterval = DateClockUtil.getTimeInterval(agentStartDate);
                agentConsole.writeLine("Uptime: " + formattedTimeInterval);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        }
    }

    /**
     * Tests the hardware possibilities of the agent for supporting emulators.
     * 
     * @param commandForExecution
     *        - should be {@link AgentConsoleCommands#AGENT_PERFORMANCE}
     */
    public void executePerformanceCommand(AgentCommand commandForExecution) {
        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_PERFORMANCE, commandForExecution);

            // TODO Should be implemented when performance score is available.
            LOGGER.error("Error performing benchmarking test: benchmarking mechanism not implemented.");
            agentConsole.writeLine("Performance benchmarking not implemented yet. Command can not be executed.");
        } catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        }
    }

    /**
     * Stops the agent and releases all allocated resources.
     * 
     * @param commandForExecution
     *        - it should be an {@link AgentConsoleCommands#AGENT_EXIT exit } command.
     */
    public void executeExitCommand(AgentCommand commandForExecution) {
        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_EXIT, commandForExecution);

            agent.stop();
        } catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        }
    }

    /**
     * Prints information about the connected devices to this agent on its console. The information consists of number
     * of attached devices and their device wrapper identifiers.
     * 
     * @param commandForExecution
     */
    public void executeDevicesCommand(AgentCommand commandForExecution) {
        try {
            validateAndVerifyCommand(AgentConsoleCommands.AGENT_DEVICES, commandForExecution);

            DeviceManager deviceManager = new DeviceManager();
            List<String> deviceWrapperIDs = deviceManager.getAllDeviceRmiIdentifiers();
            agentConsole.writeLine("Number of devices, attached to this agent: " + deviceWrapperIDs.size());
            for (String deviceWrapper : deviceWrapperIDs) {
                agentConsole.writeLine(deviceWrapper);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Could not execute command.", e);
            agentConsole.writeLine(ILLEGAL_COMMAND_MESSAGE);
        } catch (RemoteException e) {
            LOGGER.error("Could not fetch list with device's serial numbers due to connection problems.", e);
        }
    }

    /**
     * Prints on the console the server address to which the local agent is connected.
     * 
     * @param commandForExecution
     *        - it should be an {@link AgentConsoleCommands#AGENT_SERVER_ADDRESS server address} command.
     */
    public abstract void executeServerAdressCommand(AgentCommand commandForExecution);

    /**
     * Tries to connect the agent to a server, located on the address, specified in the passed command. If the agent is
     * already connected to a server, the new connect command is rejected and not executed.
     * 
     * @param commandForExecution
     *        it should be an {@link AgentConsoleCommands#AGENT_CONNECT connect } command.
     */
    public abstract void executeConnectCommand(AgentCommand commandForExecution);

    /*
     * Private and protected methods.
     */
    /**
     * This method is used to validate that when method for executing a command of some type is invoked, the passed
     * command for execution's type is valid command of the expected type, otherwise an {@link IllegalArgumentException}
     * is thrown.
     * 
     * @param expectedCommandType
     *        - the expected type of command for execution.
     * @param actualCommandForExecution
     *        - the actual command for execution.
     * 
     * @throws IllegalArgumentException
     *         - if the expected command type differs from the command's or the number of parameters is not acceptable.
     */
    protected void validateAndVerifyCommand(AgentConsoleCommands expectedCommandType,
                                            AgentCommand actualCommandForExecution) {
        // validate type of command
        AgentConsoleCommands actualCommandType = actualCommandForExecution.getCommandType();
        if (actualCommandType != expectedCommandType) {
            throw new IllegalArgumentException("Expected command " + expectedCommandType.getCommand()
                    + " but instead got command: " + actualCommandType.getCommand());
        }

        // validate number of parameters
        List<String> parameters = actualCommandForExecution.getCommandParameters();
        int numberOfParameters = parameters.size();

        List<Integer> acceptableNumberOfParameters = actualCommandType.getPossibleNumberOfParameters();
        boolean isNumberOfParamsOk = acceptableNumberOfParameters.contains(numberOfParameters);

        if (!isNumberOfParamsOk) {
            throw new IllegalArgumentException("Illegal number of parameters (" + numberOfParameters
                    + ") passed to command.");
        }
    }
}
