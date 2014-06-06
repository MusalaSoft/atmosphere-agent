package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

/**
 * Thrown when an on-device component is not installed on the device.
 * 
 * @author yordan.petrov
 * 
 */
public class ComponentNotInstalledException extends AtmosphereRuntimeException {

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
