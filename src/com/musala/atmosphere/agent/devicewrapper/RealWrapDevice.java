package com.musala.atmosphere.agent.devicewrapper;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.util.BatteryChangedIntentData;
import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.DeviceAcceleration;
import com.musala.atmosphere.commons.DeviceOrientation;
import com.musala.atmosphere.commons.Pair;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

public class RealWrapDevice extends AbstractWrapDevice
{
	/**
	 * 
	 */

	private static final long serialVersionUID = 8940498776944070469L;

	private final static Logger LOGGER = Logger.getLogger(RealWrapDevice.class.getCanonicalName());

	private final static int BATTERY_LEVEL_THRESHOLD = 15;

	private int batteryLevel;

	private int batteryState;

	private boolean powerState;

	private static boolean batteryHasBeenLow;

	public RealWrapDevice(IDevice deviceToWrap) throws NotPossibleForDeviceException, RemoteException
	{
		super(deviceToWrap);

		if (deviceToWrap.isEmulator())
		{
			throw new NotPossibleForDeviceException("Cannot create real wrap device for an emulator.");
		}

		try
		{
			batteryLevel = super.getBatteryLevel();
			batteryState = super.getBatteryState().getStateId();
			powerState = super.getPowerState();
		}
		catch (CommandFailedException e)
		{
			// If the device is offline, the following invocations will fail as well. The unresponsive device state will
			// be handled there. Assuming the battery is full, so BATTERY_LOW will not be left unsent when the battery
			// is set to be low.
			LOGGER.error("Initial battery level fetching failed.", e);
			batteryLevel = 100;
		}

		batteryHasBeenLow = (batteryLevel <= BATTERY_LEVEL_THRESHOLD)
				&& (batteryState != BatteryState.UNKNOWN.getStateId()) && !powerState;
	}

	@Override
	public void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException
	{
		// TODO implement set network speed

	}

	@Override
	public void setBatteryLevel(int level) throws RemoteException, CommandFailedException
	{
		batteryLevel = level;
		BatteryChangedIntentData data = new BatteryChangedIntentData();
		data.setLevel(batteryLevel);
		data.setState(batteryState);
		String batteryChangedIntentQuery = buildBatteryChangedIntentQuery();

		executeShellCommand(batteryChangedIntentQuery);

		// Check to see whether other intents have to be sent.

		if (batteryLevel >= BATTERY_LEVEL_THRESHOLD)
		{
			if (batteryHasBeenLow)
			{
				// If the battery level is more than the low battery level threshold
				// and the battery level has already been below that threshold, a BATTERY_OKAY intent has to be sent.
				batteryHasBeenLow = false;

				executeShellCommand("am broadcast -a android.intent.action.BATTERY_OKAY"); // Broadcasting
																							// BATTERY_OKAY
																							// intent.
			}
			else
			{
				// If the battery level is above the low battery level threshold and the battery level
				// has not been below that threshold, then there is no need to broadcast intents.
			}
		}
		else
		{
			if (batteryHasBeenLow)
			{
				// If the battery level is set below the battery level threshold and before that the battery level
				// has been below the low battery level threshold, then there is no need to broadcast BATTERY_LOW intent
				// again.
			}
			else
			{
				// If the battery is set below the low battery level threshold and the battery has been above that
				// threshold before that a BATTERY_LOW intent should be broadcasted.
				batteryHasBeenLow = true;

				executeShellCommand("am broadcast -a android.intent.action.BATTERY_LOW"); // Broadcasting
																							// BATTERY_LOW
																							// intent.

			}
		}
	}

	@Override
	public void setNetworkLatency(int latency) throws RemoteException
	{
		// TODO implement set network latency

	}

	private String buildBatteryChangedIntentQuery()
	{
		BatteryChangedIntentData data = new BatteryChangedIntentData();
		data.setLevel(batteryLevel);
		data.setState(batteryState);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("am broadcast -a android.intent.action.BATTERY_CHANGED");

		if (data.getLaunchFlags() != null)
		{
			queryBuilder.append(" -f " + data.getLaunchFlags());
		}
		if (data.getIcon_small() != null)
		{
			queryBuilder.append(" --ei icon-small " + data.getIcon_small());
		}
		if (data.getPresent() != null)
		{
			queryBuilder.append(" --ez present " + data.getPresent());
		}
		if (data.getScale() != null)
		{
			queryBuilder.append(" --ei scale " + data.getScale());
		}
		if (data.getLevel() != null)
		{
			queryBuilder.append(" --ei level " + data.getLevel());
		}
		if (data.getTechnology() != null)
		{
			queryBuilder.append(" --es technology " + data.getTechnology());
		}
		if (data.getState() != null)
		{
			queryBuilder.append(" --ei status " + data.getState());
		}
		if (data.getVoltage() != null)
		{
			queryBuilder.append(" -- ei voltage " + data.getVoltage());
		}
		if (data.getInvalid_charger() != null)
		{
			queryBuilder.append(" --ei invalid_charger " + data.getInvalid_charger());
		}
		if (data.getPlugged() != null)
		{
			queryBuilder.append(" --ei pluuged " + data.getPlugged());
		}
		if (data.getHealth() != null)
		{
			queryBuilder.append(" --ei health " + data.getHealth());
		}
		if (data.getTemperature() != null)
		{
			queryBuilder.append(" --ei temperature " + data.getTemperature());
		}
		String query = queryBuilder.toString();
		return query;

	}

	@Override
	public void setBatteryState(BatteryState state) throws RemoteException, CommandFailedException
	{
		batteryState = state.getStateId();
		String batteryChangedIntentQuery = buildBatteryChangedIntentQuery();
		executeShellCommand(batteryChangedIntentQuery);
	}

	@Override
	public void setPowerState(boolean state) throws RemoteException, CommandFailedException
	{
		String powerState;
		if (state)
		{
			powerState = "ACTION_POWER_CONNECTED";
		}
		else
		{
			powerState = "ACTION_POWER_DISCONNECTED";
		}
		executeShellCommand("am broadcast -a android.intent.action." + powerState);
	}

	@Override
	public void setOrientation(DeviceOrientation deviceOrientation) throws RemoteException, CommandFailedException
	{
		// We can't set device orientation on real device
		throw new CommandFailedException("Can not set device orientation on real devices.");
	}

	@Override
	public void setAcceleration(DeviceAcceleration deviceAcceleration) throws RemoteException, CommandFailedException
	{
		// We can't set device acceleration on real device
		throw new CommandFailedException("Can not set device acceleration on real devices.");
	}
}
