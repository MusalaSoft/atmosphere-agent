package com.musala.atmosphere.agent.devicewrapper;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.util.FakeOnDeviceComponentAnswer;
import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.ConnectionType;
import com.musala.atmosphere.commons.DeviceAcceleration;
import com.musala.atmosphere.commons.DeviceOrientation;
import com.musala.atmosphere.commons.MobileDataState;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.util.Pair;

public class AbstractWrapDeviceTest
{
	private AbstractWrapDevice testWrapDevice;

	private IDevice device;

	private FakeOnDeviceComponentAnswer fakeOnDeviceComponentAnswer;

	@Before
	public void setUp() throws Exception
	{
		device = mock(IDevice.class);
		fakeOnDeviceComponentAnswer = new FakeOnDeviceComponentAnswer();
		Mockito.doAnswer(fakeOnDeviceComponentAnswer).when(device).createForward(anyInt(), anyInt());

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

			@Override
			public void receiveSms(SmsMessage smsMessage) throws CommandFailedException, RemoteException
			{

			}
		};

	}

	@Test
	public void testGetDeviceOrientation() throws RemoteException, CommandFailedException
	{
		testWrapDevice.getDeviceOrientation();
	}

	@Test
	public void testGetDeviceAcceleration() throws RemoteException, CommandFailedException
	{
		testWrapDevice.getDeviceAcceleration();
	}

	@Test
	public void testGetBatteryLevel() throws Throwable
	{
		testWrapDevice.getBatteryLevel();
	}

	@Test
	public void testSetWiFi() throws Throwable
	{
		testWrapDevice.setWiFi(true);

		testWrapDevice.setWiFi(false);
	}

	@Test
	public void testGetBatteryState() throws Throwable
	{
		testWrapDevice.getBatteryState();
	}

	@Test
	public void testGetConnectionType() throws Throwable
	{
		testWrapDevice.getConnectionType();
	}
}