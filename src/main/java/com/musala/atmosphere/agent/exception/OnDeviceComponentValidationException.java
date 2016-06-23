package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

/**
 * Thrown when the validation of an ATMOSPHERE on-device component fails.
 * 
 * @author yordan.petrov
 * 
 */
public class OnDeviceComponentValidationException extends AtmosphereRuntimeException {
    private static final long serialVersionUID = -5184993117889537644L;

    public OnDeviceComponentValidationException() {
    }

    public OnDeviceComponentValidationException(String message) {
        super(message);
    }

    public OnDeviceComponentValidationException(String message, Throwable inner) {
        super(message, inner);
    }
}
