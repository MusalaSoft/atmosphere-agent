package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * A class that is used for running shell commands that do not return.
 *
 * @author georgi.gaydarov
 *
 */
public class BackgroundShellCommandRunner implements Runnable {
    private final String command;

    private Exception onExecutionException;

    private Thread executorThread;

    private final IDevice wrappedDevice;

    /**
     *
     * @param command
     *        - shell command to be executed.
     * @param wrappedDevice
     *        - device wrapper that will be used for the execution.
     */
    public BackgroundShellCommandRunner(String command, IDevice wrappedDevice) {
        this.command = command;
        this.wrappedDevice = wrappedDevice;
    }

    @Override
    public void run() {
        executorThread = Thread.currentThread();
        try {
            NullOutputReceiver outputReceiver = new NullOutputReceiver();
            /*
             * this execution will never throw a
             * ShellCommandUnresponsiveException
             */
            wrappedDevice.executeShellCommand(command, outputReceiver, 0, TimeUnit.MICROSECONDS);
        } catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e) {
            onExecutionException = new CommandFailedException("Shell command execution failed. See the enclosed exception for more information.",
                                                              e);
        }
    }

    /**
     *
     * @return true if an exception was thrown when the passed shell command was executed, false otherwise.
     */
    public boolean isExecutionExceptionThrown() {
        return onExecutionException != null;
    }

    /**
     *
     * @return the exception which was thrown when the passed shell command was executed (null if no exception was
     *         thrown).
     */
    public Exception getExecutionException() {
        return onExecutionException;
    }

    /**
     *
     * @return the {@link Thread} object that is responsible for the shell command execution.
     */
    public Thread getExecutorThread() {
        return executorThread;
    }
}
