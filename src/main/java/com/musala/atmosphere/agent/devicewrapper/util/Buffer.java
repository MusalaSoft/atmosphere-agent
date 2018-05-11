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

package com.musala.atmosphere.agent.devicewrapper.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A class that holds a {@link BufferProperties} buffer for a specified key.
 *
 * @author dimcho.nedev
 *
 */
public class Buffer<T, E> {
    private static final int THRESHOLD = 1024;

    private HashMap<T, BufferProperties<E>> logBuffer;

    public Buffer() {
        this.logBuffer = new HashMap<>();
    }

    /**
     * Gets the new arrival part of the buffer.
     *
     * @param key
     *            - the key with which a specified {@link BufferProperties} buffer is associated
     * @return the new arrival part of the buffer
     */
    public List<E> getBuffer(T key) {
        if (logBuffer.get(key) == null) {
            return new ArrayList<E>();
        }

        List<E> requestedBuffer = null;
        try {
            // waits until the buffer size is changed or the operation is terminated
            while (logBuffer.get(key).buffer.size() == logBuffer.get(key).startPosition) {
                if (this.logBuffer.get(key) == null) {
                    return new ArrayList<E>();
                }
            }

            List<E> buffer = logBuffer.get(key).buffer;
            synchronized (buffer) {
                BufferProperties<E> properties = logBuffer.get(key);
                int startPosition = properties.startPosition;
                int endPosition = properties.buffer.size();

                requestedBuffer = new ArrayList<E>(buffer.subList(startPosition, endPosition));

                properties.startPosition = endPosition;

                // clears the buffer and resets the start position
                if (buffer.size() >= THRESHOLD) {
                    logBuffer.get(key).buffer.clear();
                    logBuffer.get(key).startPosition = 0;
                }
            }
        } catch (NullPointerException e) {
            return new ArrayList<E>();
        }

        return requestedBuffer;
    }

    /**
     * Adds a buffer for a specified key.
     *
     * @param key
     *            - key with which a new {@link BufferProperties} buffer will be
     *            associated
     */
    public synchronized void addKey(T key) {
        logBuffer.put(key, new BufferProperties<E>());
    }

    /**
     * Adds a value to the {@link BufferProperties} buffer that is associated with specified key.
     *
     * @param key
     *            - key with which the specified value is to be associated
     * @param value
     *            - value to be associated with the specified key
     */
    public synchronized void addValue(T key, E value) {
        if (logBuffer.containsKey(key)) {
            logBuffer.get(key).buffer.add(value);
        }
    }

    /**
     * Removes the {@link BufferProperties}} value associated with a specified
     * key.
     *
     * @param key
     *            - the key with which a specified value is associated
     */
    public synchronized void remove(T key) {
        logBuffer.remove(key);
    }

    /**
     * Returns <code>true</code> if the buffer contains a mapping for the specified key.
     *
     * @param key
     *            - the key whose presence in this map is to be tested
     * @return <code>true</code> if the buffer contains a mapping for the specified key
     */
    public synchronized boolean contains(T key) {
        return logBuffer.get(key) != null;
    }

    /**
     * A private class that contains the properties for the buffer.
     *
     * @author dimcho.nedev
     *
     */
    private class BufferProperties<V> {
        public int startPosition = 0;

        public List<V> buffer;

        public BufferProperties() {
            this.buffer = Collections.synchronizedList(new ArrayList<V>());
        }

    }

}
