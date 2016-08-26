package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

/**
 * Thrown when the communication to an ATMOSPHERE on-device component fails.
 * 
 * @author yordan.petrov
 * 
 */
public class OnDeviceComponentCommunicationException extends AtmosphereRuntimeException {
    private static final long serialVersionUID = 1310592628758559355L;

    public OnDeviceComponentCommunicationException() {
    }

    public OnDeviceComponentCommunicationException(String message) {
        super(message);
    }

    public OnDeviceComponentCommunicationException(String message, Throwable inner) {
        super(message, inner);
    }
}
