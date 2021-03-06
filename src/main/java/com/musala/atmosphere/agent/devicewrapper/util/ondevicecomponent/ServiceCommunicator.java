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

package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.BackgroundShellCommandExecutor;
import com.musala.atmosphere.agent.exception.OnDeviceComponentStartingException;
import com.musala.atmosphere.agent.exception.OnDeviceServiceTerminationException;
import com.musala.atmosphere.commons.ConnectionType;
import com.musala.atmosphere.commons.PowerProperties;
import com.musala.atmosphere.commons.TelephonyInformation;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;
import com.musala.atmosphere.commons.beans.BatteryState;
import com.musala.atmosphere.commons.beans.DeviceAcceleration;
import com.musala.atmosphere.commons.beans.DeviceOrientation;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.util.GeoLocation;
import com.musala.atmosphere.commons.util.IntentBuilder;
import com.musala.atmosphere.commons.util.IntentBuilder.IntentAction;

/**
 * Class that communicates with the ATMOSPHERE service.
 *
 * @author yordan.petrov
 *
 */
public class ServiceCommunicator extends DeviceCommunicator<ServiceRequest> {
    private final static Logger LOGGER = Logger.getLogger(ServiceCommunicator.class.getCanonicalName());
    private static final String ATMOSPHERE_SERVICE_COMPONENT = "com.musala.atmosphere.service/com.musala.atmosphere.service.AtmosphereService";

    /**
     * Creates a {@link ServiceCommunicator service communicator} instance that communicate with an on-device component.
     *
     * @param requestSender
     *        - a request sender
     * @param commandExecutor
     *        - a shell command executor or the device
     * @param serialNumber
     *        - serial number of the device
     */
    public ServiceCommunicator(DeviceRequestSender<ServiceRequest> requestSender,
            BackgroundShellCommandExecutor commandExecutor,
            String serialNumber) {
        super(requestSender, commandExecutor, serialNumber);
    }

    /**
     * Gets the device power environment properties.
     *
     * @return a {@link PowerProperties} data container instance.
     * @throws CommandFailedException
     *         if getting the power properties fails
     */
    public PowerProperties getPowerProperties() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<>(ServiceRequest.GET_POWER_PROPERTIES);

