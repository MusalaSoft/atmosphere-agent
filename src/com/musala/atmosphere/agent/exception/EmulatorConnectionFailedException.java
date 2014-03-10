package com.musala.atmosphere.agent.exception;

/**
 * Thrown when the connection to an emulator's console has failed for some reason.
 * 
 * @author georgi.gaydarov
 * 
 */
public class EmulatorConnectionFailedException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4107282942130158137L;

    public EmulatorConnectionFailedException() {

    }

    public EmulatorConnectionFailedException(String message) {
        super(message);
    }

    public EmulatorConnectionFailedException(String message, Throwable inner) {
        super(message, inner);
    }
}
