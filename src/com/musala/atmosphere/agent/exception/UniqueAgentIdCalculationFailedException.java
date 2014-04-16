package com.musala.atmosphere.agent.exception;

public class UniqueAgentIdCalculationFailedException extends RuntimeException {

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
