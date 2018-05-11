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

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.android.ddmlib.IShellOutputReceiver;

/**
 * Takes care of possible device shell command requests.
 * 
 * @author georgi.gaydarov
 * 
 */
public class FakeDeviceShellAnswer implements Answer<Void> {

    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        String request = (String) args[0];
        IShellOutputReceiver output = (IShellOutputReceiver) args[1];

        switch (request) {
        // device is booted check
            case "getprop init.svc.bootanim":
                printToOutput(output, "stopped\r\n");
                break;
        }
        return null;
    }

    private final static void printToOutput(IShellOutputReceiver receiver, String message) {
        byte[] data = message.getBytes();
        receiver.addOutput(data, 0, data.length);
        receiver.flush();
    }
}
