package com.musala.atmosphere.agent.devicewrapper.util;

import java.util.concurrent.ExecutorService;

import com.android.ddmlib.IDevice;

/**
 * Class responsible for all device shell command execution able to execute commands in background.
 * 
 * 
 * @author yordan.petrov
 * 
 */
public class BackgroundShellCommandExecutor extends ShellCommandExecutor {
    private final ExecutorService executor;

    /**
     * Creates a shell command executor instance for a specified {@link IDevice}.
     * 
     * @param forDevice
     *        - the device that this instance will install to
     * @param executor
     *        - executor service used for background command execution
     */
    public BackgroundShellCommandExecutor(IDevice forDevice, ExecutorService executor) {
        super(forDevice);
        this.executor = executor;
    }

    /**
     * Executes a command in background.
     * 
     * @param command
     *        - shell command to be executed
     */
    public void executeInBackground(String command) {
        BackgroundShellCommandRunner commandExecutor = new BackgroundShellCommandRunner(command, device);

        executor.submit(commandExecutor);
    }
}
