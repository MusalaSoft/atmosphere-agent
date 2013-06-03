package com.musala.atmosphere.agent.devicewrapper;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.AgentManager;
import com.musala.atmosphere.agent.devicewrapper.util.EmulatorConnectionFailedException;
import com.musala.atmosphere.agent.devicewrapper.util.ExtendedEmulatorConsole;
import com.musala.atmosphere.commons.sa.BatteryState;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;
import com.musala.atmosphere.commons.sa.util.Pair;

public class EmulatorWrapDevice extends AbstractWrapDevice
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -112607818622127351L;

	private static final String LOG_FILENAME = "emulatorwrapdevice.log";

	private static boolean logFileSet = false;

	private final static Logger LOGGER = Logger.getLogger(AgentManager.class.getName());

	public EmulatorWrapDevice(IDevice deviceToWrap) throws NotPossibleForDeviceException, RemoteException
	{
		super(deviceToWrap);

		if (logFileSet == false)
		{
			// Set up the logger
			try
			{
				Handler fileHandler = new FileHandler(LOG_FILENAME);
				LOGGER.addHandler(fileHandler);
				logFileSet = true;
				LOGGER.setLevel(Level.ALL);
			}
			catch (SecurityException | IOException e)
			{
				// Could not create the log file.
				// Well, we can't log this...
				e.printStackTrace();
			}
		}

		if (deviceToWrap.isEmulator() == false)
		{
			throw new NotPossibleForDeviceException("Cannot create emulator wrap device for a real, physical device.");
		}
	}

	@Override
	public void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException
	{
		try
		{
			ExtendedEmulatorConsole emulatorConsole = ExtendedEmulatorConsole.getExtendedEmulatorConsole(wrappedDevice);
			boolean success = emulatorConsole.setNetoworkSpeed(speeds.getKey(), speeds.getValue());
			if (success == false)
			{
				LOGGER.log(	Level.WARNING,
							"ExtendedEmulatorConsole method .setNetworkSpeed(...) failed for device with serial number '"
									+ wrappedDevice.getSerialNumber() + "'.");
			}
		}
		catch (EmulatorConnectionFailedException e)
		{
			throw new RemoteException(	"Connection to the emulator console failed. See the enclosed exception for more information.",
										e);
		}
		catch (NotPossibleForDeviceException e)
		{
			// Not really possible, as this is an EmulatorWrapDevice and if the wrapped device was not an emulator, we
			// would not have gotten this far.
			e.printStackTrace();
		}
	}

	@Override
	public void setBatteryLevel(int level) throws RemoteException
	{
		try
		{
			ExtendedEmulatorConsole emulatorConsole = ExtendedEmulatorConsole.getExtendedEmulatorConsole(wrappedDevice);
			boolean success = emulatorConsole.setBatteryLevel(level);
			if (success == false)
			{
				LOGGER.log(	Level.WARNING,
							"ExtendedEmulatorConsole method .setBatteryLevel(...) failed for device with serial number '"
									+ wrappedDevice.getSerialNumber() + "'.");
			}
		}
		catch (EmulatorConnectionFailedException e)
		{
			throw new RemoteException(	"Connection to the emulator console failed. See the enclosed exception for more information.",
										e);
		}
		catch (IllegalArgumentException e)
		{
			throw new RemoteException(	"Illegal argument has been passed to the emulator console class. See the enclosed exception for more information.",
										e);
		}
		catch (NotPossibleForDeviceException e)
		{
			// Not really possible, as this is an EmulatorWrapDevice and if the wrapped device was not an emulator, we
			// would not have gotten this far.
			e.printStackTrace();
		}

	}

	@Override
	public int getNetworkLatency() throws RemoteException
	{
		// TODO implement get network latency
		return 0;
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
