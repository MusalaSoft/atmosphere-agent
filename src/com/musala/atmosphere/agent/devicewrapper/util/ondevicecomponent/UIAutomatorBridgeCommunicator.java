package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import com.musala.atmosphere.agent.devicewrapper.AbstractWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.exception.OnDeviceComponentInitializationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentStartingException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentValidationException;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorBridgeRequest;
import com.musala.atmosphere.commons.gesture.Timeline;

/**
 * Class that communicates with the ATMOSPHERE UIAutomator bridge.
 * 
 * @author yordan.petrov
 * 
 */
public class UIAutomatorBridgeCommunicator
{
	private static final int START_GESTURE_PLAYER_WAIT = 2000; // milliseconds

	private static final String START_GESTURE_PLAYER_COMMAND = "uiautomator runtest AtmosphereUIAutomatorBridge.jar AtmosphereUIAutomatorBridgeLibs.jar -c com.musala.atmosphere.uiautomator.ConnectionInitializer";

	private final AbstractWrapDevice wrappedDevice;

	private UIAutomatorBridgeRequestHandler uiAutomatorBridgeRequestHandler;

	private PortForwardingService portForwardingService;

	private String deviceSerialNumber;

	public UIAutomatorBridgeCommunicator(PortForwardingService portForwarder, AbstractWrapDevice wrappedDevice)
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

		startUIAutomatorBridge();
		try
		{
			uiAutomatorBridgeRequestHandler = new UIAutomatorBridgeRequestHandler(portForwardingService, localPort);
		}
		catch (OnDeviceComponentValidationException e)
		{
			String errorMessage = String.format("UIAutomator bridge initialization failed for %s.", deviceSerialNumber);
			throw new OnDeviceComponentInitializationException(errorMessage, e);
		}
	}

	/**
	 * Starts the Atmosphere UIAutomator bridge on the wrappedDevice.
	 */
	private void startUIAutomatorBridge()
	{

		wrappedDevice.executeBackgroundShellCommand(START_GESTURE_PLAYER_COMMAND);

		try
		{
			Thread.sleep(START_GESTURE_PLAYER_WAIT);
		}
		catch (InterruptedException e)
		{
			// cannot happen.
			e.printStackTrace();
		}

		// This exception fetching will probably be too soon.
		Exception commandExecutionException = wrappedDevice.getBackgroundShellCommandExecutionException(START_GESTURE_PLAYER_COMMAND);
		if (commandExecutionException != null)
		{
			String errorMessage = String.format("Starting ATMOSPHERE UIAutomator bridge failed for %s.",
												deviceSerialNumber);
			throw new OnDeviceComponentStartingException(errorMessage, commandExecutionException);
		}
	}

	/**
	 * Plays the passed list of {@link Timeline} instances.
	 * 
	 * @param pointerTimelines
	 *        - a list of {@link Timeline} instances.
	 * 
	 * @throws CommandFailedException
	 */
	public void playGesture(List<Timeline> pointerTimelines) throws CommandFailedException
	{
		Object[] arguments = new Object[] {pointerTimelines};
		Request<UIAutomatorBridgeRequest> uiAutomatorBridgeRequest = new Request<UIAutomatorBridgeRequest>(UIAutomatorBridgeRequest.PLAY_GESTURE);
		uiAutomatorBridgeRequest.setArguments(arguments);

		try
		{
			uiAutomatorBridgeRequestHandler.request(uiAutomatorBridgeRequest);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException("Gesture execution failed.", e);
		}
	}
}
