package com.musala.atmosphere.agent.devicewrapper;

import java.rmi.RemoteException;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.sa.BatteryState;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

public class RealWrapDevice extends AbstractWrapDevice
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8940498776944070469L;

	public RealWrapDevice(IDevice deviceToWrap) throws NotPossibleForDeviceException, RemoteException
	{
		super(deviceToWrap);

		if (deviceToWrap.isEmulator() == true)
		{
			throw new NotPossibleForDeviceException("Cannot create real wrap device for an emulator.");
		}
	}

	@Override
	public void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException
	{
		// TODO implement set network speed

	}

	@Override
	public void setBatteryLevel(int level) throws RemoteException
	{
		// TODO implement set battery level

	}

	@Override
	public void setNetworkLatency(int latency) throws RemoteException
	{
		// TODO implement set network latency

	}

	@Override
	public void setBatteryState(BatteryState state) throws RemoteException
	{
		// TODO implement set battery state

	}

}
