package com.musala.atmosphere.agent.exception;

/**
 * Thrown when an on-device component installation fails.
 * 
 * @author yordan.petrov
 * 
 */
public class ComponentInstallationFailedException extends RuntimeException {

    private static final long serialVersionUID = -7253311280736757390L;

    public ComponentInstallationFailedException() {
    }

    public ComponentInstallationFailedException(String message) {
        super(message);
    }

    public ComponentInstallationFailedException(String message, Throwable inner) {
        super(message, inner);
    }
}
