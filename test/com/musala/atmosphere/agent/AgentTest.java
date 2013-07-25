package com.musala.atmosphere.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.sa.DeviceParameters;

public class AgentTest
{
	// we divide on 1000 to take the timeout in seconds
	private Integer MAX_EMULATOR_CREATION_TIMEOUT_IN_SECONDS = AgentPropertiesLoader.getEmulatorCreationWaitTimeout() / 1000;

	private Agent testedAgent = null;

	private Integer AGENT_PORT = 1990;

	@Before
	public void setUp() throws Exception
	{
		testedAgent = new Agent(AGENT_PORT);
		testedAgent.startAgentThread();
	}

	@After
	public void tearDown() throws Exception
	{
		if (testedAgent != null)
		{
			testedAgent.stop();
		}
	}

	@Test
	public void getAgentRmiPortTest()
	{
		assertTrue(	"Port of Agent doesn't match with the port we wanted to create Agent on.",
					AGENT_PORT == testedAgent.getAgentRmiPort());
	}

	@Test
	public void getAllDevicesSerialNumbersTest()
		throws RemoteException,
			NoSuchFieldException,
			SecurityException,
			IllegalArgumentException,
			IllegalAccessException
	{
		Field agentManagerField = testedAgent.getClass().getDeclaredField("agentManager");
		agentManagerField.setAccessible(true);
		AgentManager agentManagerOnAgent = (AgentManager) agentManagerField.get(testedAgent);

		final String mockDeviceSerialNumber = "mdsn";
		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.getSerialNumber()).thenReturn(mockDeviceSerialNumber);

		agentManagerOnAgent.registerDeviceOnAgent(mockDevice);

		boolean present = agentManagerOnAgent.isDevicePresent(mockDeviceSerialNumber);
		assertTrue("Device with serial number '" + mockDeviceSerialNumber
				+ "' should be present as we just registered it.", present);

		agentManagerOnAgent.unregisterDeviceOnAgent(mockDevice);
		present = agentManagerOnAgent.isDevicePresent(mockDeviceSerialNumber);
		assertFalse("Device with serial number '" + mockDeviceSerialNumber
				+ "' should not be present as we just unregistered it.", present);

		present = agentManagerOnAgent.isDevicePresent(null);
		assertFalse("Device with serial number null should not be present on agent", present);

		present = agentManagerOnAgent.isDevicePresent("");
		assertFalse("Device with serial number \"\" should not be present on agent", present);
	}

	@Test
	public void createAndStartEmulatorTest() throws IOException, InterruptedException
	{
		List<String> initialListOfDevices = testedAgent.getAllDevicesSerialNumbers();
		int initialNumberOfDevices = initialListOfDevices.size();
		int expectedNumberOfDevices = initialNumberOfDevices + 1;

		// create the DeviceParameters for the device that we will create
		DeviceParameters emulatorParameters = new DeviceParameters();
		emulatorParameters.setDpi(100);
		emulatorParameters.setRam(1024);
		emulatorParameters.setResolution(new Pair<Integer, Integer>(320, 240));
		emulatorParameters.setApiLevel(17);

		// create the emulator
		testedAgent.createAndStartEmulator(emulatorParameters);

		// FIXME vlado should check if this logic makes sense.
		for (int second = 0; second < MAX_EMULATOR_CREATION_TIMEOUT_IN_SECONDS; second++)
		{
			List<String> listOfDevices = testedAgent.getAllDevicesSerialNumbers();
			Thread.sleep(1000);
			if (listOfDevices.size() != initialNumberOfDevices)
			{
				assertEquals(	"Fail in getting number of devices or creating new emulators",
								expectedNumberOfDevices,
								testedAgent.getAllDevicesSerialNumbers().size());
				break;
			}
		}

		// remove the added emulator
		removeNewlyCreatedEmulators(initialListOfDevices, testedAgent.getAllDevicesSerialNumbers());
	}

	/**
	 * This function erases and closes all emulators that are created in some time interval. The user passes it as
	 * parameters the list of running devices on his machine before he start to create emulators, and the list of
	 * devices after he is done with his work. The procedure checks which devices are in the second list and not in the
	 * first, which means they have been created in the process of working and tries to remove them.
	 * 
	 * @param initialListOfDevices
	 *        - list of running devices <u>before</u> some agent manipulation ( in our case - before test execution )
	 * @param allDevicesSerialNumbers
	 *        - list of devices <u>after</u> some agent manipulation ( in our case - after the test was executed and
	 *        some emulators are created eventually )
	 */
	private void removeNewlyCreatedEmulators(List<String> initialListOfDevices, List<String> allDevicesSerialNumbers)
	{
		int newNumberOfDevices = allDevicesSerialNumbers.size();
		for (int indexOfDevice = newNumberOfDevices - 1; indexOfDevice >= 0; indexOfDevice--)
		{
			String currentDeviceId = allDevicesSerialNumbers.get(indexOfDevice);
			if (initialListOfDevices.contains(currentDeviceId) == false)
			{
				try
				{
					testedAgent.removeEmulatorBySerialNumber(currentDeviceId);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
