package com.musala.atmosphere.agent.devicewrapper;

import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.util.EmulatorConnectionFailedException;
import com.musala.atmosphere.agent.devicewrapper.util.ExtendedEmulatorConsole;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.beans.BatteryState;
import com.musala.atmosphere.commons.beans.DeviceAcceleration;
import com.musala.atmosphere.commons.beans.DeviceOrientation;
import com.musala.atmosphere.commons.beans.MobileDataState;
import com.musala.atmosphere.commons.beans.PhoneNumber;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;
import com.musala.atmosphere.commons.util.Pair;

/**
 * Device wrapper for emulators. Implements methods in an emulator-specific way.
 *
 * @author georgi.gaydarov
 *
 */
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

		// By default, the magnetic field readings of an emulator is set to 0:0:0, which is an invalid value.
		// When the android method SensorManager.getRotationMatrix(...) in invoked with 0:0:0 as magnetic field
		// readings, it fails because of the invalidity of the data. This is why we set dummy data - so the on-device
		// service can function normally.
		try
		{
			ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
			emulatorConsole.setMagneticField(50, 50, 50);
		}
		catch (CommandFailedException e)
		{
			LOGGER.warn("Connection to emulator console failed.", e);
		}
	}

	@Override
	public void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException, CommandFailedException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.setNetworkSpeed(speeds.getKey(), speeds.getValue());
	}

	@Override
	public void setBatteryLevel(int level) throws RemoteException, CommandFailedException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.setBatteryLevel(level);
	}

	@Override
	public void setNetworkLatency(int latency) throws RemoteException
	{
		// TODO implement set network latency

	}

	@Override
	public void setBatteryState(BatteryState state) throws RemoteException, CommandFailedException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.setBatteryState(state);
	}

	@Override
	public void setPowerState(boolean state) throws RemoteException, CommandFailedException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.setPowerState(state);
	}

	@Override
	public void setDeviceOrientation(DeviceOrientation deviceOrientation)
		throws RemoteException,
			CommandFailedException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.setOrientation(deviceOrientation);
	}

	@Override
	public void setAcceleration(DeviceAcceleration deviceAcceleration) throws RemoteException, CommandFailedException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.setAcceleration(deviceAcceleration);
	}

	@Override
	public void setMobileDataState(MobileDataState state) throws CommandFailedException, RemoteException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.setMobileDataState(state);
	}

	@Override
	public MobileDataState getMobileDataState() throws CommandFailedException, RemoteException
	{
		try
		{
			ExtendedEmulatorConsole emulatorConsole = ExtendedEmulatorConsole.getExtendedEmulatorConsole(wrappedDevice);
			String response = emulatorConsole.getMobileDataState();
			String findStatusRegex = "gsm data state:\\s+(\\w+)";
			Pattern extractionPattern = Pattern.compile(findStatusRegex);
			Matcher regexMatch = extractionPattern.matcher(response);
			if (!regexMatch.find())
			{
				throw new CommandFailedException("Getting mobile data state failed.");
			}
			String mobileDataState = regexMatch.group(1);
			return MobileDataState.valueOf(mobileDataState.toUpperCase());
		}
		catch (EmulatorConnectionFailedException e)
		{
			throw new CommandFailedException("Connection to the emulator console failed. "
					+ "See the enclosed exception for more information.", e);
		}
		catch (NotPossibleForDeviceException e)
		{
			throw new CommandFailedException("Illegal argument has been passed to the emulator console class. "
					+ "See the enclosed exception for more information.", e);
		}
	}

	@Override
	public void receiveSms(SmsMessage smsMessage) throws CommandFailedException, RemoteException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.receiveSms(smsMessage);
	}

	@Override
	public void receiveCall(PhoneNumber phoneNumber) throws CommandFailedException, RemoteException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.receiveCall(phoneNumber);
	}

	@Override
	public void acceptCall(PhoneNumber phoneNumber) throws CommandFailedException, RemoteException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.acceptCall(phoneNumber);
	}

	@Override
	public void holdCall(PhoneNumber phoneNumber) throws CommandFailedException, RemoteException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.holdCall(phoneNumber);
	}

	@Override
	public void cancelCall(PhoneNumber phoneNumber) throws CommandFailedException, RemoteException
	{
		ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
		emulatorConsole.cancelCall(phoneNumber);
	}

	/** Prepares an emulator console for usage. */
	private ExtendedEmulatorConsole prepareEmulatorConsole() throws CommandFailedException
	{
		ExtendedEmulatorConsole emulatorConsole = null;
		try
		{
			emulatorConsole = ExtendedEmulatorConsole.getExtendedEmulatorConsole(wrappedDevice);
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
		return emulatorConsole;
	}
}
