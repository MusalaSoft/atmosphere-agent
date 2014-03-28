package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;
import java.util.List;

import com.musala.atmosphere.agent.devicewrapper.AbstractWrapDevice;
import com.musala.atmosphere.agent.devicewrapper.util.PortForwardingService;
import com.musala.atmosphere.agent.exception.OnDeviceComponentInitializationException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentStartingException;
import com.musala.atmosphere.agent.exception.OnDeviceComponentValidationException;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorBridgeRequest;
import com.musala.atmosphere.commons.beans.SwipeDirection;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.gesture.Timeline;
import com.musala.atmosphere.commons.ui.UiElementDescriptor;

/**
 * Class that communicates with the ATMOSPHERE UIAutomator bridge.
 * 
 * @author yordan.petrov
 * 
 */
public class UIAutomatorBridgeCommunicator {
    private static final int START_GESTURE_PLAYER_WAIT = 4000; // milliseconds

    private static final String START_GESTURE_PLAYER_COMMAND = "uiautomator runtest AtmosphereUIAutomatorBridge.jar AtmosphereUIAutomatorBridgeLibs.jar -c com.musala.atmosphere.uiautomator.ActionDispatcher";

    private final AbstractWrapDevice wrappedDevice;

    private UIAutomatorBridgeRequestHandler uiAutomatorBridgeRequestHandler;

    private PortForwardingService portForwardingService;

    private String deviceSerialNumber;

    public UIAutomatorBridgeCommunicator(PortForwardingService portForwarder, AbstractWrapDevice wrappedDevice) {
        portForwardingService = portForwarder;

        int localPort = portForwardingService.getLocalForwardedPort();
        this.wrappedDevice = wrappedDevice;
        DeviceInformation deviceInformation = wrappedDevice.getDeviceInformation();
        deviceSerialNumber = deviceInformation.getSerialNumber();

        startUIAutomatorBridge();
        try {
            uiAutomatorBridgeRequestHandler = new UIAutomatorBridgeRequestHandler(portForwardingService, localPort);
        } catch (OnDeviceComponentValidationException e) {
            String errorMessage = String.format("UIAutomator bridge initialization failed for %s.", deviceSerialNumber);
            throw new OnDeviceComponentInitializationException(errorMessage, e);
        }
    }

    /**
     * Starts the Atmosphere UIAutomator bridge on the wrappedDevice.
     */
    private void startUIAutomatorBridge() {

        wrappedDevice.getShellCommandExecutor().executeInBackground(START_GESTURE_PLAYER_COMMAND);

        try {
            Thread.sleep(START_GESTURE_PLAYER_WAIT);
        } catch (InterruptedException e) {
            // cannot happen.
            e.printStackTrace();
        }

        // This exception fetching will probably be too soon.
        Throwable commandExecutionException = wrappedDevice.getShellCommandExecutor()
                                                           .getBackgroundExecutionException(START_GESTURE_PLAYER_COMMAND);
        if (commandExecutionException != null) {
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
    public void playGesture(List<Timeline> pointerTimelines) throws CommandFailedException {
        Object[] arguments = new Object[] {pointerTimelines};
        Request<UIAutomatorBridgeRequest> uiAutomatorBridgeRequest = new Request<UIAutomatorBridgeRequest>(UIAutomatorBridgeRequest.PLAY_GESTURE);
        uiAutomatorBridgeRequest.setArguments(arguments);

        try {
            uiAutomatorBridgeRequestHandler.request(uiAutomatorBridgeRequest);
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Gesture execution failed.", e);
        }
    }

    public void clearField(UiElementDescriptor descriptor) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor};
        Request<UIAutomatorBridgeRequest> uiAutomatorBridgeRequest = new Request<UIAutomatorBridgeRequest>(UIAutomatorBridgeRequest.CLEAR_FIELD);
        uiAutomatorBridgeRequest.setArguments(arguments);
        try {
            boolean response = (Boolean) uiAutomatorBridgeRequestHandler.request(uiAutomatorBridgeRequest);
            if (!response) {
                throw new CommandFailedException("Clearing element failed. UI element could not be found.");
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Clearing element failed.", e);
        }
    }

    public void swipeElement(UiElementDescriptor descriptor, SwipeDirection direction) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor, direction};
        Request<UIAutomatorBridgeRequest> uiAutomatorBridgeRequest = new Request<UIAutomatorBridgeRequest>(UIAutomatorBridgeRequest.ELEMENT_SWIPE);
        uiAutomatorBridgeRequest.setArguments(arguments);
        try {
            boolean response = (Boolean) uiAutomatorBridgeRequestHandler.request(uiAutomatorBridgeRequest);
            if (!response) {
                throw new CommandFailedException("Swiping element failed. UI element could not be found.");
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new CommandFailedException("Swiping element failed.", e);
        }
    }
}
