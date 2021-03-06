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

package com.musala.atmosphere.agent.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.musala.atmosphere.agent.exception.NoFreePortAvailableException;

/**
 * 
 * @author yordan.petrov
 * 
 */
public class PortAllocatorTest {

    private static final int NUMBER_OF_PORTS = 65535;

    private static final int INVALID_PORT_NUMBER = -1;

    private static PortAllocator testPortAllocator = new PortAllocator();

    private static List<Integer> obtainedPorts = new ArrayList<Integer>();

    @After
    public void tearDown() {
        for (int obtainedPort : obtainedPorts) {
            testPortAllocator.releasePort(obtainedPort);
        }
    }

    @Test
    public void testGetRegisteredPort() {
        int freePort = 69;

        boolean registerPortResult = testPortAllocator.registerPort(freePort);

        assertTrue("Registering a free port did not succeed.", registerPortResult);

        int recievedPort = foundPortNumber(freePort);

        assertEquals("The registered port was not obtained.", freePort, recievedPort);
    }

    @Test
    public void testGetReleasedPort() {
        int firstRecievedPort = testPortAllocator.getPort();
        testPortAllocator.releasePort(firstRecievedPort);

        int secondReceivedPort = foundPortNumber(firstRecievedPort);

        assertEquals("The released port was not obtained.", firstRecievedPort, secondReceivedPort);
    }

    @Test
    public void testIndicateFailureWhenRegisterAllocatedPort() {
        int obtainedPort = testPortAllocator.getPort();
        boolean registerPortResult = testPortAllocator.registerPort(obtainedPort);

        assertFalse("Registering a port that is currently allocated succeeded.", registerPortResult);
    }

    @Test
    public void testIndicateFailureWhenRegisterInvalidTCPPort() {
        boolean registerPortResult = testPortAllocator.registerPort(INVALID_PORT_NUMBER);

        assertFalse("Registering an invalid TCP port succeed.", registerPortResult);

        registerPortResult = testPortAllocator.registerPort(NUMBER_OF_PORTS + 1);

        assertFalse("Registering an invalid TCP port succeed.", registerPortResult);
    }

    @Test(expected = NoFreePortAvailableException.class)
    public void testThrowExceptionWhenExhaustingPortAllocator() {
        foundPortNumber(INVALID_PORT_NUMBER);
    }

    private int foundPortNumber(int port) {
        int obtainedPort = INVALID_PORT_NUMBER;

        for (int portIndex = 0; portIndex <= NUMBER_OF_PORTS; portIndex++) {
            obtainedPort = testPortAllocator.getPort();
            obtainedPorts.add(obtainedPort);

            if (obtainedPort == port) {
                break;
            }
        }

        return obtainedPort;
    }
}
