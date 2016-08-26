package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

/**
 * Thrown when an ATMOSPHERE on-device component start command fails.
 * 
 * @author yordan.petrov
 * 
 */
public class OnDeviceComponentStartingException extends AtmosphereRuntimeException {
    private static final long serialVersionUID = -6319875898380745721L;

    public OnDeviceComponentStartingException() {
    }

    public OnDeviceComponentStartingException(String message) {
        super(message);
    }

    public OnDeviceComponentStartingException(String message, Throwable inner) {
        super(message, inner);
    }
}
