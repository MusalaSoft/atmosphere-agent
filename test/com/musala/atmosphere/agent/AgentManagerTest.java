package com.musala.atmosphere.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;
import com.musala.atmosphere.commons.sa.DeviceInformation;
import com.musala.atmosphere.commons.sa.exceptions.DeviceNotFoundException;

public class AgentManagerTest
{
	private AgentManager agentManager;

	@Before
	public void setUp() throws Exception
	{
		System.out.println();

		// TODO extract to config file
		DdmPreferences.setLogLevel("warn");
		Log.setLogOutput(new DdmLibLogListener(Level.ALL, false /* do no log to a file */));

		// TODO Extract to config file
		agentManager = new AgentManager("C:\\Android Development Tools\\sdk\\platform-tools\\adb", 1999);
	}

	@After
	public void tearDown() throws Exception
	{
		if (agentManager != null)
		{
			agentManager.close();
		}
	}

	@Test
	public void testGetAllDevicesInformation() throws RemoteException
	{
		List<DeviceInformation> list = agentManager.getAllDevicesInformation();
		assertNotNull("The devices information list should never be 'null'.", list);
	}

	@Test
	public void testIsDevicePresentWithNullAndEmpty() throws RemoteException
	{
		boolean present = agentManager.isDevicePresent(null);
		assertFalse("Device with serial number 'null' should never be present.", present);

		present = agentManager.isDevicePresent("");
		assertFalse("Device with empty serial number should never be present.", present);
	}

	@Test
	public void testIsDevicePresentWithValidSerialNumber() throws RemoteException
	{
		final String mockDeviceSerialNumber = "lol";
		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.getSerialNumber()).thenReturn(mockDeviceSerialNumber);

		agentManager.registerDeviceOnAgent(mockDevice);
		boolean present = agentManager.isDevicePresent(mockDeviceSerialNumber);
		assertTrue("Device with serial number '" + mockDeviceSerialNumber
				+ "' should be present as we just registered it.", present);

		agentManager.unregisterDeviceOnAgent(mockDevice);
		present = agentManager.isDevicePresent(mockDeviceSerialNumber);
		assertFalse("Device with serial number '" + mockDeviceSerialNumber
				+ "' should not be present as we just unregistered it.", present);
	}

	@Test(expected = DeviceNotFoundException.class)
	public void testGetDeviceInformationWithNull() throws RemoteException, DeviceNotFoundException
	{
		@SuppressWarnings("unused")
		DeviceInformation info = agentManager.getDeviceInformation(null);
		fail("DeviceNotFoundException should have been thrown.");
	}

	@Test(expected = DeviceNotFoundException.class)
	public void testGetDeviceInformationWithEmpty() throws RemoteException, DeviceNotFoundException
	{
		@SuppressWarnings("unused")
		DeviceInformation info = agentManager.getDeviceInformation("");
		fail("DeviceNotFoundException should have been thrown.");
	}

	@Test
	public void testGetDeviceInformationWithValidSerialNumber() throws RemoteException, DeviceNotFoundException
	{
		String mockDeviceSerialNumber = "lol";
		boolean mockDeviceEmulator = false;
		Integer mockDeviceLcdDensity = 123;
		String mockDeviceModel = "hello";
		String mockDeviceOS = "mockos";
		Integer mockDeviceRam = 123; // mb
		/*
		 * Integer mockDeviceScreenH = 123; Integer mockDeviceScreenW = 22; String mockScreenCmdResponse =
		 * "mUnrestrictedScreen " + mockDeviceScreenW + "x" + mockDeviceScreenH + "\n";
		 */

		Map<String, String> mockPropMap = new HashMap<>();
		mockPropMap.put(DevicePropertyStringConstants.PROPERTY_REALDEVICE_LCD_DENSITY.toString(),
						mockDeviceLcdDensity.toString());
		mockPropMap.put(DevicePropertyStringConstants.PROPERTY_PRODUCT_MODEL.toString(), mockDeviceModel);
		mockPropMap.put(DevicePropertyStringConstants.PROPERTY_OS_VERSION.toString(), mockDeviceOS);
		mockPropMap.put(DevicePropertyStringConstants.PROPERTY_REALDEVICE_RAM.toString(), mockDeviceRam.toString()
				+ " mb");

		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.getSerialNumber()).thenReturn(mockDeviceSerialNumber);
		when(mockDevice.isEmulator()).thenReturn(mockDeviceEmulator);
		when(mockDevice.arePropertiesSet()).thenReturn(true);
		when(mockDevice.getProperties()).thenReturn(mockPropMap);

		agentManager.registerDeviceOnAgent(mockDevice);

		DeviceInformation info = agentManager.getDeviceInformation(mockDeviceSerialNumber);
		assertEquals(	"Mock device creation / .getDeviceInformation() data mismatch. (serial number)",
						info.getSerialNumber(),
						mockDeviceSerialNumber);
		assertEquals(	"Mock device creation / .getDeviceInformation() data mismatch. (isEmulator)",
						info.isEmulator(),
						mockDeviceEmulator);
		assertEquals(	"Mock device creation / .getDeviceInformation() data mismatch. (lcd density)",
						info.getDpi(),
						(int) mockDeviceLcdDensity);
		assertEquals(	"Mock device creation / .getDeviceInformation() data mismatch. (device model)",
						info.getModel(),
						mockDeviceModel);
		assertEquals(	"Mock device creation / .getDeviceInformation() data mismatch. (device OS)",
						info.getOS(),
						mockDeviceOS);
		assertEquals(	"Mock device creation / .getDeviceInformation() data mismatch. (device RAM)",
						info.getRam(),
						(int) mockDeviceRam);
		assertEquals(	"Mock device creation / .getDeviceInformation() data mismatch. (device screen height)",
						info.getResolution().getKey(),
						DeviceInformation.FALLBACK_SCREEN_RESOLUTION.getKey());
		assertEquals(	"Mock device creation / .getDeviceInformation() data mismatch. (device screen width)",
						info.getResolution().getValue(),
						DeviceInformation.FALLBACK_SCREEN_RESOLUTION.getValue());

	}
}
