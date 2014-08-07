package com.musala.atmosphere.agent.devicewrapper;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.util.BatteryChangedIntentData;
import com.musala.atmosphere.commons.PowerProperties;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.beans.BatteryLevel;
import com.musala.atmosphere.commons.beans.BatteryState;
import com.musala.atmosphere.commons.beans.DeviceAcceleration;
import com.musala.atmosphere.commons.beans.DeviceMagneticField;
import com.musala.atmosphere.commons.beans.DeviceOrientation;
import com.musala.atmosphere.commons.beans.MobileDataState;
import com.musala.atmosphere.commons.beans.PhoneNumber;
import com.musala.atmosphere.commons.beans.PowerSource;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;
import com.musala.atmosphere.commons.util.IntentBuilder;
import com.musala.atmosphere.commons.util.IntentBuilder.IntentAction;
import com.musala.atmosphere.commons.util.Pair;

/**
 * Real (physical) device wrapper. Implements methods in a real device specific way.
 * 
 * @author georgi.gaydarov
 * 
 */
public class RealWrapDevice extends AbstractWrapDevice {
    /**
     * auto-generated serialization id
     */

    private static final long serialVersionUID = 8940498776944070469L;

    private final static Logger LOGGER = Logger.getLogger(RealWrapDevice.class.getCanonicalName());

    private final static int BATTERY_LEVEL_THRESHOLD = 15;

    public RealWrapDevice(IDevice deviceToWrap) throws NotPossibleForDeviceException, RemoteException {
        super(deviceToWrap);

        if (deviceToWrap.isEmulator()) {
            throw new NotPossibleForDeviceException("Cannot create real wrap device for an emulator.");
        }
    }

    @Override
    protected void setNetworkSpeed(Pair<Integer, Integer> speeds) throws CommandFailedException {
        // TODO implement set network speed
        throw new CommandFailedException("Not implemented yet!");

    }

    @Override
    protected void setPowerProperties(PowerProperties properties) throws CommandFailedException {
        PowerProperties initialEnvironment = serviceCommunicator.getPowerProperties();

        // BATTERY_CHANGED intent
        sendBatteryChangedIntent(initialEnvironment, properties);

        // POWER_CONNECTED and POWER_DISCONNECTED intent
        PowerSource newSource = properties.getPowerSource();
        if (newSource != PowerProperties.LEAVE_POWER_SOURCE_UNCHANGED) {
            PowerSource currentSource = initialEnvironment.getPowerSource();
            if (currentSource != newSource) {
                sendPowerStateIntent(newSource);
            }
        }

        // BATTERY_LOW and BATTERY_OKAY intent
        BatteryLevel newBatteryLevel = properties.getBatteryLevel();
        if (newBatteryLevel != PowerProperties.LEAVE_BATTERY_LEVEL_UNCHANGED) {
            sendBatteryLevelIntent(initialEnvironment, properties);
        }
    }

    private void sendBatteryChangedIntent(PowerProperties currentProperties, PowerProperties newProperties)
        throws CommandFailedException {
        BatteryChangedIntentData builder = new BatteryChangedIntentData();

        BatteryLevel batteryLevel = newProperties.getBatteryLevel();
        if (batteryLevel == PowerProperties.LEAVE_BATTERY_LEVEL_UNCHANGED) {
            batteryLevel = currentProperties.getBatteryLevel();
        }
        builder.setLevel(batteryLevel.getLevel());
        builder.setScale(1); // 1 scale => level is the actual value.

        BatteryState batteryState = newProperties.getBatteryState();
        if (batteryState == PowerProperties.LEAVE_BATTERY_STATE_UNCHANGED) {
            batteryState = currentProperties.getBatteryState();
        }
        builder.setState(batteryState.getStateId());

        PowerSource powerSource = currentProperties.getPowerSource();
        if (powerSource == PowerProperties.LEAVE_POWER_SOURCE_UNCHANGED) {
            powerSource = currentProperties.getPowerSource();
        }
        int powerSourceId = powerSource.getStateId();
        builder.setPlugged(powerSourceId);

        String intentCommand = builder.buildIntentQuery();
        shellCommandExecutor.execute(intentCommand);
    }

