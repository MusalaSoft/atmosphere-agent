package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.rmi.RemoteException;

import com.musala.atmosphere.agent.devicewrapper.PortForwardingService;
import com.musala.atmosphere.agent.exception.InitializeServiceRequestHandlerFailedException;
import com.musala.atmosphere.agent.exception.ServiceValidationFailedException;
import com.musala.atmosphere.agent.exception.StartAtmosphereServiceFailedException;
import com.musala.atmosphere.agent.exception.StopAtmosphereServiceFailedException;
import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.ConnectionType;
import com.musala.atmosphere.commons.DeviceAcceleration;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.DeviceOrientation;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.service.ServiceRequest;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.util.IntentBuilder;
import com.musala.atmosphere.commons.util.IntentBuilder.IntentAction;

public class ServiceCommunicator
{
	private static final String ATMOSPHERE_SERVICE_COMPONENT = "com.musala.atmosphere.service/com.musala.atmosphere.service.AtmosphereService";

	private final IWrapDevice wrappedDevice;

	private ServiceRequestHandler serviceRequestHandler;

	private PortForwardingService portForwardingService;

	private String deviceSerialNumber;

	public ServiceCommunicator(PortForwardingService portForwarder, IWrapDevice wrappedDevice)
	{
		portForwardingService = portForwarder;
		int localPort = portForwardingService.getLocalForwardedPort();

		this.wrappedDevice = wrappedDevice;
		try
		{
			DeviceInformation deviceInformation = wrappedDevice.getDeviceInformation();
			deviceSerialNumber = deviceInformation.getSerialNumber();
		}
		catch (RemoteException e1)
		{
			// not possible, as this is a local invocation.
		}

		startAtmosphereService();
		portForwardingService.forwardServicePort();
		try
		{
			serviceRequestHandler = new ServiceRequestHandler(localPort);
		}
		catch (ServiceValidationFailedException e)
		{
			String errorMessage = String.format("Service initialization failed for %s.", deviceSerialNumber);
			throw new InitializeServiceRequestHandlerFailedException(errorMessage, e);
		}
	}

	/**
	 * Starts the Atmosphere service on the wrappedDevice.
	 * 
	 * @throws StartAtmosphereServiceFailedException
	 */
	private void startAtmosphereService()
	{
		IntentBuilder startSeviceIntentBuilder = new IntentBuilder(IntentAction.START_ATMOSPHERE_SERVICE);
		startSeviceIntentBuilder.setUserId(0);
		startSeviceIntentBuilder.putComponent(ATMOSPHERE_SERVICE_COMPONENT);
		String startServiceIntentCommand = startSeviceIntentBuilder.buildIntentCommand();

		try
		{
			wrappedDevice.executeShellCommand(startServiceIntentCommand);
		}
		catch (RemoteException | CommandFailedException e)
		{
			String errorMessage = String.format("Starting ATMOSPHERE service failed for %s.", deviceSerialNumber);
			throw new StartAtmosphereServiceFailedException(errorMessage, e);
		}
	}

	@Override
	public void finalize() throws StopAtmosphereServiceFailedException
	{
		stopAtmosphereService();
	}

	/**
	 * Stops the ATMOSPHERE service on the wrapped device.
	 * 
	 * @throws StopAtmosphereServiceFailedException
	 */
	public void stopAtmosphereService()
	{
		IntentBuilder stopServiceIntentBuilder = new IntentBuilder(IntentAction.ATMOSPHERE_SERVICE_CONTROL);
		stopServiceIntentBuilder.putExtraString("command", "stop");
		String stopServiceIntentCommand = stopServiceIntentBuilder.buildIntentCommand();

		try
		{
			wrappedDevice.executeShellCommand(stopServiceIntentCommand);
		}
		catch (RemoteException | CommandFailedException e)
		{
			String errorMessage = String.format("Stopping ATMOSPHERE service failed for %s.", deviceSerialNumber);
			throw new StopAtmosphereServiceFailedException(errorMessage, e);
		}
	}

	/**
	 * Gets the battery level of the device.
	 * 
	 * @return Capacity in percents.
	 * @throws CommandFailedException
	 */
	public int getBatteryLevel() throws CommandFailedException
	{
		portForwardingService.forwardServicePort();
		Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_BATTERY_LEVEL);

