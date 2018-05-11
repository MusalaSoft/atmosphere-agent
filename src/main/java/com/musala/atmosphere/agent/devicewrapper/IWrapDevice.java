// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.agent.devicewrapper;

import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Common interface for all device wrappers.
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
