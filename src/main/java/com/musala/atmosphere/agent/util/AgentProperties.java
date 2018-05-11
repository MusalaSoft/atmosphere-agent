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
 * Enumeration class containing all possible agent properties.
 *
 * @author valyo.yolovski
 *
 */
public enum AgentProperties {
    ADB_CONNECTION_TIMEOUT("adb.connection.timeout"),
    EMULATOR_CREATION_WAIT_TIMEOUT("emulator.creation.wait.timeout"),
    EMULATOR_ABI("emulator.abi"),
    EMULATOR_EXECUTABLE_PATH("emulator.executable.path"),
    ANDROID_EXECUTABLE_PATH("android.executable.path"),
    COMMAND_EXECUTION_TIMEOUT("command.execution.timeout"),
    ADB_MIN_FORWARD_PORT("adb.min.forward.port"),
    ADB_MAX_FORWARD_PORT("adb.max.forward.port"),
    ON_DEVICE_COMPONENT_CONNECTION_RETRY_LIMIT("ondevicecomponent.connection.retry.limit"),
    DEVICE_AUTOMATIC_SETUP("device.automatic.setup"),
    ON_DEVICE_COMPONENT_FILES_PATH("ondevicecomponent.files.path"),
    SDK_DIR("sdk.dir"),
    FTP_SERVER("ftp.server"),
    CHROMEDRIVER_VERSION("chromedriver.version");

    private String value;

    private AgentProperties(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
};
