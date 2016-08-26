package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

/**
 * Thrown when a free port can not be found.
 * 
 * @author yordan.petrov
 * 
 */
public class NoFreePortAvailableException extends AtmosphereRuntimeException {
    private static final long serialVersionUID = 3921800536966534367L;

    public NoFreePortAvailableException() {
    }

    public NoFreePortAvailableException(String message) {
        super(message);
    }

    public NoFreePortAvailableException(String message, Throwable inner) {
        super(message, inner);
    }
}
