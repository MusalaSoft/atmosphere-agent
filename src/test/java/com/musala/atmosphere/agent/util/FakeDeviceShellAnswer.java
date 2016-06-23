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
