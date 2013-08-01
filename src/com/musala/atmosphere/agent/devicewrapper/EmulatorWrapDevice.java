package com.musala.atmosphere.agent.devicewrapper;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.util.EmulatorConnectionFailedException;
import com.musala.atmosphere.agent.devicewrapper.util.ExtendedEmulatorConsole;
import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

public class EmulatorWrapDevice extends AbstractWrapDevice
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -112607818622127351L;

	private final static Logger LOGGER = Logger.getLogger(EmulatorWrapDevice.class.getCanonicalName());

	public EmulatorWrapDevice(IDevice deviceToWrap) throws NotPossibleForDeviceException, RemoteException
	{
		super(deviceToWrap);

		if (deviceToWrap.isEmulator() == false)
		{
			throw new NotPossibleForDeviceException("Cannot create emulator wrap device for a real, physical device.");
		}
	}

	@Override
	public void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException, CommandFailedException
	{
		try
		{
			ExtendedEmulatorConsole emulatorConsole = ExtendedEmulatorConsole.getExtendedEmulatorConsole(wrappedDevice);
			boolean success = emulatorConsole.setNetoworkSpeed(speeds.getKey(), speeds.getValue());
			if (success == false)
			{
				LOGGER.error("FAIL: Setting network speed on '" + wrappedDevice.getSerialNumber() + "'.");
				throw new CommandFailedException("ExtendedEmulatorConsole method .setNetworkSpeed(...) "
						+ "failed for device with serial number '" + wrappedDevice.getSerialNumber() + "'.");
			}
		}
		catch (EmulatorConnectionFailedException e)
		{
			throw new CommandFailedException("Connection to the emulator console failed. "
					+ "See the enclosed exception for more information.", e);
		}
		catch (NotPossibleForDeviceException e)
		{
			// would not have gotten this far.
			e.printStackTrace();
		}
	}

	@Override
	public void setBatteryLevel(int level) throws RemoteException, CommandFailedException
	{
		try
		{
			ExtendedEmulatorConsole emulatorConsole = ExtendedEmulatorConsole.getExtendedEmulatorConsole(wrappedDevice);
			boolean success = emulatorConsole.setBatteryLevel(level);
			if (success == false)
			{
				LOGGER.error("FAIL: Setting battery level on '" + wrappedDevice.getSerialNumber() + "'.");
				throw new CommandFailedException("ExtendedEmulatorConsole method .setBatteryLevel(...) failed for device with serial number '"
						+ wrappedDevice.getSerialNumber() + "'.");
			}
		}
		catch (EmulatorConnectionFailedException e)
		{
			throw new CommandFailedException("Connection to the emulator console failed."
					+ "See the enclosed exception for more information.", e);
		}
		catch (IllegalArgumentException e)
		{
			throw new CommandFailedException("Illegal argument has been passed to the emulator console class. "
					+ "See the enclosed exception for more information.", e);
		}
		catch (NotPossibleForDeviceException e)
		{
			// Not really possible, as this is an EmulatorWrapDevice and if the
			// wrapped device was not an emulator, we
			// would not have gotten this far.
			e.printStackTrace();
		}

	}

	@Override
	public void setNetworkLatency(int latency) throws RemoteException
	{
		// TODO implement set network latency

	}

	@Override
	public void setBatteryState(BatteryState state) throws RemoteException, CommandFailedException
	{
		try
		{
			ExtendedEmulatorConsole emulatorConsole = ExtendedEmulatorConsole.getExtendedEmulatorConsole(wrappedDevice);
			boolean success = emulatorConsole.setBatteryState(state);
			if (success == false)
			{
				LOGGER.error("FAIL: Setting battery state on '" + wrappedDevice.getSerialNumber() + "'.");
				throw new CommandFailedException("ExtendedEmulatorConsole method .setBatteryState(...) "
						+ "failed for device with serial number '" + wrappedDevice.getSerialNumber() + "'.");
			}
		}
		catch (EmulatorConnectionFailedException e)
		{
			throw new CommandFailedException("Connection to the emulator console failed. "
					+ "See the enclosed exception for more information.", e);
		}
		catch (IllegalArgumentException e)
		{
			throw new CommandFailedException("Illegal argument has been passed to the emulator console class. "
					+ "See the enclosed exception for more information.", e);
		}
		catch (NotPossibleForDeviceException e)
		{
			// Not really possible, as this is an EmulatorWrapDevice and if the
			// wrapped device was not an emulator, we
			// would not have gotten this far.
			e.printStackTrace();
		}
	}

	@Override
	public void setPowerState(boolean state) throws RemoteException, CommandFailedException
	{
		try
		{
			ExtendedEmulatorConsole emulatorConsole = ExtendedEmulatorConsole.getExtendedEmulatorConsole(wrappedDevice);
			boolean success = emulatorConsole.setPowerState(state);
			if (success == false)
			{
				LOGGER.error("FAIL: Setting power state on '" + wrappedDevice.getSerialNumber() + "'.");
				throw new CommandFailedException("The setting of the power state failed for device with serial number '"
						+ wrappedDevice.getSerialNumber() + "'.");
			}
		}
		catch (EmulatorConnectionFailedException e)
		{
			throw new CommandFailedException("Connection to the emulator console failed. "
					+ "See the enclosed exception for more information.", e);
		}
		catch (IllegalArgumentException e)
		{
			throw new CommandFailedException("Illegal argument has been passed to the emulator console class. "
					+ "See the enclosed exception for more information.", e);
		}
		catch (NotPossibleForDeviceException e)
		{
			// Not really possible, as this is an EmulatorWrapDevice and if the
			// wrapped device was not an emulator, we
			// would not have gotten this far.
			e.printStackTrace();
		}

	}
}
