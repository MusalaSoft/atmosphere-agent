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

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.util.OnDeviceComponent;
import com.musala.atmosphere.agent.util.OnDeviceComponentCommand;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Manages IME functionality
 *
 * @author denis.bialev
 *
 */
public class ImeManager {

    private ShellCommandExecutor shellCommandExecutor;

    private static final Logger LOGGER = Logger.getLogger(PreconditionsManager.class.getCanonicalName());

    public ImeManager(ShellCommandExecutor shellCommandExecutor) {
        this.shellCommandExecutor = shellCommandExecutor;
    }

    /**
     * Sets the keyboard with the given ID as the default input method on the device.
     *
     * @param imeID
     *        - the input method ID
     * @return true if setting it was successful; false if not.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public boolean setDefaultKeyboard(String imeID) throws CommandFailedException {

        String command = String.format(OnDeviceComponentCommand.SET_IME.getCommand(), imeID);
        String expectedResponse = String.format(OnDeviceComponentCommand.SET_IME.getExpectedResponse(), imeID);
        String actualResponse = null;

        try {
            actualResponse = shellCommandExecutor.execute(command);
        } catch (CommandFailedException e) {
            String errorMessage = "The given keyboard id could not be set as the default input method.";
            throw new CommandFailedException(errorMessage, e);
        }
        return actualResponse.equals(expectedResponse);
    }

    /**
     * Sets the Atmosphere IME as the default input method on the device.
     *
     * @return true if setting it was successful; false if not.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public boolean setAtmosphereImeAsDefault() throws CommandFailedException {
        return setDefaultKeyboard(OnDeviceComponent.IME.getImeId());
    }
}
