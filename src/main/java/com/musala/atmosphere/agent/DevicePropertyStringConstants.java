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

package com.musala.atmosphere.agent;

public enum DevicePropertyStringConstants {
    /**
     * Property key for the display density of an emulator.
     */
    PROPERTY_EMUDEVICE_LCD_DENSITY("qemu.sf.lcd_density"),

    /**
     * Property key for the display density of a real device.
     */
    PROPERTY_REALDEVICE_LCD_DENSITY("ro.sf.lcd_density"),

    /**
     * Property key for the product model of a device.
     */
    PROPERTY_PRODUCT_MODEL("ro.product.model"),

    /**
     * Property key for the operating system string of a device.
     */
    PROPERTY_OS_VERSION("ro.build.version.release"),

    /**
     * Property key for the CPU string of a device.
     */
    PROPERTY_CPU_TYPE("ro.product.cpu.abi"),

    /**
     * Property key for the API level string of a device.
     */
    PROPERTY_API_LEVEL("ro.build.version.sdk"),

    /**
     * Property key for the manufacturer string of a device.
     */
    PROPERTY_MANUFACTURER_NAME("ro.product.manufacturer"),

    /**
     * Property key for the characteristics string of a device.
     */
    PROPERTY_CHARACTERISTICS("ro.build.characteristics");

    private String value;

    private DevicePropertyStringConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
