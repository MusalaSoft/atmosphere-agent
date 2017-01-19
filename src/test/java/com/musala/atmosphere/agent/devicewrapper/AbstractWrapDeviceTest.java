package com.musala.atmosphere.agent.devicewrapper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.chrome.ChromeDriverService;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.devicewrapper.util.BackgroundShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.agent.util.FileRecycler;
import com.musala.atmosphere.agent.util.FtpFileTransferService;
import com.musala.atmosphere.commons.ConnectionType;
import com.musala.atmosphere.commons.PowerProperties;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.TelephonyInformation;
import com.musala.atmosphere.commons.beans.BatteryLevel;
import com.musala.atmosphere.commons.beans.DeviceAcceleration;
import com.musala.atmosphere.commons.beans.DeviceMagneticField;
import com.musala.atmosphere.commons.beans.DeviceOrientation;
import com.musala.atmosphere.commons.beans.MobileDataState;
import com.musala.atmosphere.commons.beans.PhoneNumber;
import com.musala.atmosphere.commons.beans.PowerSource;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.commons.util.telephony.CallState;
import com.musala.atmosphere.commons.util.telephony.DataActivity;
import com.musala.atmosphere.commons.util.telephony.DataState;
import com.musala.atmosphere.commons.util.telephony.NetworkType;
import com.musala.atmosphere.commons.util.telephony.PhoneType;
import com.musala.atmosphere.commons.util.telephony.SimState;

