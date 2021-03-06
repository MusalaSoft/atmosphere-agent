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

/**
 * An enum listing all possible emulator console commands along with their properties
 *
 * @author boris.strandjev
 */
public enum EmulatorCommand {
    PING("help\r\n", "Ping"),
    AUTHENTICATE("auth %s\r\n", "Authenticate"),
    SET_BATTERY_LEVEL("power capacity %s\r\n", "Set battery level"),
    SET_BATTERY_STATE("power status %s\r\n", "Set battery state"),
    SET_POWER_STATE("power ac %s\r\n", "Set power state"),
    SET_NETWORK_SPEED("network speed %s:%s\r\n", "Set network speed"),
    SET_ORIENTATION("sensor set orientation %s\r\n", "Set orientation"),
    SET_ACCELERATION("sensor set acceleration %s\r\n", "Set acceleration"),
    SET_MOBILE_DATA_STATE("gsm data %s\r\n", "Set mobile data state"),
    SET_MAGNETIC_FIELD("sensor set magnetic-field %s\r\n", "Set magnetic field"),
    SET_PROXIMITY("sensor set proximity %s\r\n", "Set proximity"),
    SEND_SMS("sms send %s %s\r\n", "Send sms"),
    RECEIVE_CALL("gsm call %s\r\n", "Receive call"),
    ACCEPT_CALL("gsm accept %s\r\n", "Accept call"),
    HOLD_CALL("gsm hold %s\r\n", "Hold call"),
    CANCEL_CALL("gsm cancel %s\r\n", "Cancel call");

    /** The command to pass to the emulator console. */
    private String commandString;

    /** This is a human readable description of the command that will be used to be displayed in front of the users. */
    private String errorHumanReadableDescription;

    private EmulatorCommand(String commandString, String errorHumanReadableDescription) {
        this.commandString = commandString;
        this.errorHumanReadableDescription = errorHumanReadableDescription;
    }

    public String getCommandString() {
        return commandString;
    }

    public String getErrorHumanReadableDescription() {
        return errorHumanReadableDescription;
    }

}
