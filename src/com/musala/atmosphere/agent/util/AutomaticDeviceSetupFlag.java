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
    ASK("ask");

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
