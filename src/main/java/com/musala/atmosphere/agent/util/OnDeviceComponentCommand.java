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
