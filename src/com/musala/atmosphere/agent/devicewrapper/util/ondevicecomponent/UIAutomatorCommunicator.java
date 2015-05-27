package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.BackgroundShellCommandExecutor;
import com.musala.atmosphere.agent.exception.OnDeviceComponentStartingException;
import com.musala.atmosphere.commons.ScrollDirection;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorRequest;
import com.musala.atmosphere.commons.beans.SwipeDirection;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.gesture.Gesture;
import com.musala.atmosphere.commons.gesture.Timeline;
import com.musala.atmosphere.commons.ui.UiElementDescriptor;
import com.musala.atmosphere.commons.ui.selector.UiElementSelector;
import com.musala.atmosphere.commons.ui.tree.AccessibilityElement;
import com.musala.atmosphere.commons.util.structure.tree.Tree;

/**
 * Class that communicates with the ATMOSPHERE UIAutomator-based on-device component.
 * 
 * @author yordan.petrov
 * 
 */
public class UIAutomatorCommunicator extends DeviceCommunicator<UIAutomatorRequest> {
    private static final Logger LOGGER = Logger.getLogger(UIAutomatorCommunicator.class);

    public UIAutomatorCommunicator(DeviceRequestSender<UIAutomatorRequest> requestSender,
            BackgroundShellCommandExecutor commandExecutor,
            String serialNumber) {
        super(requestSender, commandExecutor, serialNumber);
    }

    /**
     * Plays the passed list of {@link Timeline} instances.
     * 
     * @param pointerTimelines
     *        - a list of {@link Timeline} instances.
     * 
     * @throws CommandFailedException
     */
    public void playGesture(Gesture gesture) throws CommandFailedException {
        Object[] arguments = new Object[] {gesture};

        requestAction(UIAutomatorRequest.PLAY_GESTURE, arguments);
    }

