package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

public class IllegalPortException extends AtmosphereRuntimeException {
    private static final long serialVersionUID = -5316605982125815278L;

    public IllegalPortException(String message) {
        super(message);
    }

    public IllegalPortException(String message, Throwable cause) {
        super(message, cause);
    }
}
