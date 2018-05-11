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
 * Contains the different states of the automatic device setup flags.
 * 
 * @author yordan.petrov
 * 
 */
public enum AutomaticDeviceSetupFlag {
    ON("on"),
    OFF("off"),
    ASK("ask"),
    FORCE_INSTALL("force install");

    private String value;

    private AutomaticDeviceSetupFlag(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static AutomaticDeviceSetupFlag getByValue(String value) {
        AutomaticDeviceSetupFlag flagByValue = null;
        for (AutomaticDeviceSetupFlag currentFlag : AutomaticDeviceSetupFlag.values()) {
            if (currentFlag.toString().equals(value)) {
                flagByValue = currentFlag;
                break;
            }
        }

        return flagByValue;
    }
}
