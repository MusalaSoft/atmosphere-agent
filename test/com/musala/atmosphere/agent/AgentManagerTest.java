package com.musala.atmosphere.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.agent.util.FakeOnDeviceComponentAnswer;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.sa.DeviceParameters;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.SystemSpecification;

public class AgentManagerTest {
    private static AgentManager agentManager;

    private static final int RMI_PORT = AgentPropertiesLoader.getAgentRmiPort();

    private static final String PATH_TO_ADB = AgentPropertiesLoader.getADBPath();

    @BeforeClass
    public static void setUp() throws Exception {
        DdmPreferences.setLogLevel("warn");
        Log.setLogOutput(new DdmLibLogListener(Level.ALL, false /* do no log to a file */));

        agentManager = new AgentManager(PATH_TO_ADB, RMI_PORT);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (agentManager != null) {
            agentManager.close();
        }
    }

    @Test
    public void testGetAllDeviceWrappers() throws RemoteException {
        List<String> list = agentManager.getAllDeviceWrappers();
        assertNotNull("The devices information list should never be 'null'.", list);
    }

    @Test
    public void testGetDeviceInformationWithValidSerialNumber() throws Exception {
        String mockDeviceSerialNumber = "lol";
        boolean mockDeviceEmulator = false;
        Integer mockDeviceLcdDensity = 123;
        String mockDeviceModel = "hello";
        String mockDeviceOS = "mockos";
        String mockDeviceCpu = "megacpu";

        Map<String, String> mockPropMap = new HashMap<>();
        mockPropMap.put(DevicePropertyStringConstants.PROPERTY_REALDEVICE_LCD_DENSITY.toString(),
                        mockDeviceLcdDensity.toString());
        mockPropMap.put(DevicePropertyStringConstants.PROPERTY_PRODUCT_MODEL.toString(), mockDeviceModel);
        mockPropMap.put(DevicePropertyStringConstants.PROPERTY_OS_VERSION.toString(), mockDeviceOS);
        mockPropMap.put(DevicePropertyStringConstants.PROPERTY_CPU_TYPE.toString(), mockDeviceCpu);

        IDevice mockDevice = mock(IDevice.class);
        when(mockDevice.getSerialNumber()).thenReturn(mockDeviceSerialNumber);
        when(mockDevice.isEmulator()).thenReturn(mockDeviceEmulator);
        when(mockDevice.arePropertiesSet()).thenReturn(true);
        when(mockDevice.getProperties()).thenReturn(mockPropMap);

        Mockito.doAnswer(new FakeOnDeviceComponentAnswer()).when(mockDevice).createForward(anyInt(), anyInt());

        agentManager.registerDeviceOnAgent(mockDevice);

        Registry agentRegistry = LocateRegistry.getRegistry("localhost", RMI_PORT);
        IWrapDevice device = (IWrapDevice) agentRegistry.lookup(mockDeviceSerialNumber);

        DeviceInformation info = device.getDeviceInformation();
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
        assertEquals("Mock device creation / .getDeviceInformation() data mismatch. (device OS)",
                     info.getOS(),
                     mockDeviceOS);
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
    public void testGetUniqueAgentId() throws RemoteException {
        String agentId = agentManager.getAgentId();
        assertNotNull("Unique Agent ID can never be null", agentId);
    }

    @Test
    public void testGetAgentSpecifications() throws RemoteException {
        SystemSpecification systemSpecification = agentManager.getSpecification();
        assertNotNull("System specification should never be null", systemSpecification);
    }

    @Test
    public void testGetPerformanceScore() throws RemoteException {
        DeviceParameters requiredDeviceParameters = new DeviceParameters();
        requiredDeviceParameters.setRam(0);

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
