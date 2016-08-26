package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

/**
 * Thrown when initialization of the communication to an ATMOSPHERE on-device component fails.
 * 
 * @author yordan.petrov
 * 
 */
public class OnDeviceComponentInitializationException extends AtmosphereRuntimeException {
    private static final long serialVersionUID = -194901596410158197L;

    public OnDeviceComponentInitializationException() {
    }

    public OnDeviceComponentInitializationException(String message) {
        super(message);
    }

    public OnDeviceComponentInitializationException(String message, Throwable inner) {
        super(message, inner);
    }
}