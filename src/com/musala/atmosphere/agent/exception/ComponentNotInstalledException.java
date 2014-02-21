package com.musala.atmosphere.agent.exception;

/**
 * Thrown when an on-device component is not installed on the device.
 * 
 * @author yordan.petrov
 * 
 */
public class ComponentNotInstalledException extends RuntimeException {

    private static final long serialVersionUID = -7253311280736757390L;

    public ComponentNotInstalledException() {
    }

    public ComponentNotInstalledException(String message) {
        super(message);
    }

    public ComponentNotInstalledException(String message, Throwable inner) {
        super(message, inner);
    }
}