    private void sendPowerStateIntent(PowerSource source) throws CommandFailedException {
        IntentAction intentAction = source != PowerSource.UNPLUGGED ? IntentAction.ACTION_POWER_CONNECTED
                : IntentAction.ACTION_POWER_DISCONNECTED;
        IntentBuilder intentBuilder = new IntentBuilder(intentAction);
        String command = intentBuilder.buildIntentCommand();
        shellCommandExecutor.execute(command);
    }

    private void sendBatteryLevelIntent(PowerProperties currentPropertes, PowerProperties newProperties)
        throws CommandFailedException {
        int newBatteryLevel = newProperties.getBatteryLevel().getLevel();
        int currentBatteryLevel = currentPropertes.getBatteryLevel().getLevel();

        IntentBuilder intentBuilder = null;
        if (currentBatteryLevel > BATTERY_LEVEL_THRESHOLD && newBatteryLevel <= BATTERY_LEVEL_THRESHOLD) {
            intentBuilder = new IntentBuilder(IntentAction.BATTERY_LOW);
        } else if (currentBatteryLevel <= BATTERY_LEVEL_THRESHOLD && newBatteryLevel > BATTERY_LEVEL_THRESHOLD) {
            intentBuilder = new IntentBuilder(IntentAction.BATTERY_OKAY);
        }
        String command = intentBuilder.buildIntentCommand();
        shellCommandExecutor.execute(command);
    }

    @Override
    protected void setOrientation(DeviceOrientation deviceOrientation) throws CommandFailedException {
        // We can't set orientation on real device.
        throw new CommandFailedException("Can not set orientation on real devices.");
    }

    @Override
    protected void setAcceleration(DeviceAcceleration deviceAcceleration) throws CommandFailedException {
        // We can't set acceleration on real device.
        throw new CommandFailedException("Can not set acceleration on real devices.");
    }

    @Override
    protected void setMagneticField(DeviceMagneticField deviceMagneticField) throws CommandFailedException {
        // We can't set magnetic field on real device.
        throw new CommandFailedException("Can not set magnetic field on real devices.");
    }

    @Override
    protected void setMobileDataState(MobileDataState state) throws CommandFailedException {
        // We can't set mobile data state on real device.
        throw new CommandFailedException("Can not set mobile data state on real devices.");
    }

    @Override
    protected MobileDataState getMobileDataState() throws CommandFailedException {
        // We can't get mobile data state on real device.
        throw new CommandFailedException("Can not get mobile data state on real devices.");
    }

    @Override
    protected void receiveSms(SmsMessage smsMessage) throws CommandFailedException {
        // We can't simulate that a real device has received SMS.
        throw new CommandFailedException("Can not send SMS to real devices.");
    }

    @Override
    protected void receiveCall(PhoneNumber phoneNumber) throws CommandFailedException {
        // We can't simulate that a real device has received a call.
        throw new CommandFailedException("Can not send call to real devices.");
    }

    @Override
    protected void acceptCall(PhoneNumber phoneNumber) throws CommandFailedException {
        // We can't simulate that a real device accepted a certain call.
        throw new CommandFailedException("Can not accept certain call to real devices.");
    }

    @Override
    protected void holdCall(PhoneNumber phoneNumber) throws CommandFailedException {
        // We can't simulate that a real device has hold a call.
        throw new CommandFailedException("Can not hold call to real devices.");
    }

    @Override
    protected void cancelCall(PhoneNumber phoneNumber) throws CommandFailedException {
        // We can't simulate that a real device has canceled a certain call.
        throw new CommandFailedException("Can not cancel certain call to real devices.");
    }

    @Override
    protected void setProximity(float proximity) throws CommandFailedException {
        // We can't set the proximity on a real device.
        throw new CommandFailedException("Can not set the proximity on a real device.");

    }
}
