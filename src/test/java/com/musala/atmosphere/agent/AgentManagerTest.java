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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.Log;
import com.musala.atmosphere.agent.devicewrapper.IWrapDevice;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.agent.util.FakeDeviceShellAnswer;
import com.musala.atmosphere.agent.util.FakeOnDeviceComponentAnswer;
import com.musala.atmosphere.agent.util.FileRecycler;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.sa.EmulatorParameters;
import com.musala.atmosphere.commons.sa.SystemSpecification;

public class AgentManagerTest {
    private static AgentManager agentManager;

    private static DeviceManager deviceManager;

    private static FileRecycler fileRecycler;

    @BeforeClass
    public static void setUp() throws Exception {
        DdmPreferences.setLogLevel("warn");
        Log.setLogOutput(new DdmLibLogListener(Level.ALL, false /* do no log to a file */));

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
    public void testGetAllDeviceWrappers() {
        List<String> list = deviceManager.getAllDeviceSerialNumbers();
        assertNotNull("The devices information list should never be 'null'.", list);
    }

    @Test
    public void testGetDeviceInformationWithValidSerialNumber() throws Exception {
        // FIXME: I'm not really an unit test >:)
        String mockDeviceSerialNumber = "lol";
        boolean mockDeviceEmulator = false;
        Integer mockDeviceLcdDensity = 123;
        String mockDeviceModel = "hello";
        String mockDeviceApi = "19";
        String mockDeviceCpu = "megacpu";

        Map<String, String> mockPropMap = new HashMap<>();
        mockPropMap.put(DevicePropertyStringConstants.PROPERTY_REALDEVICE_LCD_DENSITY.toString(),
                        mockDeviceLcdDensity.toString());
        mockPropMap.put(DevicePropertyStringConstants.PROPERTY_PRODUCT_MODEL.toString(), mockDeviceModel);
        mockPropMap.put(DevicePropertyStringConstants.PROPERTY_API_LEVEL.toString(), mockDeviceApi);
        mockPropMap.put(DevicePropertyStringConstants.PROPERTY_CPU_TYPE.toString(), mockDeviceCpu);

        IDevice mockDevice = mock(IDevice.class);
        when(mockDevice.getSerialNumber()).thenReturn(mockDeviceSerialNumber);
        when(mockDevice.getProperty(IDevice.PROP_BUILD_API_LEVEL)).thenReturn(mockDeviceApi);
        when(mockDevice.isEmulator()).thenReturn(mockDeviceEmulator);
        when(mockDevice.arePropertiesSet()).thenReturn(true);
        when(mockDevice.getProperties()).thenReturn(mockPropMap);

        FakeOnDeviceComponentAnswer onDeviceAnswer = new FakeOnDeviceComponentAnswer();
        FakeDeviceShellAnswer shellAnswer = new FakeDeviceShellAnswer();
        Mockito.doAnswer(onDeviceAnswer).when(mockDevice).createForward(anyInt(), anyInt());
        Mockito.doAnswer(shellAnswer).when(mockDevice).executeShellCommand(Matchers.anyString(),
                                                                           Matchers.any(IShellOutputReceiver.class));
        Mockito.doAnswer(shellAnswer).when(mockDevice).executeShellCommand(Matchers.anyString(),
                                                                           Matchers.any(IShellOutputReceiver.class),
                                                                           anyInt());

        deviceManager.registerDevice(mockDevice);

        IWrapDevice device = deviceManager.getDeviceWrapperByDeviceId(mockDevice.getSerialNumber());

        // TODO getting device information fails for the not mocked data such as Ram and Camera.
        // It is because the service socket server is stopped after it has been validated. It should be fixed.
        DeviceInformation info = (DeviceInformation) device.route(RoutingAction.GET_DEVICE_INFORMATION);
        assertEquals("Mock device creation / .getDeviceInformation() data mismatch. (serial number)",
                     info.getSerialNumber(),
                     mockDeviceSerialNumber);
        assertEquals("Mock device creation / .getDeviceInformation() data mismatch. (isEmulator)",
                     info.isEmulator(),
                     mockDeviceEmulator);
        assertEquals("Mock device creation / .getDeviceInformation() data mismatch. (lcd density)",
                     info.getDpi(),
                     (int) mockDeviceLcdDensity);
        assertEquals("Mock device creation / .getDeviceInformation() data mismatch. (device model)",
                     info.getModel(),
                     mockDeviceModel);
        assertEquals("Mock device creation / .getDeviceInformation() data mismatch. (device Api)",
                     info.getApiLevel(),
                     Integer.parseInt(mockDeviceApi));
        assertEquals("Mock device creation / .getDeviceInformation() data mismatch. (device screen height)",
                     info.getResolution().getKey(),
                     DeviceInformation.FALLBACK_SCREEN_RESOLUTION.getKey());
        assertEquals("Mock device creation / .getDeviceInformation() data mismatch. (device screen width)",
                     info.getResolution().getValue(),
                     DeviceInformation.FALLBACK_SCREEN_RESOLUTION.getValue());
        assertEquals("Mock device creation / .getDeviceInformation() data mismatch. (device CPU identifier)",
                     info.getCpu(),
                     mockDeviceCpu);
    }

    @Test
    public void testGetUniqueAgentId() {
        String agentId = agentManager.getAgentId();
        assertNotNull("Unique Agent ID can never be null", agentId);
    }

    @Test
    public void testGetAgentSpecifications() {
        SystemSpecification systemSpecification = agentManager.getSpecification();
        assertNotNull("System specification should never be null", systemSpecification);
    }

    @Test
    public void testGetPerformanceScore() {
        EmulatorParameters requiredDeviceParameters = new EmulatorParameters();
        requiredDeviceParameters.setRam(0l);

        double performanceScore = agentManager.getPerformanceScore(requiredDeviceParameters);
        boolean isScorePositive = performanceScore > 0d;
        assertTrue("Performance score should not be 0 when the requested RAM memory is 0.", isScorePositive);

        SystemSpecification systemSpecification = agentManager.getSpecification();
        long totalSystemRam = systemSpecification.getTotalRam();

        requiredDeviceParameters.setRam(totalSystemRam + 1);

        performanceScore = agentManager.getPerformanceScore(requiredDeviceParameters);
        boolean isScoreNonpositive = performanceScore <= 0d;
        assertTrue("Performance score should not be positive when the requested RAM is higher than the free RAM memory on the agent.",
                   isScoreNonpositive);
    }
}
