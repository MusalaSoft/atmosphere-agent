package com.musala.atmosphere.agent.devicewrapper;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.util.FakeOnDeviceComponentAnswer;
import com.musala.atmosphere.commons.PowerProperties;
import com.musala.atmosphere.commons.RoutingAction;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.beans.DeviceAcceleration;
import com.musala.atmosphere.commons.beans.DeviceMagneticField;
import com.musala.atmosphere.commons.beans.DeviceOrientation;
import com.musala.atmosphere.commons.beans.MobileDataState;
import com.musala.atmosphere.commons.beans.PhoneNumber;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.util.Pair;

public class AbstractWrapDeviceTest {
    private AbstractWrapDevice testWrapDevice;

    private IDevice device;

    private FakeOnDeviceComponentAnswer fakeOnDeviceComponentAnswer;

    @Before
    public void setUp() throws Exception {
        device = mock(IDevice.class);
        fakeOnDeviceComponentAnswer = new FakeOnDeviceComponentAnswer();
        Mockito.doAnswer(fakeOnDeviceComponentAnswer).when(device).createForward(anyInt(), anyInt());

        testWrapDevice = new AbstractWrapDevice(device) {
            /**
             * auto-generated serialization id
             */
            private static final long serialVersionUID = -1975205948325404220L;

            @Override
            public void setAcceleration(DeviceAcceleration deviceAcceleration) throws CommandFailedException {
            }

            @Override
            public void setPowerProperties(PowerProperties state) throws CommandFailedException {
            }

            @Override
            public void setNetworkSpeed(Pair<Integer, Integer> speeds) throws CommandFailedException {
            }

            @Override
            public void setOrientation(DeviceOrientation deviceOrientation) throws CommandFailedException {
            }

            @Override
            public void setMobileDataState(MobileDataState state) throws CommandFailedException {
            }

            @Override
            public MobileDataState getMobileDataState() throws CommandFailedException {
                return null;
            }

            @Override
            public void receiveSms(SmsMessage smsMessage) throws CommandFailedException {

            }

            @Override
            public void receiveCall(PhoneNumber phoneNumber) throws CommandFailedException {
            }

            @Override
            public void acceptCall(PhoneNumber phoneNumber) throws CommandFailedException {
            }

            @Override
            public void holdCall(PhoneNumber phoneNumber) throws CommandFailedException {
            }

            @Override
            public void cancelCall(PhoneNumber phoneNumber) throws CommandFailedException {
            }

            @Override
            protected void setMagneticField(DeviceMagneticField deviceMagneticField) throws CommandFailedException {
            }

            @Override
            protected void setProximity(float proximity) throws CommandFailedException {
            }
        };

    }

    @Test
    public void testGetDeviceOrientation() throws RemoteException, CommandFailedException {
        testWrapDevice.route(RoutingAction.GET_DEVICE_ORIENTATION);
    }

    @Test
    public void testGetDeviceAcceleration() throws RemoteException, CommandFailedException {
        testWrapDevice.route(RoutingAction.GET_DEVICE_ACCELERATION);
    }

    @Test
    public void testGetPowerProperties() throws Throwable {
        testWrapDevice.route(RoutingAction.GET_POWER_PROPERTIES);
    }

    @Test
    public void testSetWiFi() throws Throwable {
        testWrapDevice.route(RoutingAction.SET_WIFI_STATE, true);

        testWrapDevice.route(RoutingAction.SET_WIFI_STATE, false);
    }

    @Test
    public void testGetConnectionType() throws Throwable {
        testWrapDevice.route(RoutingAction.GET_CONNECTION_TYPE);
    }

    @Test
    public void testGetTelephonyInformation() throws Throwable {
        testWrapDevice.route(RoutingAction.GET_TELEPHONY_INFO);
    }
}