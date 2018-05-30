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

package com.musala.atmosphere.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.agent.util.FakeDeviceShellAnswer;
import com.musala.atmosphere.agent.util.FakeOnDeviceComponentAnswer;
import com.musala.atmosphere.agent.util.FileRecycler;

/**
 *
 * @author denis.bialev
 *
 */
public class DeviceCompatibilityTest {
    private static AgentManager agentManager;

    private static DeviceManager deviceManager;

    private static FileRecycler fileRecycler;

    @BeforeClass
    public static void tearUp() throws Exception {
        String pathToAdb = AgentPropertiesLoader.getAdbPath();
        AndroidDebugBridgeManager androidDebugBridgeManager = new AndroidDebugBridgeManager();
        androidDebugBridgeManager.setAndroidDebugBridgePath(pathToAdb);
        androidDebugBridgeManager.startAndroidDebugBridge();

        fileRecycler = mock(FileRecycler.class);

        agentManager = new AgentManager(fileRecycler);
        deviceManager = new DeviceManager(fileRecycler);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (agentManager != null) {
            agentManager.close();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRegisteringUncompatibleDevice() throws Exception {
        // FIXME: This is not really an unit test
        String mockDeviceSerialNumber = "UncompatibleDevice";
        boolean mockDeviceEmulator = false;
        String mockDeviceApiNotCompatibleApiVersion = "16";

        IDevice mockDevice = mock(IDevice.class);
        when(mockDevice.getSerialNumber()).thenReturn(mockDeviceSerialNumber);
        when(mockDevice.isEmulator()).thenReturn(mockDeviceEmulator);
        when(mockDevice.arePropertiesSet()).thenReturn(true);

        // API level
        Future<String> mockApiVersionFuture = mock(Future.class);
        when(mockDevice.getSystemProperty(DevicePropertyStringConstants.PROPERTY_API_LEVEL.toString())).thenReturn(mockApiVersionFuture);
        when(mockApiVersionFuture.get()).thenReturn(mockDeviceApiNotCompatibleApiVersion);

        FakeOnDeviceComponentAnswer onDeviceAnswer = new FakeOnDeviceComponentAnswer();
        FakeDeviceShellAnswer shellAnswer = new FakeDeviceShellAnswer();
        Mockito.doAnswer(onDeviceAnswer).when(mockDevice).createForward(anyInt(), anyInt());
        Mockito.doAnswer(shellAnswer).when(mockDevice).executeShellCommand(Matchers.anyString(),
                                                                           Matchers.any(IShellOutputReceiver.class));
        Mockito.doAnswer(shellAnswer).when(mockDevice).executeShellCommand(Matchers.anyString(),
                                                                           Matchers.any(IShellOutputReceiver.class),
                                                                           anyInt(),
                                                                           Matchers.any(TimeUnit.class));

        assertNull("Successfully registered device with API Level lower than 17.",
                   deviceManager.registerDevice(mockDevice));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRegisteringCompatibleDevice() throws Exception {
        // FIXME: This is not really an unit test
        String mockDeviceSerialNumber = "CompatibleDevice";
        boolean mockDeviceEmulator = false;
        String mockDeviceApiCompatibleApiVersion = "17";

        IDevice mockDevice = mock(IDevice.class);
        when(mockDevice.getSerialNumber()).thenReturn(mockDeviceSerialNumber);
        when(mockDevice.getProperty(IDevice.PROP_BUILD_API_LEVEL)).thenReturn(mockDeviceApiCompatibleApiVersion);
        when(mockDevice.isEmulator()).thenReturn(mockDeviceEmulator);
        when(mockDevice.arePropertiesSet()).thenReturn(true);

        // API level
        Future<String> mockApiVersionFuture = mock(Future.class);
        when(mockDevice.getSystemProperty(DevicePropertyStringConstants.PROPERTY_API_LEVEL.toString())).thenReturn(mockApiVersionFuture);
        when(mockApiVersionFuture.get()).thenReturn(mockDeviceApiCompatibleApiVersion);

        FakeOnDeviceComponentAnswer onDeviceAnswer = new FakeOnDeviceComponentAnswer();
        FakeDeviceShellAnswer shellAnswer = new FakeDeviceShellAnswer();
        Mockito.doAnswer(onDeviceAnswer).when(mockDevice).createForward(anyInt(), anyInt());
        Mockito.doAnswer(shellAnswer).when(mockDevice).executeShellCommand(Matchers.anyString(),
                                                                           Matchers.any(IShellOutputReceiver.class));
        Mockito.doAnswer(shellAnswer).when(mockDevice).executeShellCommand(Matchers.anyString(),
                                                                           Matchers.any(IShellOutputReceiver.class),
                                                                           anyInt(),
                                                                           Matchers.any(TimeUnit.class));

        assertEquals("The mocked device was not successfully wrapped.",
                     mockDevice.getSerialNumber(),
                     deviceManager.registerDevice(mockDevice));

        assertEquals("The mocked device is not present in Agent after it was registered.",
                     mockDevice,
                     deviceManager.getDeviceBySerialNumber(mockDeviceSerialNumber));
    }
}
