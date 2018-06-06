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
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests a buffer with the logCat log.
 * 
 * @author dimcho.nedev
 *
 */
public class BufferTest {
    private static final Logger LOGGER = Logger.getLogger(BufferTest.class.getCanonicalName());

    private static final int WAIT_UNTIL_CONDITION_TIMEOUT = 100;

    @Test(timeout = 10_000)
    public void variousTimeoutsAndSizesTest() throws InterruptedException {
        testBuffer(0, 10, 100_000);

        testBuffer(0, 0, 100_000);

        testBuffer(0, 0, 3000);

        testBuffer(10, 5, 35);

        testBuffer(10, 5, 35);

        testBuffer(0, 10, 35);

        testBuffer(10, 0, 35);

        testBuffer(0, 0, 2500);
    }

    /**
     * Tests a buffer with specific data size.
     *
     * @param add
     *        - the time between two consecutive additions
     * @param get
     *        - the time between two consecutive getBuffer operations
     * @param size
     *        - the size of the log
     * @throws InterruptedException
     *         - Thrown when a thread is interrupted
     */
    private void testBuffer(final int add, final int get, final int size) throws InterruptedException {
        List<String> expectedResult = new ArrayList<String>();
        List<String> actualResult = new ArrayList<String>();

        for (int i = 0; i < size; i++) {
            expectedResult.add(String.valueOf(i));
        }

        Buffer<String> buffer = new Buffer<String>(WAIT_UNTIL_CONDITION_TIMEOUT);

        // Adds values to the buffer
        new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < size; i++) {
                    buffer.addValue(String.valueOf(i));
                    try {
                        Thread.sleep(add);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        int sum = 0;

        while (true) {
            List<String> current = buffer.getBuffer();
            actualResult.addAll(current);
            final int currentSize = current.size();
            sum += currentSize;

            if (actualResult.size() == size) {
                LOGGER.debug(currentSize);
                break;
            }

            // LOGGER.debug(currentSize + "+");
            Assert.assertNotEquals("The current buffer size should not be zero", 0, currentSize);

            Thread.sleep(get);
        }

        Assert.assertEquals(expectedResult, actualResult);
        Assert.assertEquals("The buffer size is not as expected.", 0, buffer.size());
        Assert.assertEquals("The size of the result is not as expected..", sum, size);

        LOGGER.debug("sum: " + sum);
    }

}
