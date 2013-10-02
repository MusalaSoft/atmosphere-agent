package com.musala.atmosphere.agent.devicewrapper;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Test;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.ConnectionType;
import com.musala.atmosphere.commons.DeviceAcceleration;
import com.musala.atmosphere.commons.DeviceOrientation;
import com.musala.atmosphere.commons.MobileDataState;
import com.musala.atmosphere.commons.util.Pair;

public class AbstractWrapDeviceTest
{
	private AbstractWrapDevice testWrapDevice;

	private IDevice device;

	private final String DUMPSYS_SENSORSERVICE_COMMAND_RESULT = "Sensor List:\r\nGoldfish 3-axis Accelerometer"
			+ "                   | The Android Open Source Project  | 0x00000000 | maxRate=   0.00Hz |"
			+ " last=<  0.0,  9.8,  0.0>\r\nGoldfish 3-axis Magnetic field sensor           |"
			+ " The Android Open Source Project  | 0x00000001 | maxRate=   0.00Hz |"
			+ " last=<  0.0,  0.0,  0.0>\r\nGoldfish Orientation sensor                     |"
			+ " The Android Open Source Project  | 0x00000002 | maxRate=   0.00Hz |"
			+ " last=< 97.1,-13.1, 55.9>\r\nGoldfish Temperature sensor                     |"
			+ " The Android Open Source Project  | 0x00000003 | maxRate=   0.00Hz |"
			+ " last=<  0.0,  0.0,  0.0>\r\nGoldfish Proximity sensor                       |"
			+ " The Android Open Source Project  | 0x00000004 | maxRate=   0.00Hz |"
			+ " last=<  0.0,  0.0,  0.0>\r\n9-axis fusion disabled (0 clients), gyro-rate=   0.00Hz,"
			+ " q=< 0, 0, 0, 0 > (0), b=< 0, 0, 0 >\r\n5 h/w sensors:\r\nhandle=0x00000000, active-count=1,"
			+ " rates(ms)={ 66.7 }, selected=66.7 ms\r\nhandle=0x00000001, active-count=0, rates(ms)={  },"
			+ " selected= 0.0 ms\r\nhandle=0x00000002, active-count=1, rates(ms)={ 200.0 }, selected=200.0 ms\r\n"
			+ "handle=0x00000003, active-count=0, rates(ms)={  }, selected= 0.0 ms\r\nhandle=0x00000004,"
			+ " active-count=0, rates(ms)={  }, selected= 0.0 ms\r\n2 active connections\r\nActive sensors:\r\n"
			+ "Goldfish 3-axis Accelerometer (handle=0x00000000, connections=1)\r\n"
			+ "Goldfish Orientation sensor (handle=0x00000002, connections=1)\r\n";

	@Before
	public void setUp() throws RemoteException
	{
		device = mock(IDevice.class);
		testWrapDevice = new AbstractWrapDevice(device)
		{

			@Override
			public void setAcceleration(DeviceAcceleration deviceAcceleration)
				throws CommandFailedException,
					RemoteException
			{

			}

			@Override
			public void setPowerState(boolean state) throws RemoteException, CommandFailedException
			{

			}

			@Override
			public void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException, CommandFailedException
			{

			}

			@Override
			public void setNetworkLatency(int latency) throws RemoteException
			{

			}

			@Override
			public void setDeviceOrientation(DeviceOrientation deviceOrientation)
				throws RemoteException,
					CommandFailedException
			{

			}

			@Override
			public void setBatteryState(BatteryState state) throws RemoteException, CommandFailedException
			{

			}

			@Override
			public void setBatteryLevel(int level) throws RemoteException, CommandFailedException
			{

			}

			@Override
			public void setMobileDataState(MobileDataState state) throws CommandFailedException, RemoteException
			{

			}

			@Override
			public ConnectionType getConnectionType() throws RemoteException, CommandFailedException
			{
				return null;
			}

			@Override
			public MobileDataState getMobileDataState() throws CommandFailedException, RemoteException
			{
				return null;
			}
		};

	}

	@Test
	public void testGetDeviceOrientation() throws RemoteException, CommandFailedException
	{
		AbstractWrapDevice spiedWrapDevice = spy(testWrapDevice);
		doReturn(DUMPSYS_SENSORSERVICE_COMMAND_RESULT).when(spiedWrapDevice).executeShellCommand(anyString());
		spiedWrapDevice.getDeviceOrientation();
	}

	@Test
	public void testGetDeviceAcceleration() throws RemoteException, CommandFailedException
	{
		AbstractWrapDevice spiedWrapDevice = spy(testWrapDevice);
		doReturn(DUMPSYS_SENSORSERVICE_COMMAND_RESULT).when(spiedWrapDevice).executeShellCommand(anyString());
		spiedWrapDevice.getDeviceAcceleration();
	}
}
