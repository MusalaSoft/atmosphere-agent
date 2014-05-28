package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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

    private static final int COMMAND_EXECUTION_TIMEOUT = AgentPropertiesLoader.getCommandExecutionTimeout();

    private final IDevice device;

    private Map<String, BackgroundShellCommandRunner> backgroundRunnersMap = new HashMap<String, BackgroundShellCommandRunner>();

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
     */
    public String execute(String command, int timeout) throws CommandFailedException {
        String response = "";

        int commandExecutionTimeout = Math.max(timeout, COMMAND_EXECUTION_TIMEOUT);

        try {
            CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
            device.executeShellCommand(command, outputReceiver, commandExecutionTimeout);

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
     */
    public List<String> executeSequence(List<String> commandsList) throws CommandFailedException {
        List<String> responses = new ArrayList<String>(commandsList.size());

        for (String commandForExecution : commandsList) {
            String responseFromCommandExecution = execute(commandForExecution);
            responses.add(responseFromCommandExecution);
        }

        return responses;
    }

    /**
     * Executes a shell command in the background. Returns immediately. Usage should be limited to commands which do
     * will not return for a long time (because of thread related performance issues).
     * 
     * @param command
     *        - shell command that should be executed in the background.
     */
    public void executeInBackground(String command) {
        if (backgroundRunnersMap.containsKey(command)) {
            terminateBackgroundCommand(command);
        }

        BackgroundShellCommandRunner commandExecutor = new BackgroundShellCommandRunner(command, device);
        Thread executorThread = new Thread(commandExecutor);
        executorThread.start();

        backgroundRunnersMap.put(command, commandExecutor);
    }

    /**
     * Returns the execution exception that was thrown when a background shell command was executed (null if no
     * exception was thrown).
     * 
     * @param command
     *        - the executed command for which we want the thrown exception.
     * @return the exception itself.
     */
    public Throwable getBackgroundExecutionException(String command) {
        if (!backgroundRunnersMap.containsKey(command)) {
            throw new NoSuchElementException("No command '" + command + "' was found to be running or done executing.");
        }
        BackgroundShellCommandRunner executor = backgroundRunnersMap.get(command);
        Throwable executionException = executor.getExecutionException();
        return executionException;
    }

    /**
     * Terminates a background executing command.
     * 
     * @param command
     *        - the command to be terminated.
     */
    public void terminateBackgroundCommand(String command) {
        if (!backgroundRunnersMap.containsKey(command)) {
            throw new NoSuchElementException("No command '" + command + "' was found to be running or done executing.");
        }
        BackgroundShellCommandRunner executor = backgroundRunnersMap.get(command);
        Thread executorThread = executor.getExecutorThread();
        if (executorThread.isAlive()) {
            executorThread.stop();
        }
        backgroundRunnersMap.remove(command);
    }

    /**
     * Terminates all commands that are executing in the background.
     */
    public void terminateAllInBackground() {
        for (BackgroundShellCommandRunner runner : backgroundRunnersMap.values()) {
            // we cannot modify the map here!
            Thread executorThread = runner.getExecutorThread();
            executorThread.stop();
        }
        backgroundRunnersMap.clear();
    }
}
