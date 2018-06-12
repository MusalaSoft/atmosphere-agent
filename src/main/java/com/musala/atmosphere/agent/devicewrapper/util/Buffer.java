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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A class that holds a buffer with a generic data.
 *
 * @author dimcho.nedev
 *
 */
public final class Buffer<T> {
    /**
     * The maximum size of the buffer.
     */
    private static final short THRESHOLD = 1024;

    /**
     * The minimum size that can be taken from the buffer.
     */
    private static final byte MIN_STEP = 4;

    private static final byte COUNT_DOWN_NUMBER = 1;

    /**
     * The time in milliseconds for wait until a certain condition.
     */
    private int waitUntilConditionTimeout = 4000;

    /**
     * The start position on the buffer.
     */
    private volatile int startPosition = 0;

    /**
     * The end position on the buffer.
     */
    private volatile int endPosition = 0;

    /**
     * Provides the synchronization.
     */
    private CountDownLatch bufferDone;

    /**
     * Contains the elements of the buffer.
     */
    private List<T> buffer;

    private boolean isActive = false;

    public Buffer() {
        this.buffer = Collections.synchronizedList(new ArrayList<T>());
        bufferDone = new CountDownLatch(COUNT_DOWN_NUMBER);
        isActive = true;
    }

    public Buffer(int waitUntilConditionTimeout) {
        this();
        this.waitUntilConditionTimeout = waitUntilConditionTimeout;
    }

    /**
     * Gets the new arrival part of the buffer.
     *
     * @return the new arrival part of the buffer
     */
    public List<T> getBuffer() {
        List<T> requestedBuffer = null;

        try {
            bufferDone.await(waitUntilConditionTimeout, TimeUnit.MILLISECONDS);

            synchronized (buffer) {
                endPosition = buffer.size();

                requestedBuffer = new ArrayList<T>(buffer.subList(startPosition, endPosition));

                startPosition = endPosition;

                // clears the buffer and resets the start position
                if (buffer.size() >= THRESHOLD) {
                    buffer.clear();
                    startPosition = 0;
                }
            }
        } catch (InterruptedException e) {
            return new ArrayList<T>();
        }

        return requestedBuffer;
    }

    /**
     * Adds a value to the buffer.
     *
     * @param value
     *        - the value
     */
    public void addValue(T value) {
        synchronized (buffer) {
            buffer.add(value);
            if (buffer.size() - startPosition >= MIN_STEP) {
                bufferDone.countDown();
                bufferDone = new CountDownLatch(COUNT_DOWN_NUMBER);
            }
        }
    }

    /**
     * Clears the buffer and resets the positions.
     */
    public synchronized void terminate() {
        buffer.clear();
        isActive = false;
        startPosition = 0;
        endPosition = 0;
    }

    /**
     * Gets the current size of the buffer
     *
     * @return size of the buffer
     */
    public synchronized int size() {
        return buffer.size() - startPosition;
    }

    /**
     * Checks whether the buffer is active.
     *
     * @return <code>true</code> if the buffer is active; <code>false</code> otherwise
     */
    public boolean isActive() {
        return isActive;
    }
}
