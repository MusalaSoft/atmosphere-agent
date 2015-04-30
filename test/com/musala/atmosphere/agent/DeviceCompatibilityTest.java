package com.musala.atmosphere.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

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

/**
 *
 * @author denis.bialev
 *
 */
public class DeviceCompatibilityTest {
    private static AgentManager agentManager;

    private static DeviceManager deviceManager;

    private static final int RMI_PORT = AgentPropertiesLoader.getAgentRmiPort();

    @BeforeClass
    public static void tearUp() throws Exception {
        String pathToAdb = AgentPropertiesLoader.getADBPath();
        AndroidDebugBridgeManager androidDebugBridgeManager = new AndroidDebugBridgeManager();
        androidDebugBridgeManager.setAndroidDebugBridgePath(pathToAdb);
        androidDebugBridgeManager.startAndroidDebugBridge();

        agentManager = new AgentManager(RMI_PORT);
        deviceManager = new DeviceManager(RMI_PORT);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (agentManager != null) {
            agentManager.close();
        }
    }

    @Test
    public void testRegisteringUncompatibleDevice() throws Exception {
        // FIXME: This is not really an unit test
        String mockDeviceSerialNumber = "UncompatibleDevice";
        boolean mockDeviceEmulator = false;
        String mockDeviceApiNotCompatibleApiVersion = "16";

        Map<String, String> mockPropMap = new HashMap<>();
        mockPropMap.put(DevicePropertyStringConstants.PROPERTY_API_LEVEL.toString(),
                        mockDeviceApiNotCompatibleApiVersion);

        IDevice mockDevice = mock(IDevice.class);
        when(mockDevice.getSerialNumber()).thenReturn(mockDeviceSerialNumber);
        when(mockDevice.isEmulator()).thenReturn(mockDeviceEmulator);
        when(mockDevice.arePropertiesSet()).thenReturn(true);
        when(mockDevice.getProperties()).thenReturn(mockPropMap);

        FakeOnDeviceComponentAnswer onDeviceAnswer = new FakeOnDeviceComponentAnswer();
        FakeDeviceShellAnswer shellAnswer = new FakeDeviceShellAnswer();
        Mockito.doAnswer(onDeviceAnswer).when(mockDevice).createForward(anyInt(), anyInt());
        Mockito.doAnswer(shellAnswer)
               .when(mockDevice)
               .executeShellCommand(Matchers.anyString(), Matchers.any(IShellOutputReceiver.class));
        Mockito.doAnswer(shellAnswer)
               .when(mockDevice)
               .executeShellCommand(Matchers.anyString(), Matchers.any(IShellOutputReceiver.class), anyInt());

        assertNull("Successfully registered device with API Level lower than 17.",
                   deviceManager.registerDevice(mockDevice));
    }

    @Test
    public void testRegisteringCompatibleDevice() throws Exception {
        // FIXME: This is not really an unit test
        String mockDeviceSerialNumber = "CompatibleDevice";
        boolean mockDeviceEmulator = false;
        String mockDeviceApiCompatibleApiVersion = "17";

        Map<String, String> mockPropMap = new HashMap<>();
        mockPropMap.put(DevicePropertyStringConstants.PROPERTY_API_LEVEL.toString(), mockDeviceApiCompatibleApiVersion);

        IDevice mockDevice = mock(IDevice.class);
        when(mockDevice.getSerialNumber()).thenReturn(mockDeviceSerialNumber);
        when(mockDevice.isEmulator()).thenReturn(mockDeviceEmulator);
        when(mockDevice.arePropertiesSet()).thenReturn(true);
        when(mockDevice.getProperties()).thenReturn(mockPropMap);

        FakeOnDeviceComponentAnswer onDeviceAnswer = new FakeOnDeviceComponentAnswer();
        FakeDeviceShellAnswer shellAnswer = new FakeDeviceShellAnswer();
        Mockito.doAnswer(onDeviceAnswer).when(mockDevice).createForward(anyInt(), anyInt());
        Mockito.doAnswer(shellAnswer)
               .when(mockDevice)
               .executeShellCommand(Matchers.anyString(), Matchers.any(IShellOutputReceiver.class));
        Mockito.doAnswer(shellAnswer)
               .when(mockDevice)
               .executeShellCommand(Matchers.anyString(), Matchers.any(IShellOutputReceiver.class), anyInt());

        assertEquals("The mocked device was not successfully wrapped.",
                     mockDevice.getSerialNumber(),
                     deviceManager.registerDevice(mockDevice));

        assertEquals("The mocked device is not present in Agent after it was registered.",
                     mockDevice,
                     deviceManager.getDeviceBySerialNumber(mockDeviceSerialNumber));
    }
}