		try
		{
			int level = (Integer) serviceRequestHandler.request(serviceRequest);
			return level;
		}
		catch (ClassNotFoundException | IOException e)
		{
			// Redirect the exception to the server
			throw new CommandFailedException("Getting battery level failed.", e);
		}
	}

	/**
	 * Gets the power state of the device.
	 * 
	 * @return boolean value; true if power is connected and false if power is disconnected.
	 * @throws CommandFailedException
	 */
	public boolean getPowerState() throws CommandFailedException
	{
		portForwardingService.forwardServicePort();
		Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_POWER_STATE);
		boolean powerState;

		try
		{
			powerState = (Boolean) serviceRequestHandler.request(serviceRequest);
		}
		catch (ClassNotFoundException | IOException e)
		{
			// Redirect the exception to the server
			throw new CommandFailedException("Getting power state failed.", e);
		}
		return powerState;
	}

	/**
	 * Fetches the sensor orientation readings of the device.
	 * 
	 * @return a {@link DeviceOrientation} instance.
	 * @throws CommandFailedException
	 */
	public DeviceOrientation getDeviceOrientation() throws CommandFailedException
	{
		portForwardingService.forwardServicePort();
		Request<ServiceRequest> request = new Request<ServiceRequest>(ServiceRequest.GET_ORIENTATION_READINGS);

		try
		{
			float[] response = (float[]) serviceRequestHandler.request(request);

			float orientationAzimuth = response[0];
			float orientationPitch = response[1];
			float orientationRoll = response[2];
			DeviceOrientation deviceOrientation = new DeviceOrientation(orientationAzimuth,
																		orientationPitch,
																		orientationRoll);

			return deviceOrientation;
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException("Getting battery status failed.", e);
		}
	}

	/**
	 * Gets the battery state of the device.
	 * 
	 * @return a member of the {@link BatteryState BatteryState} enumeration.
	 * @throws CommandFailedException
	 */
	public BatteryState getBatteryState() throws CommandFailedException
	{
		portForwardingService.forwardServicePort();
		Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_BATTERY_STATE);

		try
		{
			Integer serviceResponse = (Integer) serviceRequestHandler.request(serviceRequest);
			if (serviceResponse != -1)
			{
				BatteryState currentBatteryState = BatteryState.getStateById(serviceResponse);
				return currentBatteryState;
			}
			else
			{
				throw new CommandFailedException("The service could not retrieve the battery status.");
			}
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException(	"Getting battery status failed. See enclosed exception for more information.",
												e);
		}
	}

	/**
	 * Gets the connection type of the device.
	 * 
	 * @return a member of the {@link ConnectionType} enumeration.
	 * @throws CommandFailedException
	 */
	public ConnectionType getConnectionType() throws CommandFailedException
	{
		portForwardingService.forwardServicePort();
		Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_CONNECTION_TYPE);

		try
		{
			Integer serviceResponse = (Integer) serviceRequestHandler.request(serviceRequest);
			return ConnectionType.getById(serviceResponse);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException(	"Getting connection type failed. See enclosed exception for more information.",
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
	public void setWiFi(boolean state) throws CommandFailedException
	{
		portForwardingService.forwardServicePort();
		Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.SET_WIFI);
		Boolean[] arguments = new Boolean[] {state};
		serviceRequest.setArguments(arguments);

		try
		{
			serviceRequestHandler.request(serviceRequest);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException("Setting WiFi failed. See enclosed exception for more information.", e);
		}
	}

	/**
	 * Fetches the sensor acceleration readings of the device.
	 * 
	 * @return a {@link DeviceAcceleration} instance.
	 * @throws CommandFailedException
	 */
	public DeviceAcceleration getAcceleration() throws CommandFailedException
	{
		portForwardingService.forwardServicePort();
		Request<ServiceRequest> serviceRequest = new Request<ServiceRequest>(ServiceRequest.GET_ACCELERATION_READINGS);
		try
		{
			Float[] acceeleration = (Float[]) serviceRequestHandler.request(serviceRequest);
			DeviceAcceleration deviceAcceleration = new DeviceAcceleration(	acceeleration[0],
																			acceeleration[1],
																			acceeleration[2]);
			return deviceAcceleration;
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException(	"Getting acceleration failed. See enclosed exception for more information.",
												e);
		}
	}
}