/**
 *
 * @author yordan.petrov
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractWrapDeviceTest {
    private static class FakeWrapDevice extends AbstractWrapDevice {

        private static final long serialVersionUID = 1941712619444651820L;

        public FakeWrapDevice(IDevice deviceToWrap,
                ExecutorService executor,
                BackgroundShellCommandExecutor shellCommandExecutor,
                ServiceCommunicator serviceCommunicator,
                UIAutomatorCommunicator automatorCommunicator,
                ChromeDriverService chromeDriverService,
                FileRecycler fileRecycler,
                FtpFileTransferService ftpFileTransferService) throws RemoteException {
            super(deviceToWrap,
                  executor,
                  shellCommandExecutor,
                  serviceCommunicator,
                  automatorCommunicator,
                  chromeDriverService,
                  fileRecycler,
                  null);

        }

        @Override
        protected void setPowerProperties(PowerProperties properties) throws CommandFailedException {
        }

        @Override
        protected void cancelCall(PhoneNumber n) throws CommandFailedException {
        }

        @Override
        protected void receiveCall(PhoneNumber phoneNumber) throws CommandFailedException {
        }

        @Override
        protected void acceptCall(PhoneNumber phoneNumber) throws CommandFailedException {
        }

        @Override
        protected void holdCall(PhoneNumber phoneNumber) throws CommandFailedException {
        }

        @Override
        protected void receiveSms(SmsMessage smsMessage) throws CommandFailedException {
        }

        @Override
        protected void setOrientation(DeviceOrientation deviceOrientation) throws CommandFailedException {
        }

        @Override
        protected void setAcceleration(DeviceAcceleration deviceAcceleration) throws CommandFailedException {
        }

        @Override
        protected void setMagneticField(DeviceMagneticField deviceMagneticField) throws CommandFailedException {
        }

        @Override
        protected void setProximity(float proximity) throws CommandFailedException {
        }

        @Override
        protected void setMobileDataState(MobileDataState state) throws CommandFailedException {
        }

        @Override
        protected MobileDataState getMobileDataState() throws CommandFailedException {
            return null;
        }

        @Override
        protected void setNetworkSpeed(Pair<Integer, Integer> speeds) throws CommandFailedException {
        }

    };

    @Mock
    private IDevice wrappedDevice;

    @Mock
    private ServiceCommunicator serviceCommunicator;

    @Mock
    private static ExecutorService mockedExecutor;

    @InjectMocks
    private FakeWrapDevice testWrapDevice;

    @BeforeClass
    public static void setUp() {
        mockedExecutor = mock(ExecutorService.class);
    }

    @Test
    public void testGetDeviceOrientation() throws Exception {
        DeviceOrientation expected = new DeviceOrientation(1.0f, 2.0f, 3.0f);
        Mockito.doReturn(expected).when(serviceCommunicator).getDeviceOrientation();

        DeviceOrientation actual = (DeviceOrientation) testWrapDevice.route(RoutingAction.GET_DEVICE_ORIENTATION);
        assertEquals("The expected device orientation did not match the actual.", expected, actual);
    }

    @Test
    public void testGetDeviceAcceleration() throws Exception {
        DeviceAcceleration expected = new DeviceAcceleration(1.0f, 2.0f, 3.0f);
        Mockito.doReturn(expected).when(serviceCommunicator).getAcceleration();

        DeviceAcceleration actual = (DeviceAcceleration) testWrapDevice.route(RoutingAction.GET_DEVICE_ACCELERATION);
        assertEquals("The expected device acceleration did not match the actual.", expected, actual);
    }

    @Test
    public void testGetDeviceProximity() throws Exception {
        float expectedProximity = 3.33f;
        Mockito.doReturn(expectedProximity).when(serviceCommunicator).getProximity();

        float actualProximity = (float) testWrapDevice.route(RoutingAction.GET_DEVICE_PROXIMITY);
        assertEquals("The expected device proximity did not match the actual.",
                     expectedProximity,
                     actualProximity,
                     0.1);
    }

    @Test
    public void testGetPowerProperties() throws Exception {
        PowerProperties expectedPowerProperties = new PowerProperties();
        expectedPowerProperties.setBatteryLevel(new BatteryLevel(69));
        expectedPowerProperties.setPowerSource(PowerSource.PLUGGED_WIRELESS);
        Mockito.doReturn(expectedPowerProperties).when(serviceCommunicator).getPowerProperties();

        PowerProperties actualPowerProperties = (PowerProperties) testWrapDevice.route(RoutingAction.GET_POWER_PROPERTIES);
        assertEquals("The expected power properties did not match the actual.",
                     expectedPowerProperties,
                     actualPowerProperties);
    }

    @Test
    public void testSetWiFi() throws Exception {
        testWrapDevice.route(RoutingAction.SET_WIFI_STATE, true);
        verify(serviceCommunicator, times(1)).setWiFi(true);

        testWrapDevice.route(RoutingAction.SET_WIFI_STATE, false);
        verify(serviceCommunicator, times(1)).setWiFi(false);
    }

    @Test
    public void testGetConnectionType() throws Exception {
        ConnectionType expectedConnectionType = ConnectionType.MOBILE_HIPRI;
        Mockito.doReturn(expectedConnectionType).when(serviceCommunicator).getConnectionType();

        ConnectionType actualConnectionType = (ConnectionType) testWrapDevice.route(RoutingAction.GET_CONNECTION_TYPE);
        assertEquals("The expected connection type did not match the actual.",
                     expectedConnectionType,
                     actualConnectionType);
    }

    @Test
    public void testGetTelephonyInformation() throws Exception {
        TelephonyInformation expectedTelephonyInformation = new TelephonyInformation();
        expectedTelephonyInformation.setCallState(CallState.CALL_STATE_RINGING);
        expectedTelephonyInformation.setDataActivity(DataActivity.DATA_ACTIVITY_INOUT);
        expectedTelephonyInformation.setDataState(DataState.DATA_CONNECTING);
        expectedTelephonyInformation.setDeviceId("007");
        expectedTelephonyInformation.setDeviceSoftwareVersion("iOS 7");
        expectedTelephonyInformation.setLine1Number("333");
        expectedTelephonyInformation.setNetworkCountryIso("359");
        expectedTelephonyInformation.setNetworkOperator("451343416978");
        expectedTelephonyInformation.setNetworkOperatorName("MednaTel");
        expectedTelephonyInformation.setNetworkType(NetworkType.NETWORK_TYPE_EVDO_B);
        expectedTelephonyInformation.setPhoneType(PhoneType.PHONE_TYPE_SIP);
        expectedTelephonyInformation.setSimOperator("175915619");
        expectedTelephonyInformation.setSimOperatorName("SimOperatorName");
        expectedTelephonyInformation.setSimState(SimState.SIM_STATE_PUK_REQUIRED); // Shit happens
        expectedTelephonyInformation.setSubscriberId("123");
        expectedTelephonyInformation.setVoiceMailAlphaTag("alphaTag");
        expectedTelephonyInformation.setVoiceMailNumber("0879665321");
        Mockito.doReturn(expectedTelephonyInformation).when(serviceCommunicator).getTelephonyInformation();

        TelephonyInformation actualTelephonyInformation = (TelephonyInformation) testWrapDevice.route(RoutingAction.GET_TELEPHONY_INFO);
        assertEquals("The expected telephony information did not match the actual.",
                     expectedTelephonyInformation,
                     actualTelephonyInformation);
    }
}