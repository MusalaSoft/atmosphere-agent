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
    PROPERTY_MANUFACTURER_NAME("ro.product.manufacturer");

    private String value;

    private DevicePropertyStringConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
