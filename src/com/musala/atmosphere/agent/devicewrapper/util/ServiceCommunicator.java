package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.rmi.RemoteException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.exception.InitializeServiceRequestHandlerFailedException;
import com.musala.atmosphere.agent.exception.NoFreePortAvailableException;
import com.musala.atmosphere.agent.exception.RemovePortForwardFailedException;
import com.musala.atmosphere.agent.exception.ServiceValidationFailedException;
import com.musala.atmosphere.agent.exception.StartAtmosphereServiceFailedException;
import com.musala.atmosphere.agent.exception.StopAtmosphereServiceFailedException;
import com.musala.atmosphere.agent.util.PortAllocator;
import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.ConnectionType;
import com.musala.atmosphere.commons.as.ServiceConstants;
import com.musala.atmosphere.commons.as.ServiceRequest;
import com.musala.atmosphere.commons.as.ServiceRequestType;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.util.IntentBuilder;
import com.musala.atmosphere.commons.util.IntentBuilder.IntentAction;

public class ServiceCommunicator
{
	private static final String ATMOSPHERE_SERVICE_COMPONENT = "com.musala.atmosphere.service/com.musala.atmosphere.service.AtmosphereService";

	private IDevice device;

	private IWrapDevice wrappedDevice;

	private int forwardedPort;

	private ServiceRequestHandler serviceRequesthandler;

	public ServiceCommunicator(IDevice device, IWrapDevice wrappedDevice)
	{
		this.device = device;
		this.wrappedDevice = wrappedDevice;
	}

	/**
	 * Forwards a local port to the ATMOSPHERE service's port on the wrapped device.
	 * 
	 * @throws ForwardServicePortFailedException
	 */
	public void forwardServicePort()
	{
		try
		{
			forwardedPort = PortAllocator.getFreePort();
			device.createForward(forwardedPort, ServiceConstants.SERVICE_PORT);
		}
		catch (TimeoutException | AdbCommandRejectedException | IOException | NoFreePortAvailableException e)
		{
			String errorMessage = String.format("Could not forward port for %s.", device.getSerialNumber());
			throw new ForwardServicePortFailedException(errorMessage, e);
		}
	}

	/**
	 * Removes the port forwarding.
	 * 
	 * @throws RemovePortForwardFailedException
	 */
	public void removeForward() throws RemovePortForwardFailedException
	{
		try
		{
			device.removeForward(forwardedPort, ServiceConstants.SERVICE_PORT);
		}
		catch (TimeoutException | AdbCommandRejectedException | IOException e)
		{
			String errorMessage = String.format("Could not remove port forwarding for %s.", device.getSerialNumber());
			throw new RemovePortForwardFailedException(errorMessage, e);
		}
	}

	/**
	 * Starts the Atmosphere service on the wrappedDevice.
	 * 
	 * @throws StartAtmosphereServiceFailedException
	 */
	public void startAtmosphereService()
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
			String errorMessage = String.format("Starting ATMOSPHERE service failed for %s.", device.getSerialNumber());
			throw new StartAtmosphereServiceFailedException(errorMessage, e);
		}
	}

	/**
	 * Stops the ATMOSPHERE service on the wrapped device.
	 * 
	 * @throws StopAtmosphereServiceFailedException
	 */
	public void stopAtmosphereService() throws StopAtmosphereServiceFailedException
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
			String errorMessage = String.format("Stopping ATMOSPHERE service failed for %s.", device.getSerialNumber());
			throw new StopAtmosphereServiceFailedException(errorMessage, e);
		}
	}

	/**
	 * Initializes the {@link ServiceRequestHandler} on the wrapped device.
	 * 
	 * @throws InitializeServiceRequestHandlerFailedException
	 */
	public void initializeServiceRequestHandler()
	{

		try
		{
			serviceRequesthandler = new ServiceRequestHandler(forwardedPort);
		}
		catch (ServiceValidationFailedException e)
		{
			String errorMessage = String.format("Service initialization failed for %s.", device.getSerialNumber());
			throw new InitializeServiceRequestHandlerFailedException(errorMessage, e);
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
		ServiceRequest serviceRequest = new ServiceRequest(ServiceRequestType.GET_BATTERY_LEVEL);

		try
		{
			int level = (Integer) serviceRequesthandler.request(serviceRequest);
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
		ServiceRequest serviceRequest = new ServiceRequest(ServiceRequestType.GET_POWER_STATE);
		boolean powerState;

		try
		{
			powerState = (Boolean) serviceRequesthandler.request(serviceRequest);
		}
		catch (ClassNotFoundException | IOException e)
		{
			// Redirect the exception to the server
			throw new CommandFailedException("Getting power state failed.", e);
		}
		return powerState;
	}

	/**
	 * Gets the battery state of the device.
	 * 
	 * @return a member of the {@link BatteryState BatteryState} enumeration.
	 * @throws CommandFailedException
	 */
	public BatteryState getBatteryState() throws CommandFailedException
	{
		ServiceRequest serviceRequest = new ServiceRequest(ServiceRequestType.GET_BATTERY_STATE);

		try
		{
			Integer serviceResponse = (Integer) serviceRequesthandler.request(serviceRequest);
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

	public ConnectionType getConnectionType() throws CommandFailedException
	{
		ServiceRequest serviceRequest = new ServiceRequest(ServiceRequestType.GET_CONNECTION_TYPE);

		try
		{
			Integer serviceResponse = (Integer) serviceRequesthandler.request(serviceRequest);
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
		ServiceRequest serviceRequest = new ServiceRequest(ServiceRequestType.SET_WIFI);
		Boolean[] arguments = new Boolean[1];

		try
		{
			if (state)
			{
				arguments[0] = true;
				serviceRequest.setArguments(arguments);
				serviceRequesthandler.request(serviceRequest);
			}
			else
			{
				arguments[0] = false;
				serviceRequest.setArguments(arguments);
				serviceRequesthandler.request(serviceRequest);
			}
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException("Setting WiFi failed. See enclosed exception for more information.", e);
		}
	}
}