    public void clearField(UiElementDescriptor descriptor) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor};
        requestAction(UIAutomatorRequest.CLEAR_FIELD, arguments);
    }

    public void swipeElement(UiElementDescriptor descriptor, SwipeDirection direction) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor, direction};
        requestAction(UIAutomatorRequest.ELEMENT_SWIPE, arguments);
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
     *        <code>true</code> if the view has vertical orientation, <code>false</code> otherwise
     * @return <code>true</code> if scrolled is performed else <code>false</code>
     * @throws CommandFailedException
     */
    public boolean scrollToDirection(ScrollDirection scrollDirection,
                                     UiElementDescriptor viewDescriptor,
                                     Integer maxSwipes,
                                     Integer maxSteps,
                                     Boolean isVertical) throws CommandFailedException {
        Object[] arguments = new Object[] {scrollDirection, viewDescriptor, maxSwipes, maxSteps, isVertical};

        return (boolean) requestActionWithResponse(UIAutomatorRequest.SCROLL_TO_DIRECTION, arguments);
    }

    /**
     * Starts a process on the UIAutomatorBridge that waits for a UI element to appear on the screen with a given
     * timeout.
     * 
     * @param descriptor
     *        - the descriptor of the UI element
     * @param timeout
     *        - the given timeout
     * @return - returns <code>true</code> if the element exists or <code>false</code> if there isn't such element on
     *         the screen
     * 
     * @throws CommandFailedException
     */
    public boolean waitForExists(UiElementDescriptor descriptor, Integer timeout) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor, timeout};

        return (boolean) requestActionWithResponse(UIAutomatorRequest.WAIT_FOR_EXISTS, arguments);
    }

    /**
     * Starts a process on the UIAutomatorBridge that waits for a UI element to disappear on the screen with a given
     * timeout.
     * 
     * @param descriptor
     *        - the descriptor of the UI element
     * @param timeout
     *        - the given timeout.
     * @return <code>true</code> if the element disappears or <code>false</code> if there is such element on the screen
     *         after the timeout
     * 
     * @throws CommandFailedException
     */
    public boolean waitUntilGone(UiElementDescriptor descriptor, Integer timeout) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor, timeout};

        return (boolean) requestActionWithResponse(UIAutomatorRequest.WAIT_UNTIL_GONE, arguments);
    }

    /**
     * Starts a process on the UIAutomatorBridge that opens the notification bar on the device.
     * 
     * @return <code>true</code> if the notification bar has been successfully opened, <code>false</code> otherwise
     * @throws CommandFailedException
     */
    public boolean openNotificationBar() throws CommandFailedException {
        return (boolean) requestActionWithResponse(UIAutomatorRequest.OPEN_NOTIFICATION_BAR, null);
    }

    /**
     * Starts a process on the UIAutomatorBridge that opens the quick settings on the device.
     * 
     * @return <code>true</code> if the quick settings have been successfully opened, <code>false</code> otherwise
     * @throws CommandFailedException
     */
    public boolean openQuickSettings() throws CommandFailedException {
        return (boolean) requestActionWithResponse(UIAutomatorRequest.OPEN_QUICK_SETTINGS, null);
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
     * @return <code>true</code> if a window update occurred, <code>false</code> if timeout has elapsed or if the
     *         current window does not have the specified package name
     * @throws CommandFailedException
     */
    public boolean waitForWindowUpdate(String packageName, int timeout) throws CommandFailedException {
        Object[] arguments = new Object[] {packageName, timeout};

        return (boolean) requestActionWithResponse(UIAutomatorRequest.WAIT_FOR_WINDOW_UPDATE, arguments);
    }

    /**
     * Sends a request for dumping the screen of the device to a given XML file.
     * 
     * @param remoteFile
     *        - the name of the XML file
     * @throws CommandFailedException
     *         - if request fails
     */
    public void getUiDumpXml(String remoteFile) throws CommandFailedException {
        Object[] arguments = new Object[] {remoteFile};

        requestAction(UIAutomatorRequest.GET_UI_DUMP_XML, arguments);
    }

    /**
     * Sends a request for building a tree representation of the active screen of the device.
     * 
     * @param visibleOnly
     *        - if <code>true</code> only the visible nodes will be used; if <code>false</code> all nodes will be used
     * @return a tree representation of the screen
     * @throws CommandFailedException
     *         - if request fails
     */
    @SuppressWarnings("unchecked")
    public Tree<AccessibilityElement> getUiTree(boolean visibleOnly) throws CommandFailedException {
        Object[] arguments = new Object[] {visibleOnly};

        return (Tree<AccessibilityElement>) requestActionWithResponse(UIAutomatorRequest.GET_UI_TREE, arguments);
    }

    /**
     * Sends a request for getting all {@link AccessibilityElement UI elements} present on the screen and matching the
     * given selector.
     * 
     * @param visibleOnly
     *        - if <code>true</code> only the visible elements will be searched; if <code>false</code> all elements will
     *        be searched
     * @return a list containing all found elements matching the selector
     * @throws CommandFailedException
     *         if the request fails
     */
    @SuppressWarnings("unchecked")
    public List<AccessibilityElement> getUiElements(UiElementSelector selector, Boolean visibleOnly)
        throws CommandFailedException {
        Object[] arguments = new Object[] {selector, visibleOnly};

        return (List<AccessibilityElement>) requestActionWithResponse(UIAutomatorRequest.GET_UI_ELEMENTS, arguments);
    }

    /**
     * Requests the given {@link UIAutomatorRequest action} and returns the response.
     * 
     * @param requestType
     *        - the {@link UIAutomatorRequest requested action}
     * @param arguments
     *        - the arguments that are passed with the request
     * @return the response from the request
     * @throws CommandFailedException
     *         - if the request fails
     */
    private Object requestActionWithResponse(UIAutomatorRequest requestType, Object[] arguments)
        throws CommandFailedException {
        Request<UIAutomatorRequest> automatorRequest = new Request<UIAutomatorRequest>(requestType);
        automatorRequest.setArguments(arguments);

        try {
            return requestSender.request(automatorRequest);
        } catch (ClassNotFoundException | IOException e) {
            String messageFormat = "Failed request %.";
            String message = String.format(messageFormat, requestType.toString());
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
    private void requestAction(UIAutomatorRequest requestType, Object[] arguments) throws CommandFailedException {
        Request<UIAutomatorRequest> automatorRequest = new Request<UIAutomatorRequest>(requestType);
        automatorRequest.setArguments(arguments);

        try {
            requestSender.request(automatorRequest);
        } catch (ClassNotFoundException | IOException e) {
            String messageFormat = "Failed request %.";
            String message = String.format(messageFormat, requestType.toString());
            LOGGER.error(message, e);
            throw new CommandFailedException(message, e);
        }
    }

    @Override
    public void startComponent() {
        UIAutomatorProcessStarter processStarter = new UIAutomatorProcessStarter();
        try {
            processStarter.runInBackground(shellCommandExecutor);
        } catch (CommandFailedException e) {
            String errorMessage = String.format("Starting ATMOSPHERE gesture player failed for %s.", deviceSerialNumber);
            throw new OnDeviceComponentStartingException(errorMessage, e);
        }

    }

    @Override
    public void stopComponent() {
        Request<UIAutomatorRequest> automatorRequest = new Request<UIAutomatorRequest>(UIAutomatorRequest.STOP);

        try {
            requestSender.request(automatorRequest);
        } catch (ClassNotFoundException | IOException | CommandFailedException e) {
            // Redirect the exception to the server
            LOGGER.warn("Stoopping ATMOSPHERE gesture player failed.", e);
        }
    }

    @Override
    public void validateRemoteServer() {
        Request<UIAutomatorRequest> validationRequest = new Request<UIAutomatorRequest>(UIAutomatorRequest.VALIDATION);
        validateRemoteServer(validationRequest);
    }
}