        try {
            PowerProperties properties = (PowerProperties) requestSender.request(serviceRequest);
            return properties;
        } catch (ClassNotFoundException | IOException e) {
            // Redirect the exception to the server
            throw new CommandFailedException("Getting environment power properties failed.", e);
        }
    }

    /**
     * Fetches the sensor orientation readings of the device.
     *
     * @return a {@link DeviceOrientation} instance.
     * @throws CommandFailedException
     *         if getting the device orientation fails
     */
    public DeviceOrientation getDeviceOrientation() throws CommandFailedException {
        Request<ServiceRequest> request = new Request<>(ServiceRequest.GET_ORIENTATION_READINGS);

        try {
            float[] response = (float[]) requestSender.request(request);

            float orientationAzimuth = response[0];
            float orientationPitch = response[1];
            float orientationRoll = response[2];
            DeviceOrientation deviceOrientation = new DeviceOrientation(orientationAzimuth,
                                                                        orientationPitch,
                                                                        orientationRoll);

            return deviceOrientation;
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting device orientation failed.", e);
        }
    }

    /**
     * Gets the battery state of the device.
     *
     * @return a member of the {@link BatteryState BatteryState} enumeration.
     * @throws CommandFailedException
     *         if getting the battery state fails
     */
    public BatteryState getBatteryState() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<>(ServiceRequest.GET_POWER_PROPERTIES);

        try {
            Integer serviceResponse = (Integer) requestSender.request(serviceRequest);
            if (serviceResponse != -1) {
                BatteryState currentBatteryState = BatteryState.getStateById(serviceResponse);
                return currentBatteryState;
            } else {
                throw new CommandFailedException("The service could not retrieve the battery status.");
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting battery status failed.", e);
        }
    }

    /**
     * Gets the total RAM of the device.
     *
     * @return the total RAM of the device.
     * @throws CommandFailedException
     *         if getting the total ram fails
     */
    public int getTatalRamMemory() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<>(ServiceRequest.GET_TOTAL_RAM);

        try {
            int serviceResponse = (int) requestSender.request(serviceRequest);
            return serviceResponse;
        } catch (ClassNotFoundException | IOException | CommandFailedException e) {
            throw new CommandFailedException("Getting device's total RAM memory failed.", e);
        }
    }

    /**
     * Gets the connection type of the device.
     *
     * @return a member of the {@link ConnectionType} enumeration.
     * @throws CommandFailedException
     *         if getting the connection type fails
     */
    public ConnectionType getConnectionType() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<>(ServiceRequest.GET_CONNECTION_TYPE);

        try {
            Integer serviceResponse = (Integer) requestSender.request(serviceRequest);
            return ConnectionType.getById(serviceResponse);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting connection type failed. See enclosed exception for more information.",
                                             e);
        }
    }

    /**
     * Sets the WiFi state on the device.
     *
     * @param state
     *        - true if the WiFi should be on; false if it should be off.
     * @throws CommandFailedException
     *         if setting the WiFi fails
     *
     */
    public void setWiFi(boolean state) throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<>(ServiceRequest.SET_WIFI);
        Boolean[] arguments = new Boolean[] {state};
        serviceRequest.setArguments(arguments);

        try {
            requestSender.request(serviceRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Setting WiFi failed. See enclosed exception for more information.", e);
        }
    }

    /**
     * Opens the location settings activity.
     *
     * @throws CommandFailedException
     *         if failed to open the location settings
     *
     */
    public void openLocationSettings() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<>(ServiceRequest.OPEN_LOCATION_SETTINGS);

        try {
            requestSender.request(serviceRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Opening location settings failed. See enclosed exception for more information.",
                                             e);
        }
    }

    /**
     * Check if the GPS location is enabled on this device.
     *
     * @return <code>true</code> if the GPS location is enabled, <code>false</code> if it's disabled
     * @throws CommandFailedException
     *         if failed to get the GPS location state of the device
     */
    public boolean isGpsLocationEnabled() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<>(ServiceRequest.IS_GPS_LOCATION_ENABLED);

        try {
            return (boolean) requestSender.request(serviceRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting GPS Location state of the device failed. See enclosed exception for more information.",
                                             e);
        }
    }

    /**
     * Fetches the sensor acceleration readings of the device.
     *
     * @return a {@link DeviceAcceleration} instance.
     * @throws CommandFailedException
     *         - if the execution of the command failed
     */
    public DeviceAcceleration getAcceleration() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<>(ServiceRequest.GET_ACCELERATION_READINGS);
        try {
            Float[] acceeleration = (Float[]) requestSender.request(serviceRequest);
            DeviceAcceleration deviceAcceleration = new DeviceAcceleration(acceeleration[0],
                                                                           acceeleration[1],
                                                                           acceeleration[2]);
            return deviceAcceleration;
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting acceleration failed. See enclosed exception for more information.",
                                             e);
        }
    }

    /**
     * Fetches the sensor proximity readings of the device.
     *
     * @return a float representing the proximity of the device
     * @throws CommandFailedException
     *         if getting the proximity fails
     */
    public float getProximity() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<>(ServiceRequest.GET_PROXIMITY_READINGS);
        try {
            float proximity = (float) requestSender.request(serviceRequest);

            return proximity;
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting the proximity failed. See enclosed exception for more information.",
                                             e);
        }
    }

    /**
     * Gets information about the telephony services on the device.
     *
     * @return {@link TelephonyInformation} instance.
     * @throws CommandFailedException
     *         if getting the telephony information fails
     */
    public TelephonyInformation getTelephonyInformation() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<>(ServiceRequest.GET_TELEPHONY_INFORMATION);
        try {
            TelephonyInformation telephonyInformation = (TelephonyInformation) requestSender.request(serviceRequest);
            return telephonyInformation;
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting telephony information failed.", e);
        }
    }

    /**
     * Starts an application on the device.
     *
     * @param args
     *        start application arguments
     * @return <code>true</code> if the application launch is successful and <code>false</code> otherwise
     * @throws CommandFailedException
     *         thrown when starting an application fails
     */
    public boolean startApplication(Object[] args) throws CommandFailedException {
        Request<ServiceRequest> startAppRequest = new Request<>(ServiceRequest.START_APP);
        startAppRequest.setArguments(args);

        try {
            return (boolean) requestSender.request(startAppRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Starting application faliled.", e);
        }
    }

    /**
     * Sends a request to the ATMOSPHERE service to mock the current location of the device using the passed location.
     *
     * @param args
     *        - a {@link GeoLocation} object representing the mock location
     * @return <code>true</code> if mocking the location is successful, <code>false</code> otherwise
     * @throws CommandFailedException
     *         thrown when the mock fails, e.g. when communication with the service fails
     */
    public boolean mockLocation(Object[] args) throws CommandFailedException {
        Request<ServiceRequest> mockLocationRequest = new Request<>(ServiceRequest.MOCK_LOCATION);
        mockLocationRequest.setArguments(args);

        try {
            return (boolean) requestSender.request(mockLocationRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Mocking the location of the device failed.", e);
        }
    }

    /**
     * Sends a request to the ATMOSPHERE service to stop mocking the location of a given provider.
     *
     * @param args
     *        - a {@link String} representing the provider name
     * @throws CommandFailedException
     *         thrown when disabling the mock provider fails, e.g. communication with the service fails
     */
    public void disableMockLocation(Object[] args) throws CommandFailedException {
        Request<ServiceRequest> disableMockLocationRequest = new Request<>(ServiceRequest.DISABLE_MOCK_LOCATION);
        disableMockLocationRequest.setArguments(args);

        try {
            requestSender.request(disableMockLocationRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Disabling mock lcoation provider on the device failed.", e);
        }
    }

    /**
     * Checks if the device is awake.
     *
     * @return - <code>true</code> if the device is awake and <code>false</code> otherwise
     * @throws CommandFailedException
     *         thrown when getting the awake status fails, e.g. communication with the service fails
     */
    public boolean getAwakeStatus() throws CommandFailedException {
        Request<ServiceRequest> getAwakeStatusRequest = new Request<>(ServiceRequest.GET_AWAKE_STATUS);

        try {
            return (boolean) requestSender.request(getAwakeStatusRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting device awake status failed.", e);
        }
    }

    /**
     * Checks if the device has an available camera.
     *
     * @return true if the device has a camera, else false
     * @throws CommandFailedException
     *         thrown when getting the camera availability fails, e.g. communication with the service fails
     */
    public boolean getCameraAvailability() throws CommandFailedException {
        Request<ServiceRequest> getCameraAvailabilityRequest = new Request<>(ServiceRequest.GET_CAMERA_AVAILABILITY);

        try {
            return (boolean) requestSender.request(getCameraAvailabilityRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting device camera availability failed.", e);
        }
    }

    /**
     * Checks if there are running processes with the given package on the device.
     *
     * @param args
     *        - contains the package name to look for
     * @return <code>true</code> if there are such processes and <code>false</code> otherwise
     * @throws CommandFailedException
     *         thrown when getting the running process fails, e.g. communication with the service fails
     * @deprecated Since Lollipop this method is no longer available.
     *         Use {@link com.musala.atmosphere.agent.devicewrapper.AbstractWrapDevice#isProcessRunning AbstractWrapDevice.isProcessRunning}.
     */
    @Deprecated
    public boolean getProcessRunning(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> getProcessRunningRequest = new Request<>(ServiceRequest.GET_PROCESS_RUNNING);
        getProcessRunningRequest.setArguments(args);

        try {
            return (boolean) requestSender.request(getProcessRunningRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Checking for running process failed.", e);
        }
    }

    /**
     * Sets the Keyguard status on the Device.
     *
     * @param args
     *        - <code>false</code> for the keyguard to be dismissed and <code>true</code> to be re enabled
     * @throws CommandFailedException
     *         if setting the keyguard status failed
     */
    public void setKeyguard(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> setKeyguardRequest = new Request<>(ServiceRequest.SET_KEYGUARD);
        setKeyguardRequest.setArguments(args);

        try {
            requestSender.request(setKeyguardRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Setting keyguard status failed.", e);
        }
    }

    /**
     * Used to check the lock state of the device.
     *
     * @throws CommandFailedException
     *         thrown when the request fails
     * @return boolean, the lock state of the device
     */
    public boolean isLocked() throws CommandFailedException {
        Request<ServiceRequest> isLocked = new Request<>(ServiceRequest.IS_LOCKED);

        try {
            return (boolean) requestSender.request(isLocked);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting the lock state of the device failed.", e);
        }
    }

    /**
     * Brings the task with the given id to the front.
     *
     * @param args
     *        - id of the task which is going to be brought to front and timeout to wait while the task is brought to
     *        front.
     * @return <code>true</code> if the task is on the front and <code>false</code> otherwise.
     * @throws CommandFailedException
     *         if bringing the task to the front fails.
     * @deprecated Since Lollipop this method is no longer available.
     */
    @Deprecated
    public boolean bringTaskToFront(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> bringTaskToFrontRequest = new Request<>(ServiceRequest.BRING_TASK_TO_FRONT);
        bringTaskToFrontRequest.setArguments(args);

        try {
            return (boolean) requestSender.request(bringTaskToFrontRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Bringing the task to the front failed.", e);
        }
    }

    /**
     * Gets the ids of the running tasks.
     *
     * @param args
     *        - integer with maximum number of tasks to be returned.
     * @return array of the task ids that are currently running.
     * @throws CommandFailedException
     *         if getting the running tasks id fails.
     * @deprecated Since Lollipop this method is no longer available.
     */
    @Deprecated
    public int[] getRunningTaskIds(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> getRunningTasksRequest = new Request<>(ServiceRequest.GET_RUNNING_TASK_IDS);
        getRunningTasksRequest.setArguments(args);

        try {
            return (int[]) requestSender.request(getRunningTasksRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting the running tasks id failed.", e);
        }
    }

    /**
     * Wait for the given task to be moved to the given position.
     *
     * @param args
     *        - containing the id of the task, the position in which the task should be updated to and timeout to wait
     *        for moving the task.
     * @return <code>true</code> if the task is updated for the given timeout and <code>false</code> otherwise.
     * @throws CommandFailedException
     *         if waiting for the task update fails.
     * @deprecated Since Lollipop this method is no longer available.
     */
    @Deprecated
    public boolean waitForTasksUpdate(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> waitForTasksUpdateRequest = new Request<>(ServiceRequest.WAIT_FOR_TASKS_UPDATE);
        waitForTasksUpdateRequest.setArguments(args);

        try {
            return (boolean) requestSender.request(waitForTasksUpdateRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Waiting for the task to be moved failed.", e);
        }
    }

    /**
     * Checks if there is any audio currently playing on the device.
     *
     * @return <code>true</code> if and audio is playing, <code>false</code> otherwise.
     * @throws CommandFailedException
     *         if checking for audio playing fails
     */
    public boolean isAudioPlaying() throws CommandFailedException {
        Request<ServiceRequest> isAudioPlayingRequest = new Request<>(ServiceRequest.IS_AUDIO_PLAYING);

        try {
            return (boolean) requestSender.request(isAudioPlayingRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Checking if an audio is currently playing failed.", e);
        }
    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers.
     *
     * @param args
     *        - args[0] should contain the AtmosphereIntent object for the broadcast
     * @throws CommandFailedException
     *         if sending the broadcast fails
     */
    public void sendBroadcast(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> sendBroadcastRequest = new Request<>(ServiceRequest.SEND_BROADCAST);
        sendBroadcastRequest.setArguments(args);

        try {
            requestSender.request(sendBroadcastRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Sending broadcast failed.", e);
        }
    }

    /**
     * Shows a tap location on the device screen.
     *
     * @param args
     *        - args[0] should contain the point where the tap will be placed
     * @throws CommandFailedException
     *         if showing the tap location fails
     */
    public void showTapLocation(Object[] args) throws CommandFailedException {
        Request<ServiceRequest> showTapLocationRequest = new Request<>(ServiceRequest.SHOW_TAP_LOCATION);
        showTapLocationRequest.setArguments(args);

        try {
            requestSender.request(showTapLocationRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Failed to show the tap location.", e);
        }
    }

    /**
     * Stops all background processes associated with the given package.
     *
     * @param args
     *        - args[0] should contain the given package name
     * @throws CommandFailedException
     *         if stopping the background processes fails
     */
    public void stopBackgroundProcess(Object[] args) throws CommandFailedException {
        Request<ServiceRequest> stopBackgroundProcessRequest = new Request<>(ServiceRequest.STOP_BACKGROUND_PROCESS);
        stopBackgroundProcessRequest.setArguments(args);

        try {
            requestSender.request(stopBackgroundProcessRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Stopping the given background process failed.", e);
        }
    }

    @Override
    public void startComponent() {
        IntentBuilder startSeviceIntentBuilder = new IntentBuilder(IntentAction.START_ATMOSPHERE_SERVICE);
        startSeviceIntentBuilder.setUserId(0);
        startSeviceIntentBuilder.putComponent(ATMOSPHERE_SERVICE_COMPONENT);
        String startServiceIntentCommand = startSeviceIntentBuilder.buildIntentCommand();

        try {
            shellCommandExecutor.execute(startServiceIntentCommand);
        } catch (CommandFailedException e) {
            String errorMessage = String.format("Starting ATMOSPHERE service failed for %s.", deviceSerialNumber);
            throw new OnDeviceComponentStartingException(errorMessage, e);
        }
    }

    /**
     * Retrieves token from the Augmented Traffic Control tool used for authentication before the Agent will be able to
     * modify the network connection properties.
     *
     * @return string in JSON format containing key value pairs - token, device ip address in the network, validity of
     *         the token
     * @throws CommandFailedException
     *         if sending request for retrieving token fails
     */
    public String retrieveToken() throws CommandFailedException {
        Request<ServiceRequest> receiveTokenRequest = new Request<>(ServiceRequest.RETRIEVE_TOKEN);

        try {
            return (String) requestSender.request(receiveTokenRequest);
        } catch (ClassNotFoundException | IOException | CommandFailedException e) {
            throw new CommandFailedException(String.format("Retrieving token for device %s failed.",
                                                           deviceSerialNumber),
                                             e);
        }
    }

    /**
     * Sends request to the service for modifying WiFi connection properties.
     *
     * @param args
     *        - contains information about the connection properties to be set
     * @throws CommandFailedException
     *         if sending request for device shaping fails
     * @return <code>true</code> if shaping is successful, <code>false</code> otherwise
     */
    public Boolean shapeDevice(Object[] args) throws CommandFailedException {
        Request<ServiceRequest> shapeDeviceRequest = new Request<>(ServiceRequest.SHAPE_DEVICE);
        shapeDeviceRequest.setArguments(args);

        try {
            return (Boolean) requestSender.request(shapeDeviceRequest);
        } catch (ClassNotFoundException | IOException | CommandFailedException e) {
            throw new CommandFailedException(String.format("Modifying WiFi connection properties for device %s failed.",
                                                           deviceSerialNumber),
                                             e);
        }
    }

    /**
     * Sends request to the service for restoring WiFi connection properties.
     *
     * @throws CommandFailedException
     *         if sending request for unshaping fails
     * @return <code>true</code> if unshaping is successful, <code>false</code> otherwise
     */
    public Boolean unshapeDevice() throws CommandFailedException {
        Request<ServiceRequest> shapeDeviceRequest = new Request<>(ServiceRequest.UNSHAPE_DEVICE);

        try {
            return (Boolean) requestSender.request(shapeDeviceRequest);
        } catch (ClassNotFoundException | IOException | CommandFailedException e) {
            throw new CommandFailedException(String.format("Restoring WiFi connection properties for device %s failed.",
                                                           deviceSerialNumber),
                                             e);
        }
    }

    /**
     * Sends request to the service for getting device's free space.
     *
     * @return device's free disk space in megabytes
     * @throws CommandFailedException
     *         if sending request for getting free space fails
     */
    public Long getAvailableDiskSpace() throws CommandFailedException {
        Request<ServiceRequest> getFreeSpaceRequest = new Request<>(ServiceRequest.GET_AVAILABLE_DISK_SPACE);
        try {
            return (Long) requestSender.request(getFreeSpaceRequest);
        } catch (ClassNotFoundException | IOException | CommandFailedException e) {
            throw new CommandFailedException(String.format("Checking free disk space for device %s failed",
                                                           deviceSerialNumber),
                                             e);
        }
    }

    /**
     * Sends request to the service for getting the device external storage absolute path, stored in system environment.
     *
     * @return device external storage absolute path
     * @throws CommandFailedException
     *         if sending request for getting external storage absolute path fails
     */
    public String getExternalStorage() throws CommandFailedException {
        Request<ServiceRequest> externalStorageRequest = new Request<>(ServiceRequest.GET_EXTERNAL_STORAGE);

        try {
            return (String) requestSender.request(externalStorageRequest);
        } catch (ClassNotFoundException | IOException | CommandFailedException e) {
            throw new CommandFailedException(String.format("Getting external storage for device %s failed",
                                                           deviceSerialNumber),
                                             e);
        }
    }

    /**
     * Sends request to the service to show a toast message on the device's screen.
     *
     * @param message
     *        - the message that should be shown on the device's screen
     * @throws CommandFailedException
     *         if the request sending fails
     */
    public void showToast(String message) throws CommandFailedException {
        Request<ServiceRequest> showToastRequest = new Request<>(ServiceRequest.SHOW_TOAST);
        showToastRequest.setArguments(new Object[] { message });

        try {
            requestSender.request(showToastRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException(String.format("Showing toast message failed for device %s.",
                                                           deviceSerialNumber),
                                             e);
        }
    }

    @Override
    public void stopComponent() {
        // TODO: Use socket requests here. Refactor the service to use dispatchers like the UI automator bridge.
        IntentBuilder stopServiceIntentBuilder = new IntentBuilder(IntentAction.ATMOSPHERE_SERVICE_CONTROL);
        stopServiceIntentBuilder.putExtraString("command", "stop");
        String stopServiceIntentCommand = stopServiceIntentBuilder.buildIntentCommand();

        try {
            shellCommandExecutor.execute(stopServiceIntentCommand);
            LOGGER.info("ATMOSPHERE service intent has stopped.");
        } catch (CommandFailedException e) {
            String errorMessage = String.format("Stopping ATMOSPHERE service failed for %s.", deviceSerialNumber);
            throw new OnDeviceServiceTerminationException(errorMessage, e);
        }
    }

    @Override
    public void validateRemoteServer() {
        Request<ServiceRequest> validationRequest = new Request<>(ServiceRequest.VALIDATION);
        validateRemoteServer(validationRequest);
    }

}
