package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;

import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.agent.exception.OnDeviceComponentInitializationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentStartingException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentValidationException;
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
public class ServiceCommunicator {
    private static final String ATMOSPHERE_SERVICE_COMPONENT = "com.musala.atmosphere.service/com.musala.atmosphere.service.AtmosphereService";

    private ServiceRequestHandler serviceRequestHandler;

    private final PortForwardingService portForwardingService;

    private final String deviceSerialNumber;

    private final ShellCommandExecutor shellCommandExecutor;

    public ServiceCommunicator(PortForwardingService portForwarder,
            ShellCommandExecutor commandExecutor,
            String serialNumber) {
        portForwardingService = portForwarder;
        int localPort = portForwardingService.getLocalForwardedPort();

        deviceSerialNumber = serialNumber;
        shellCommandExecutor = commandExecutor;

        startAtmosphereService();
        try {
            serviceRequestHandler = new ServiceRequestHandler(portForwardingService, localPort);
        } catch (OnDeviceComponentValidationException e) {
            String errorMessage = String.format("Service initialization failed for %s.", deviceSerialNumber);
            throw new OnDeviceComponentInitializationException(errorMessage, e);
        }
    }

    /**
     * Starts the Atmosphere service on the wrappedDevice.
     * 
     * @throws OnDeviceComponentStartingException
     */
    private void startAtmosphereService() {
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

    @Override
    public void finalize() throws OnDeviceServiceTerminationException {
        stopAtmosphereService();
    }

    /**
     * Stops the ATMOSPHERE service on the wrapped device.
     * 
     * @throws OnDeviceServiceTerminationException
     */
    public void stopAtmosphereService() {
        IntentBuilder stopServiceIntentBuilder = new IntentBuilder(IntentAction.ATMOSPHERE_SERVICE_CONTROL);
        stopServiceIntentBuilder.putExtraString("command", "stop");
        String stopServiceIntentCommand = stopServiceIntentBuilder.buildIntentCommand();

        try {
            shellCommandExecutor.execute(stopServiceIntentCommand);
        } catch (CommandFailedException e) {
            String errorMessage = String.format("Stopping ATMOSPHERE service failed for %s.", deviceSerialNumber);
            throw new OnDeviceServiceTerminationException(errorMessage, e);
        }
    }

    /**
     * Gets the device power environment properties.
     * 
     * @return a {@link PowerProperties} data container instance.
     * @throws CommandFailedException
     */
    public PowerProperties getPowerProperties() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_POWER_PROPERTIES);

