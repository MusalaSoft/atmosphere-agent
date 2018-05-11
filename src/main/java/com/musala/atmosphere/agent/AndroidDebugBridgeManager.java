// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.agent;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.util.AgentPropertiesLoader;
import com.musala.atmosphere.commons.sa.exceptions.ADBridgeFailException;

/**
 * Manages the connection to the {@link AndroidDebugBridge}, sets listeners and gets a list of connected devices.
 *
 * @author yordan.petrov
 *
 */
public class AndroidDebugBridgeManager {
    private final static Logger LOGGER = Logger.getLogger(AndroidDebugBridgeManager.class.getCanonicalName());

    private static AndroidDebugBridge androidDebugBridge;

    private static String adbPath;

    private static DeviceChangeListener currentDeviceChangeListener;

    /**
     * Sets the path to {@link AndroidDebugBridge}.
     *
     * @param adbPath
     *        - the path to {@link AndroidDebugBridge} to be set.
     */
    public void setAndroidDebugBridgePath(String adbPath) {
        this.adbPath = adbPath;
    }

    /**
     * Initializes a connection to the {@link AndroidDebugBridge}. Make sure You have called
     * {@link #setAndroidDebugBridgePath(String)} first.
     *
     * @throws ADBridgeFailException
     *         thrown when the Android debug bridge failed to init.
     */
    public void startAndroidDebugBridge() throws ADBridgeFailException {
        String errorMessage = "The Android debug bridge failed to init.";
        // Start the bridge
        try {
            AndroidDebugBridge.init(false /* debugger support */);
            androidDebugBridge = AndroidDebugBridge.createBridge(adbPath, false /*
                                                                                 * force new bridge, no need for that
                                                                                 */);
            DdmPreferences.setTimeOut(AgentPropertiesLoader.getAdbConnectionTimeout());
        } catch (IllegalStateException e) {
            LOGGER.fatal(errorMessage, e);
            throw new ADBridgeFailException(errorMessage, e);
        } catch (NullPointerException e) {
            // The debug bridge creation failed internally.
            LOGGER.fatal(errorMessage, e);
            throw new ADBridgeFailException(errorMessage, e);
        }
    }

    /**
     * Removes the previous {@link DeviceChangeListener} and sets the given one.
     *
     * @param deviceChangeListener
     *        - the new {@link DeviceChangeListener} to be set.
     */
    public void setListener(DeviceChangeListener deviceChangeListener) {
        AndroidDebugBridge.removeDeviceChangeListener(currentDeviceChangeListener);
        currentDeviceChangeListener = deviceChangeListener;
        AndroidDebugBridge.addDeviceChangeListener(currentDeviceChangeListener);
    }

    /**
     * Gets the current {@link DeviceChangeListener}.
     *
     * @return - the current {@link DeviceChangeListener}
     */
    public DeviceChangeListener getCurrentListener() {
        return currentDeviceChangeListener;
    }

    /**
     * Gets the {@link AndroidDebugBridge} instance used by the {@link AndroidDebugBridgeManager}. Make sure you have
     * called {@link #startAndroidDebugBridge()} first.
     *
     * @return the {@link AndroidDebugBridge} instance used by the {@link AndroidDebugBridgeManager}.
     */
    public AndroidDebugBridge getAndroidDebugBridge() {
        return androidDebugBridge;
    }

    /**
     * Gets the initial devices list (IDevices). Make sure you have called {@link #startAndroidDebugBridge()} first.
     *
     * @return List of IDevices
     * @throws ADBridgeFailException
     *         thrown when the Android debug bridge failed to init.
     */
    public List<IDevice> getInitialDeviceList() throws ADBridgeFailException {
        // From an adb example :
        // we can't just ask for the device list right away, as the internal thread getting
        // them from ADB may not be done getting the first list.
        // Since we don't really want getDevices() to be blocking, we wait here manually.
        int timeout = 0;

        while (androidDebugBridge.hasInitialDeviceList() == false) {
            try {
                Thread.sleep(100);
                timeout++;
            } catch (InterruptedException e) {
            }
            // let's not wait > timeout milliseconds.
            if (timeout * 100 > AgentPropertiesLoader.getAdbConnectionTimeout()) {
                LOGGER.fatal("Timeout getting initial device list.");

                throw new ADBridgeFailException("Bridge timed out.");
            }
        }

        IDevice[] devicesArray = androidDebugBridge.getDevices();
        return Arrays.asList(devicesArray);
    }
}
