package com.musala.atmosphere.agent.devicewrapper;

import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * <p>
 * Common interface for all device wrappers, used in RMI.
 * </p>
 *
 * @author georgi.gaydarov
 *
 */
public interface IWrapDevice {
    /**
     * Requests an action invocation on the device wrapper.
     *
     * @param action
     *        - a {@link RoutingAction} instance that specifies the action to be invoked.
     * @param args
     *        - the action parameters (if required).
     * @return the result from the action invocation.
     * @throws CommandFailedException
     *         thrown when a command failed
     */
    public Object route(RoutingAction action, Object... args) throws CommandFailedException;

    /**
     * Stops all ATMOSPHERE on-device components and releases the allocated ports.
     * 
     */
    public void unbindWrapper();
}
