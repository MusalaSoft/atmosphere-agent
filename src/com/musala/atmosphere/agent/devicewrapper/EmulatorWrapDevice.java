package com.musala.atmosphere.agent.devicewrapper;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.util.BackgroundShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.ExtendedEmulatorConsole;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.agent.exception.EmulatorConnectionFailedException;
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
import com.musala.atmosphere.commons.util.Pair;

/**
 * Device wrapper for emulators. Implements methods in an emulator-specific way.
 * 
 * @author georgi.gaydarov
 * 
 */
public class EmulatorWrapDevice extends AbstractWrapDevice {
    private static final long serialVersionUID = -112607818622127351L;

    private final static Logger LOGGER = Logger.getLogger(EmulatorWrapDevice.class.getCanonicalName());

    /**
     * Creates an wrapper of the given emulator {@link IDevice device}.
     * 
     * @param deviceToWrap
     *        - device to be wrapped
     * @param executor
     *        - an {@link ExecutorService} used for async tasks
     * @param shellCommandExecutor
     *        - an executor of shell commands for the wrapped device
     * @param serviceCommunicator
     *        - a communicator to the service component on the device
     * @param automatorCommunicator
     *        - a communicator to the UI automator component on the device
     * @throws RemoteException
     *         required when implementing {@link UnicastRemoteObject}
     */
    public EmulatorWrapDevice(IDevice deviceToWrap,
            ExecutorService executor,
            BackgroundShellCommandExecutor shellCommandExecutor,
            ServiceCommunicator serviceCommunicator,
            UIAutomatorCommunicator automatorCommunicator) throws NotPossibleForDeviceException, RemoteException {
        super(deviceToWrap, executor, shellCommandExecutor, serviceCommunicator, automatorCommunicator);

        if (!deviceToWrap.isEmulator()) {
            throw new NotPossibleForDeviceException("Cannot create emulator wrap device for a real, physical device.");
        }

        // By default, the magnetic field readings of an emulator is set to 0:0:0, which is an invalid value.
        // When the android method SensorManager.getRotationMatrix(...) in invoked with 0:0:0 as magnetic field
        // readings, it fails because of the invalidity of the data. This is why we set dummy data - so the on-device
        // service can function normally.

        try {

            ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();

            emulatorConsole.setMagneticField(new DeviceMagneticField(50.0f, 50.0f, 50.0f));

        } catch (CommandFailedException e) {

            LOGGER.warn("Connection to emulator console failed.", e);

        }
    }

    @Override
    protected void setNetworkSpeed(Pair<Integer, Integer> speeds) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.setNetworkSpeed(speeds.getKey(), speeds.getValue());
    }

    private void setBatteryLevel(BatteryLevel level) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.setBatteryLevel(level);
    }

    private void setBatteryState(BatteryState state) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.setBatteryState(state);
    }

    private void setPowerSource(PowerSource source) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.setPowerSource(source);
    }

    @Override
    protected void setPowerProperties(PowerProperties properties) throws CommandFailedException {
        BatteryLevel level = properties.getBatteryLevel();
        if (level != PowerProperties.LEAVE_BATTERY_LEVEL_UNCHANGED) {
            setBatteryLevel(level);
        }

        BatteryState state = properties.getBatteryState();
        if (state != PowerProperties.LEAVE_BATTERY_STATE_UNCHANGED) {
            setBatteryState(state);
        }

        PowerSource powerSource = properties.getPowerSource();
        if (powerSource != PowerProperties.LEAVE_POWER_SOURCE_UNCHANGED) {
            setPowerSource(powerSource);
        }
    }

    @Override
    protected void setOrientation(DeviceOrientation deviceOrientation) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.setOrientation(deviceOrientation);
    }

    @Override
    protected void setAcceleration(DeviceAcceleration deviceAcceleration) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.setAcceleration(deviceAcceleration);
    }

    @Override
    protected void setMagneticField(DeviceMagneticField deviceMagneticField) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.setMagneticField(deviceMagneticField);
    }

    @Override
    protected void setProximity(float proximity) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.setProximity(proximity);
    }

    @Override
    protected void setMobileDataState(MobileDataState state) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.setMobileDataState(state);
    }

    @Override
    protected MobileDataState getMobileDataState() throws CommandFailedException {
        try {
            ExtendedEmulatorConsole emulatorConsole = ExtendedEmulatorConsole.getExtendedEmulatorConsole(wrappedDevice);
            String response = emulatorConsole.getMobileDataState();
            String findStatusRegex = "gsm data state:\\s+(\\w+)";
            Pattern extractionPattern = Pattern.compile(findStatusRegex);
            Matcher regexMatch = extractionPattern.matcher(response);
            if (!regexMatch.find()) {
                throw new CommandFailedException("Getting mobile data state failed.");
            }
            String mobileDataState = regexMatch.group(1);
            return MobileDataState.valueOf(mobileDataState.toUpperCase());
        } catch (EmulatorConnectionFailedException e) {
            throw new CommandFailedException("Connection to the emulator console failed. "
                    + "See the enclosed exception for more information.", e);
        } catch (NotPossibleForDeviceException e) {
            throw new CommandFailedException("Illegal argument has been passed to the emulator console class. "
                    + "See the enclosed exception for more information.", e);
        }
    }

    @Override
    protected void receiveSms(SmsMessage smsMessage) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.receiveSms(smsMessage);
    }

    @Override
    protected void receiveCall(PhoneNumber phoneNumber) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.receiveCall(phoneNumber);
    }

    @Override
    protected void acceptCall(PhoneNumber phoneNumber) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.acceptCall(phoneNumber);
    }

    @Override
    protected void holdCall(PhoneNumber phoneNumber) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.holdCall(phoneNumber);
    }

    @Override
    protected void cancelCall(PhoneNumber phoneNumber) throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();
        emulatorConsole.cancelCall(phoneNumber);
    }

    /** Prepares an emulator console for usage. */
    private ExtendedEmulatorConsole prepareEmulatorConsole() throws CommandFailedException {
        ExtendedEmulatorConsole emulatorConsole = null;
        try {
            emulatorConsole = ExtendedEmulatorConsole.getExtendedEmulatorConsole(wrappedDevice);
        } catch (EmulatorConnectionFailedException e) {
            throw new CommandFailedException("Connection to the emulator console failed. "
                    + "See the enclosed exception for more information.", e);
        } catch (NotPossibleForDeviceException e) {
            // would not have gotten this far.
            e.printStackTrace();
        }
        return emulatorConsole;
    }
}