        try {
            PowerProperties properties = (PowerProperties) serviceRequestHandler.request(serviceRequest);
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
     */
    public DeviceOrientation getDeviceOrientation() throws CommandFailedException {
        Request<ServiceRequest> request = new Request<ServiceRequest>(ServiceRequest.GET_ORIENTATION_READINGS);

        try {
            float[] response = (float[]) serviceRequestHandler.request(request);

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
     */
    public BatteryState getBatteryState() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_POWER_PROPERTIES);

        try {
            Integer serviceResponse = (Integer) serviceRequestHandler.request(serviceRequest);
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
     * Gets the connection type of the device.
     * 
     * @return a member of the {@link ConnectionType} enumeration.
     * @throws CommandFailedException
     */
    public ConnectionType getConnectionType() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_CONNECTION_TYPE);

        try {
            Integer serviceResponse = (Integer) serviceRequestHandler.request(serviceRequest);
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
     */
    public void setWiFi(boolean state) throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.SET_WIFI);
        Boolean[] arguments = new Boolean[] {state};
        serviceRequest.setArguments(arguments);

        try {
            serviceRequestHandler.request(serviceRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Setting WiFi failed. See enclosed exception for more information.", e);
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
        Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_ACCELERATION_READINGS);
        try {
            Float[] acceeleration = (Float[]) serviceRequestHandler.request(serviceRequest);
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
     */
    public float getProximity() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_PROXIMITY_READINGS);
        try {
            float proximity = (float) serviceRequestHandler.request(serviceRequest);

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
     */
    public TelephonyInformation getTelephonyInformation() throws CommandFailedException {
        Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_TELEPHONY_INFORMATION);
        try {
            TelephonyInformation telephonyInformation = (TelephonyInformation) serviceRequestHandler.request(serviceRequest);
            return telephonyInformation;
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting telephony information failed.", e);
        }
    }

    /**
     * Starts an application on the device.
     * 
     * @return <code>true</code> if the application launch is successful and <code>false</code> otherwise
     * @throws CommandFailedException
     */
    public boolean startApplication(Object[] args) throws CommandFailedException {
        Request<ServiceRequest> startAppRequest = new Request<ServiceRequest>(ServiceRequest.START_APP);
        startAppRequest.setArguments(args);

        try {
            return (boolean) serviceRequestHandler.request(startAppRequest);
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
        Request<ServiceRequest> mockLocationRequest = new Request<ServiceRequest>(ServiceRequest.MOCK_LOCATION);
        mockLocationRequest.setArguments(args);

        try {
            return (boolean) serviceRequestHandler.request(mockLocationRequest);
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
        Request<ServiceRequest> disableMockLocationRequest = new Request<ServiceRequest>(ServiceRequest.DISABLE_MOCK_LOCATION);
        disableMockLocationRequest.setArguments(args);

        try {
            serviceRequestHandler.request(disableMockLocationRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Disabling mock lcoation provider on the device failed.", e);
        }
    }

    /**
     * Checks if the device is awake.
     * 
     * @return - <code>true</code> if the device is awake and <code>false</code> otherwise
     * @throws CommandFailedException
     */
    public boolean getAwakeStatus() throws CommandFailedException {
        Request<ServiceRequest> getAwakeStatusRequest = new Request<ServiceRequest>(ServiceRequest.GET_AWAKE_STATUS);

        try {
            return (boolean) serviceRequestHandler.request(getAwakeStatusRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting device awake status failed.", e);
        }
    }

    /**
     * Checks if the device has an available camera.
     * 
     * @return true if the device has a camera, else false
     * @throws CommandFailedException
     */
    public boolean getCameraAvailability() throws CommandFailedException {
        Request<ServiceRequest> getCameraAvailabilityRequest = new Request<ServiceRequest>(ServiceRequest.GET_CAMERA_AVAILABILITY);

        try {
            return (boolean) serviceRequestHandler.request(getCameraAvailabilityRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Getting device camera availability failed.", e);
        }
    }

    /**
     * Checks of there are running Processes on the device with the given package
     * 
     * @param args
     *        -contains the packageName
     * @return true if there are such processes and false otherwise
     * @throws CommandFailedException
     */

    public boolean getProcessRunning(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> getProcessRunningRequest = new Request<ServiceRequest>(ServiceRequest.GET_PROCESS_RUNNING);
        getProcessRunningRequest.setArguments(args);

        try {
            return (boolean) serviceRequestHandler.request(getProcessRunningRequest);
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
        Request<ServiceRequest> setKeyguardRequest = new Request<ServiceRequest>(ServiceRequest.SET_KEYGUARD);
        setKeyguardRequest.setArguments(args);

        try {
            serviceRequestHandler.request(setKeyguardRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Setting keyguard status failed.", e);
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
     */
    public boolean bringTaskToFront(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> bringTaskToFrontRequest = new Request<ServiceRequest>(ServiceRequest.BRING_TASK_TO_FRONT);
        bringTaskToFrontRequest.setArguments(args);

        try {
            return (boolean) serviceRequestHandler.request(bringTaskToFrontRequest);
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
     */
    public int[] getRunningTaskIds(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> getRunningTasksRequest = new Request<ServiceRequest>(ServiceRequest.GET_RUNNING_TASK_IDS);
        getRunningTasksRequest.setArguments(args);

        try {
            return (int[]) serviceRequestHandler.request(getRunningTasksRequest);
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
     */
    public boolean waitForTasksUpdate(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> waitForTasksUpdateRequest = new Request<ServiceRequest>(ServiceRequest.WAIT_FOR_TASKS_UPDATE);
        waitForTasksUpdateRequest.setArguments(args);

        try {
            return (boolean) serviceRequestHandler.request(waitForTasksUpdateRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Waiting for the task to be moved failed.", e);
        }
    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers.
     * 
     * @param args
     *        - args[0] should contain the AtmosphereIntent object for the broadcast
     * 
     */
    public void sendBroadcast(Object args[]) throws CommandFailedException {
        Request<ServiceRequest> sendBroadcastRequest = new Request<ServiceRequest>(ServiceRequest.SEND_BROADCAST);
        sendBroadcastRequest.setArguments(args);

        try {
            serviceRequestHandler.request(sendBroadcastRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Sending broadcast failed.", e);
        }
    }
}
