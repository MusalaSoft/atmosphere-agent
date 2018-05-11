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

import java.util.LinkedList;
import java.util.List;

import com.musala.atmosphere.commons.sa.EmulatorParameters;

/**
 * A builder of commands for the emulator tool.
 * 
 * @author yordan.petrov
 * 
 */
public class EmulatorToolCommandBuilder {
    private static enum EmulatorToolCommandParameter {
        // TODO: Add all parameters.
        SYSDIR("-sysdir %s", "search for system disk images in <dir>"),
        AVD("-avd %s", "use a specific android virtual device"),
        MEMORY("-memory %d", "physical RAM size in MBs"),
        DPI_DEVICE("-dpi-device %d", " specify device's resolution in dpi (default 165)");

        private EmulatorToolCommandParameter(String format, String description) {
            this.format = format;
            this.description = description;
        }

        private String format;

        private String description;

        public String getFormat() {
            return format;
        }

        public String getDescription() {
            return description;
        }
    }

    private String emulatorName;

    private EmulatorParameters deviceParameters;

    public EmulatorToolCommandBuilder(String emulatorName, EmulatorParameters deviceParameters) {
        this.emulatorName = emulatorName;
        this.deviceParameters = deviceParameters;
    }

    /**
     * Returns a command used for starting emulator by the emulator tool.
     * 
     * @return a command used for starting emulator by the emulator tool.
     */
    public List<String> getStartCommand() {
        List<String> emulatorToolCommand = new LinkedList<String>();

        String avdParameter = String.format(EmulatorToolCommandParameter.AVD.getFormat(), emulatorName);
        emulatorToolCommand.add(avdParameter);

        Long ram = deviceParameters.getRam();
        if (ram != null) {
            String ramParameter = String.format(EmulatorToolCommandParameter.MEMORY.getFormat(), ram);
            emulatorToolCommand.add(ramParameter);
        }

        Integer dpi = deviceParameters.getDpi();
        if (dpi != null) {
            String dpiParameter = String.format(EmulatorToolCommandParameter.DPI_DEVICE.getFormat(), dpi);
            emulatorToolCommand.add(dpiParameter);
        }

        return emulatorToolCommand;
    }
}
