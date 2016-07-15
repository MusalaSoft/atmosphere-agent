package com.musala.atmosphere.agent.exception;

import com.musala.atmosphere.commons.exceptions.AtmosphereRuntimeException;

/**
 * Thrown when unable to resolve the correct entity implementation matching the given restrictions or could not find a
 * default one.
 *
 * @author filareta.yordanova
 *
 */
public class UnresolvedEntityTypeException extends AtmosphereRuntimeException {
    private static final long serialVersionUID = 4349519112412357251L;

    /**
     * Creates new {@link UnresolvedEntityTypeException UnresolvedEntityTypeException}.
     */
    public UnresolvedEntityTypeException() {
        super();
    }

    /**
     * Creates new {@link UnresolvedEntityTypeException UnresolvedEntityTypeException} with the given message.
     *
     * @param message
     *        - message representing the error that occurred
     */
    public UnresolvedEntityTypeException(String message) {
        super(message);
    }

    /**
     * Creates new {@link UnresolvedEntityTypeException UnresolvedEntityTypeException} with the given message and the
     * {@link Throwable cause} for the exception.
     *
     * @param message
     *        - message representing the error that occurred
     * @param throwable
     *        - the cause for the exception
     */
    public UnresolvedEntityTypeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
