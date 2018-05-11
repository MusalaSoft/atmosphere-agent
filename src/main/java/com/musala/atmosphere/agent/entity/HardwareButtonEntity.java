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

package com.musala.atmosphere.agent.entity;

import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Entity responsible for operations related with hardware buttons.
 *
 * @author filareta.yordanova
 *
 */
public class HardwareButtonEntity {
    private ShellCommandExecutor shellCommandExecutor;

    HardwareButtonEntity(ShellCommandExecutor shellCommandExecutor) {
        this.shellCommandExecutor = shellCommandExecutor;
    }

    /**
     * Presses hardware button on this device.
     *
     * @param keyCode
     *        - button key code as specified by the Android KeyEvent KEYCODE_ constants
     * @return <code>true</code> if the hardware button press is successful, <code>false</code> if it fails
     */
    public Boolean pressButton(int keyCode) {
        String query = "input keyevent " + Integer.toString(keyCode);
        boolean response = true;
        try {
            shellCommandExecutor.execute(query);
        } catch (CommandFailedException e) {
            response = false;
        }

        return response;
    }
}
