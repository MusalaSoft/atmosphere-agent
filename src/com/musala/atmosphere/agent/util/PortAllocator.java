package com.musala.atmosphere.agent.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.musala.atmosphere.agent.exception.NoFreePortAvailableException;

/**
 * Controls port allocation and keeps track of the currently allocated ports.
 * 
 * @author yordan.petrov
 * 
 */

public class PortAllocator {
    /**
     * TODO: This constants should be passed to the constructor of the object when a dependency injection mechanism is
     * introduced.
     */
    private static final int MIN_FORWARD_PORT = AgentPropertiesLoader.getADBMinForwardPort();

    private static final int MAX_FORWARD_PORT = AgentPropertiesLoader.getADBMaxForwardPort();

    private static final int MIN_TCP_PORT = 0;

    private static final int MAX_TCP_PORT = 65535;

    private static Stack<Integer> freePorts = new Stack<Integer>();

    private static Set<Integer> allocatedPorts = Collections.synchronizedSet(new HashSet<Integer>());

    static {
        for (int currentPort = MAX_FORWARD_PORT; currentPort >= MIN_FORWARD_PORT; currentPort--) {
            freePorts.add(currentPort);
        }
    }

    /**
     * Registers a port that has recently been freed by a testing device as released.
     * 
     * @param port
     *        - the free port identifier
     * @return <code>true</code> if free port was registered successfully as released and <code>false</code> otherwise
     */
    public boolean releasePort(int port) {
        if (allocatedPorts.remove(port)) {
            freePorts.add(port);
        }

        return true;
    }

    /**
     * Registers a port that has recently been freed by a testing device as released.
     * 
     * @param port
     *        - the free port identifier
     * @return <code>true</code> if free port was registered successfully and <code>false</code> otherwise
     */
    public synchronized boolean registerPort(int port) {
        if (!isPortValid(port)) {
            return false;
        }

        if (allocatedPorts.contains(port)) {
            return false;
        }

        if (!freePorts.contains(port)) {
            freePorts.add(port);
        }

        return true;
    }

    /**
     * Gets a free port identifier.
     * 
     * @return a free port identifier
     * @throws NoFreePortAvailableException
     *         if there isn't any port available for allocation
     */
    public synchronized int getPort() throws NoFreePortAvailableException {
        if (freePorts.isEmpty()) {
            throw new NoFreePortAvailableException("Colud not find a free port for allocation.");
        }

        int freePort = freePorts.pop();
        allocatedPorts.add(freePort);

        return freePort;
    }

    /**
     * Validate whether a given port number is valid TCP port number
     * 
     * @param portNumber
     *        - the port number that is being validated
     * @return <code>true</code> if the port number is valid TCP port and <code>false</code> otherwise
     */
    private boolean isPortValid(int portNumber) {
        return portNumber >= MIN_TCP_PORT && portNumber <= MAX_TCP_PORT;
    }
}
