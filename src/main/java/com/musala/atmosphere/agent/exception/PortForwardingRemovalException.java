package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

/**
 * Thrown when the port forwarded to the ATMOSPHERE service port can not be freed.
 * 
 * @author yordan.petrov
 * 
 */
public class PortForwardingRemovalException extends AtmosphereRuntimeException {
    private static final long serialVersionUID = 1973383660575881546L;

    public PortForwardingRemovalException() {
    }

    public PortForwardingRemovalException(String message) {
        super(message);
    }

    public PortForwardingRemovalException(String message, Throwable inner) {
        super(message, inner);
    }
}
