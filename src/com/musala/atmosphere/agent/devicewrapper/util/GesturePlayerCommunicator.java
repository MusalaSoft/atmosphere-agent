package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.rmi.RemoteException;

import com.musala.atmosphere.agent.devicewrapper.PortForwardingService;
import com.musala.atmosphere.agent.exception.AtmosphereOnDeviceComponentStartFailedException;
import com.musala.atmosphere.agent.exception.InitializeServiceRequestHandlerFailedException;
import com.musala.atmosphere.agent.exception.ServiceValidationFailedException;
import com.musala.atmosphere.commons.CommandFailedException;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.gestureplayer.GesturePlayerRequest;
import com.musala.atmosphere.commons.ad.gestureplayer.Timeline;
import com.musala.atmosphere.commons.sa.IWrapDevice;

/**
 * Class that communicates with the ATMOSPHERE gesture player.
 * 
 * @author yordan.petrov
 * 
 */
public class GesturePlayerCommunicator
{
	private static final String START_GESTURE_PLAYER_COMMAND = "uiautomator runtest AtmosphereGesturePlayer.jar AtmosphereGesturePlayerLibs.jar -c com.musala.atmosphere.gestureplayer.ConnectionInitializer &";

	private final IWrapDevice wrappedDevice;

	private GesturePlayerRequestHandler gesturePlayerRequestHandler;

	private PortForwardingService portForwardingService;

	private String deviceSerialNumber;

	public GesturePlayerCommunicator(PortForwardingService portForwarder, IWrapDevice wrappedDevice)
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

		startGesturePlayer();
		portForwardingService.forwardGesturePlayerPort();
		try
		{
			gesturePlayerRequestHandler = new GesturePlayerRequestHandler(portForwardingService, localPort);
		}
		catch (ServiceValidationFailedException e)
		{
			String errorMessage = String.format("Gesture player initialization failed for %s.", deviceSerialNumber);
			throw new InitializeServiceRequestHandlerFailedException(errorMessage, e);
		}
	}

	/**
	 * Starts the Atmosphere gesture player on the wrappedDevice.
	 */
	private void startGesturePlayer()
	{
		try
		{
			wrappedDevice.executeShellCommand(START_GESTURE_PLAYER_COMMAND);
		}
		catch (RemoteException e)
		{
			String errorMessage = String.format("Starting ATMOSPHERE gesture player failed for %s.", deviceSerialNumber);
			throw new AtmosphereOnDeviceComponentStartFailedException(errorMessage, e);
		}
		catch (CommandFailedException e)
		{
			// TODO: Check the returned error message.
		}
	}

	/**
	 * Adds a {@link Timeline} instance that will be used for touch event generation.
	 * 
	 * @param timeline
	 *        - the {@link Timeline} instance that will be added.
	 * @throws CommandFailedException
	 */
	public void insertTimeline(Timeline timeline) throws CommandFailedException
	{
		Request<GesturePlayerRequest> gesturePlayerRequest = new Request<GesturePlayerRequest>(GesturePlayerRequest.INSERT_TIMELINE);
		Timeline[] arguments = new Timeline[] {timeline};
		gesturePlayerRequest.setArguments(arguments);

		try
		{
			gesturePlayerRequestHandler.request(gesturePlayerRequest);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException(	"Inserting timeline failed. See enclosed exception for more information.",
												e);
		}
	}

	/**
	 * Marks this moment as the moment when the execution starts.
	 * 
	 * @throws CommandFailedException
	 */
	public void markTimelineStart() throws CommandFailedException
	{
		Request<GesturePlayerRequest> gesturePlayerRequest = new Request<GesturePlayerRequest>(GesturePlayerRequest.MARK_TIMELINE_START);

		try
		{
			gesturePlayerRequestHandler.request(gesturePlayerRequest);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException(	"Marking timeline start failed. See enclosed exception for more information.",
												e);
		}
	}

	/**
	 * Calculates the pointer position on which each {@link Timeline} should be at the current moment and then sends
	 * adequate motion event to the device's input manager.
	 * 
	 * @throws CommandFailedException
	 */
	public void act() throws CommandFailedException
	{
		Request<GesturePlayerRequest> gesturePlayerRequest = new Request<GesturePlayerRequest>(GesturePlayerRequest.ACT);

		try
		{
			gesturePlayerRequestHandler.request(gesturePlayerRequest);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException("Playing gesture failed. See enclosed exception for more information.", e);
		}
	}

	/**
	 * Plays the inserted {@link Timeline} instances.
	 * 
	 * @throws CommandFailedException
	 */
	public void playGesture() throws CommandFailedException
	{
		Request<GesturePlayerRequest> gesturePlayerRequest = new Request<GesturePlayerRequest>(GesturePlayerRequest.PLAY_GESTURE);

		try
		{
			gesturePlayerRequestHandler.request(gesturePlayerRequest);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new CommandFailedException("Playing gesture failed. See enclosed exception for more information.", e);
		}
	}
}
