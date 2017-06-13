package com.musala.atmosphere.agent.devicewrapper;

import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriverService;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.util.BackgroundShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.ExtendedEmulatorConsole;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.agent.exception.EmulatorConnectionFailedException;
import com.musala.atmosphere.agent.util.FileRecycler;
import com.musala.atmosphere.agent.util.FtpFileTransferService;
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
    private final static Logger LOGGER = Logger.getLogger(EmulatorWrapDevice.class.getCanonicalName());

    private final static int SET_MAGNETIC_FIELD_MAXIMUM_API_LEVEL_SUPPORT = 18;

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
     * @param fileRecycler
     *        - responsible for removing obsolete files
     * @param chromeDriverService
     *        - the service component of the ChromeDriver
     * @param ftpFileTransferService
     *        - responsible for file transfers to the FTP server
     * @throws NotPossibleForDeviceException
     *         - thrown when cannot create emulator wrap device.
     */
    public EmulatorWrapDevice(IDevice deviceToWrap,
            ExecutorService executor,
            BackgroundShellCommandExecutor shellCommandExecutor,
            ServiceCommunicator serviceCommunicator,
            UIAutomatorCommunicator automatorCommunicator,
            ChromeDriverService chromeDriverService,
            FileRecycler fileRecycler,
            FtpFileTransferService ftpFileTransferService)
        throws NotPossibleForDeviceException {
        super(deviceToWrap,
              executor,
              shellCommandExecutor,
              serviceCommunicator,
              automatorCommunicator,
              chromeDriverService,
              fileRecycler,
              ftpFileTransferService);

        if (!deviceToWrap.isEmulator()) {
            throw new NotPossibleForDeviceException("Cannot create emulator wrap device for a real, physical device.");
        }

        // By default, the magnetic field readings of an emulator is set to 0:0:0, which is an invalid value.
        // When the android method SensorManager.getRotationMatrix(...) in invoked with 0:0:0 as magnetic field
        // readings, it fails because of the invalidity of the data. This is why we set dummy data - so the on-device
        // service can function normally.

        try {

            ExtendedEmulatorConsole emulatorConsole = prepareEmulatorConsole();

            if (deviceToWrap.getVersion().getApiLevel() <= SET_MAGNETIC_FIELD_MAXIMUM_API_LEVEL_SUPPORT) {
                emulatorConsole.setMagneticField(new DeviceMagneticField(50.0f, 50.0f, 50.0f));
            }
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
            LOGGER.error("A command for real devices only was attempted on an emulator.", e);
        }
        return emulatorConsole;
    }
}
