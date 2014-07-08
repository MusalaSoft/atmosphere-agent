package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.FileTransferService;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.commons.ScrollDirection;
import com.musala.atmosphere.commons.ad.FileTransferConstants;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorConstants;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorRequest;
import com.musala.atmosphere.commons.ad.util.FileObjectTransferManager;
import com.musala.atmosphere.commons.beans.SwipeDirection;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.gesture.Timeline;
import com.musala.atmosphere.commons.ui.UiElementDescriptor;

/**
 * Class that communicates with the ATMOSPHERE UIAutomator-based on-device component.
 * 
 * @author yordan.petrov
 * 
 */
public class UIAutomatorCommunicator {
    private static final Logger LOGGER = Logger.getLogger(UIAutomatorCommunicator.class);

    private final ShellCommandExecutor executor;

    private final FileTransferService transferService;

    private FileObjectTransferManager fileObjectTransferManager = new FileObjectTransferManager();

    // TODO Maybe add run validation in the future

    public UIAutomatorCommunicator(ShellCommandExecutor executor, FileTransferService transferService) {
        this.executor = executor;
        this.transferService = transferService;
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
        startUIAutomatorProcess(UIAutomatorRequest.PLAY_GESTURE, arguments);
    }

    public void clearField(UiElementDescriptor descriptor) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor};
        startUIAutomatorProcess(UIAutomatorRequest.CLEAR_FIELD, arguments);
    }

    public void swipeElement(UiElementDescriptor descriptor, SwipeDirection direction) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor, direction};
        startUIAutomatorProcess(UIAutomatorRequest.ELEMENT_SWIPE, arguments);
    }

    /**
     * Starts a process on the UIAutomatorBridge that executes scrolling in a direction determined by the
     * scrollDirection parameter.
     * 
     * @param scrollDirection
     *        determine scrolling direction
     * @param viewDescriptor
     *        descriptor of the scrollable view
     * @param maxSwipes
     *        maximum swipes to perform a scroll action
     * @param maxSteps
     *        steps to be executed when scrolling, steps controls the speed
     * @param isVertical
     *        true if the view has vertical orientation, false otherwise
     * @param deviceSerialNumber
     *        serial number of the current device
     * @return true if scrolled is performed else false
     * @throws CommandFailedException
     */
    public boolean scrollToDirection(ScrollDirection scrollDirection,
                                     UiElementDescriptor viewDescriptor,
                                     Integer maxSwipes,
                                     Integer maxSteps,
                                     Boolean isVertical,
                                     String deviceSerialNumber) throws CommandFailedException {
        Object[] arguments = new Object[] {scrollDirection, viewDescriptor, maxSwipes, maxSteps, isVertical};
        startUIAutomatorProcess(UIAutomatorRequest.SCROLL_TO_DIRECTION, arguments);

        return (boolean) getResponse(deviceSerialNumber);
    }

    /**
     * Starts a process on the UIAutomatorBridge that executes scrolling into a view or a contained text.
     * 
     * @param viewDescriptor
     *        descriptor of the scrollable view
     * @param innerViewDescriptor
     *        descriptor of the view into which will be scrolled
     * @param isVertical
     *        true if the view has vertical orientation, false otherwise
     * @param deviceSerialNumber
     *        serial number of the current device
     * @return true if scrolled is performed else false
     * @throws CommandFailedException
     */
    public boolean scrollIntoView(UiElementDescriptor viewDescriptor,
                                  UiElementDescriptor innerViewDescriptor,
                                  Boolean isVertical,
                                  String deviceSerialNumber) throws CommandFailedException {
        Object[] arguments = new Object[] {viewDescriptor, innerViewDescriptor, isVertical};
        startUIAutomatorProcess(UIAutomatorRequest.SCROLL_INTO_VIEW, arguments);

        return (boolean) getResponse(deviceSerialNumber);
    }

    /**
     * Starts a process on the UIAutomatorBridge that waits for a UI element to appear on the screen with a given
     * timeout.
     * 
     * @param descriptor
     *        - the descriptor of the UI element.
     * @param timeout
     *        - the given timeout.
     * @param deviceSerialNumber
     *        - the serial number of the used device.
     * @return - Returns <code>true</code> if the element exists.<br>
     *         <code>false</code> if there isn't such element on the screen. <br>
     * 
     * @throws CommandFailedException
     */
    public boolean waitForExists(UiElementDescriptor descriptor, Integer timeout, String deviceSerialNumber)
        throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor, timeout};
        startUIAutomatorProcess(UIAutomatorRequest.WAIT_FOR_EXISTS, arguments, timeout);

        return (boolean) getResponse(deviceSerialNumber);
    }

    /**
     * Starts a process on the UIAutomatorBridge that waits for a UI element to disappear on the screen with a given
     * timeout.
     * 
     * @param descriptor
     *        - the descriptor of the UI element.
     * @param timeout
     *        - the given timeout.
     * @param deviceSerialNumber
     *        - the serial number of the used device.
     * @return <code>true</code> if the element disappears.<br>
     *         <code>false</code> if there is such element on the screen after the timeout. <br>
     * 
     * @throws CommandFailedException
     */
    public boolean waitUntilGone(UiElementDescriptor descriptor, Integer timeout, String deviceSerialNumber)
        throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor, timeout};
        startUIAutomatorProcess(UIAutomatorRequest.WAIT_UNTIL_GONE, arguments, timeout);

        return (boolean) getResponse(deviceSerialNumber);
    }

    /**
     * Starts a process on the UIAutomatorBridge that opens the notification bar on the device.
     * 
     * @param deviceSerialNumber
     *        - the serial number of the used device
     * @return true if the notification bar has been successfully opened, false otherwise
     * @throws CommandFailedException
     */
    public boolean openNotificationBar(String deviceSerialNumber) throws CommandFailedException {
        startUIAutomatorProcess(UIAutomatorRequest.OPEN_NOTIFICATION_BAR, null);

        return (boolean) getResponse(deviceSerialNumber);
    }

    /**
     * Starts a process on the UI automator bridge that Waits for a window content update event to occur. If a package
     * name for the window is specified, but the current window does not have the same package name, the function
     * returns immediately.
     * 
     * @param packageName
     *        - the specified window package name (can be null). If null, a window update from any front-end window will
     *        end the wait
     * @param timeout
     *        - the timeout for the operation
     * @param deviceSerialNumber
     *        - the serial number of the device on which the operation will be executed
     * @return <code>true</code> if a window update occurred, <code>false</code> if timeout has elapsed or if the
     *         current window does not have the specified package name
     * @throws CommandFailedException
     */
    public boolean waitForWindowUpdate(String packageName, int timeout, String deviceSerialNumber)
        throws CommandFailedException {
        Object[] arguments = new Object[] {packageName, timeout};
        startUIAutomatorProcess(UIAutomatorRequest.WAIT_FOR_WINDOW_UPDATE, arguments, timeout);

        return (boolean) getResponse(deviceSerialNumber);
    }

    /**
     * Used for reading a response from the pulled file.
     * 
     * @param deviceSerialNumber
     *        - the serial number of the used device.
     * @return
     * @throws CommandFailedException
     */
    private Object getResponse(String deviceSerialNumber) throws CommandFailedException {
        // TODO consider using UUID to create unique response file names instead of using the device serial number
        String localFileName = FileTransferConstants.RESPONSE_FILE_NAME + deviceSerialNumber;
        transferService.pullFile(FileTransferConstants.RESPONSE_FILE_NAME, localFileName);

        try {
            return fileObjectTransferManager.readObjectFromFile(localFileName);
        } catch (ClassNotFoundException | IOException e) {
            String message = "Failed to read the response from the file";
            LOGGER.error(message, e);
            throw new CommandFailedException(message, e);
        }
    }

    /**
     * Starts a process on the UI automator corresponding to the passed parameters, with the default execution timeout.
     * 
     * @param requestType
     *        - the type of the request to start
     * @param arguments
     *        - arguments for the request
     * @throws CommandFailedException
     */
    private void startUIAutomatorProcess(UIAutomatorRequest requestType, Object[] arguments)
        throws CommandFailedException {
        startUIAutomatorProcess(requestType, arguments, -1);
    }

    /**
     * Starts a process on the UI automator corresponding to the passed parameters.
     * 
     * @param requestType
     *        - the type of the request to start
     * @param arguments
     *        - arguments for the request
     * @param timeout
     *        - a timeout for the request, if any non-positive value is passed, the request will be executed with the
     *        default timeout
     * @throws CommandFailedException
     */
    private void startUIAutomatorProcess(UIAutomatorRequest requestType, Object[] arguments, int timeout)
        throws CommandFailedException {
        Request<UIAutomatorRequest> request = new Request<UIAutomatorRequest>(requestType);
        request.setArguments(arguments);

        UIAutomatorProcessStarter starter = new UIAutomatorProcessStarter();
        starter.attachObject(UIAutomatorConstants.PARAM_REQUEST, request);

        if (timeout > 0) {
            starter.run(executor, transferService, timeout);
        } else {
            starter.run(executor, transferService);
        }
    }
}
