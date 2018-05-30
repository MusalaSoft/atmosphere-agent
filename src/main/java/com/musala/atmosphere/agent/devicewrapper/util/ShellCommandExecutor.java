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

package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Class responsible for all device shell command execution.
 *
 * @author georgi.gaydarov
 *
 */
public class ShellCommandExecutor {
    /**
     * The timeout for command execution from the config file.
     */
    private static final int COMMAND_EXECUTION_TIMEOUT = AgentPropertiesLoader.getCommandExecutionTimeout();

    protected final IDevice device;

    /**
     * Creates a shell command executor instance for a specified {@link IDevice}.
     *
     * @param forDevice
     *        - the device that this instance will install to.
     */
    public ShellCommandExecutor(IDevice forDevice) {
        device = forDevice;
    }

    /**
     * Executes a command with a specified timeout on the device's shell and returns the result of the execution. If the
     * default timeout is grater than the requested one, default will be used.
     *
     * @param command
     *        - Shell command to be executed.
     * @param timeout
     *        - timeout to be used in the adb connection, when executing a command on the device.
     * @return Shell response from the command execution.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public String execute(String command, int timeout) throws CommandFailedException {
        String response = "";

        int commandExecutionTimeout = Math.max(timeout, COMMAND_EXECUTION_TIMEOUT);

        try {
            CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
            device.executeShellCommand(command, outputReceiver, commandExecutionTimeout, TimeUnit.MILLISECONDS);

            response = outputReceiver.getOutput();
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            throw new CommandFailedException("Shell command execution failed.", e);
        }

        return response;
    }

    /**
     * Executes a command on the device's shell and returns the result of the execution.
     *
     * @param command
     *        - Shell command to be executed.
     * @return Shell response from the command execution.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public String execute(String command) throws CommandFailedException {
        return execute(command, COMMAND_EXECUTION_TIMEOUT);
    }

    /**
     * Executes a list of shell commands sequentially.
     *
     * @param commandsList
     *        - List of command strings to be executed in the shell of the device.
     * @return List of response strings, one for each executed shell command.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public List<String> executeSequence(List<String> commandsList) throws CommandFailedException {
        List<String> responses = new ArrayList<>(commandsList.size());

        for (String commandForExecution : commandsList) {
            String responseFromCommandExecution = execute(commandForExecution);
            responses.add(responseFromCommandExecution);
        }

        return responses;
    }
}
