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
