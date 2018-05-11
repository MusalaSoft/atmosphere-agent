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
