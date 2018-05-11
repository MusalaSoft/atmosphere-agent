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
