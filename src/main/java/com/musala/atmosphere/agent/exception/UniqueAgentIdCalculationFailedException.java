package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

public class UniqueAgentIdCalculationFailedException extends AtmosphereRuntimeException {

    private static final long serialVersionUID = -9086891181302017743L;

    public UniqueAgentIdCalculationFailedException() {
    }

    public UniqueAgentIdCalculationFailedException(String message) {
        super(message);
    }

    public UniqueAgentIdCalculationFailedException(String message, Throwable inner) {
        super(message, inner);
    }
}
