package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

/**
 * Thrown when the ATMOSPHERE service stop command fails.
 * 
 * @author yordan.petrov
 * 
 */
public class OnDeviceServiceTerminationException extends AtmosphereRuntimeException {
    private static final long serialVersionUID = -8414514520282900457L;

    public OnDeviceServiceTerminationException() {
    }

    public OnDeviceServiceTerminationException(String message) {
        super(message);
    }

    public OnDeviceServiceTerminationException(String message, Throwable inner) {
        super(message, inner);
    }
}
