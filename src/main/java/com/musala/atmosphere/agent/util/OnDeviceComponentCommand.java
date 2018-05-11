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

package com.musala.atmosphere.agent.util;

/**
 * Contains command information related to the on-device component verification.
 * 
 * @author yordan.petrov
 * 
 */
public enum OnDeviceComponentCommand {
    IS_APK_INSTALLED("pm list packages %s", "package:%s\r\n"),
    SET_IME("ime set %s", "Input method %s selected\r\n"),
    IS_FILE_OR_FOLDER_EXISTING("ls %s", "%s\r\n");

    private String command;

    private String expectedResponse;

    private OnDeviceComponentCommand(String command, String expectedResponse) {
        this.command = command;
        this.expectedResponse = expectedResponse;
    }

    public String getCommand() {
        return command;
    }

    public String getExpectedResponse() {
        return expectedResponse;
    }
}
