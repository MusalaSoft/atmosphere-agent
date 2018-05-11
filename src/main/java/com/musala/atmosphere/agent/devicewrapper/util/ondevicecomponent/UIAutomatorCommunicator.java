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
import com.musala.atmosphere.commons.ui.UiElementPropertiesContainer;
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
     * Plays the passed list of Gesture instances.
     *
     * @param gesture
     *        - a Gesture instance.
     *
     * @throws CommandFailedException
     *         thrown when play gesture fails
     */
    public void playGesture(Gesture gesture) throws CommandFailedException {
        Object[] arguments = new Object[] {gesture};

        requestAction(UIAutomatorRequest.PLAY_GESTURE, arguments);
    }

    /**
     * Starts a process on the UiAutomatorBridge that clears an EditText field containing the given properties.
     *
     * @param propertiesContainer
     *        - the properties by which the UI element will be selected
     * @throws CommandFailedException
     *         if the request fails
     */
    @Deprecated
    public void clearField(UiElementPropertiesContainer propertiesContainer) throws CommandFailedException {
        Object[] arguments = new Object[] {propertiesContainer};
        requestAction(UIAutomatorRequest.CLEAR_FIELD, arguments);
    }

    /**
     * Starts a process on the UiAutomatorBridge that executes a swipe gesture by given direction.
     *
     * @param propertiesContainer
     *        - the properties container of the UI element
     * @param direction
     *        - determine swipe direction
     * @throws CommandFailedException
     *         if the request fails
     */
    public void swipeElement(UiElementPropertiesContainer propertiesContainer, SwipeDirection direction)
        throws CommandFailedException {
        Object[] arguments = new Object[] {propertiesContainer, direction};
        requestAction(UIAutomatorRequest.ELEMENT_SWIPE, arguments);
    }

    /**
     * Starts a process on the UIAutomatorBridge that executes scrolling in a direction determined by the
     * scrollDirection parameter.
     *
     * @param scrollDirection
     *        determine scrolling direction
     * @param propertiesContainer
     *        - the properties container of the scrollable view
     * @param maxSwipes
     *        maximum swipes to perform a scroll action
     * @param maxSteps
     *        steps to be executed when scrolling, steps controls the speed
     * @param isVertical
     *        <code>true</code> if the view has vertical orientation, <code>false</code> otherwise
     * @return <code>true</code> if scrolled is performed else <code>false</code>
     * @throws CommandFailedException
     *         thrown when getting the running process fails
     */
    public boolean scrollToDirection(ScrollDirection scrollDirection,
                                     UiElementPropertiesContainer propertiesContainer,
                                     Integer maxSwipes,
                                     Integer maxSteps,
                                     Boolean isVertical)
        throws CommandFailedException {
        Object[] arguments = new Object[] {scrollDirection, propertiesContainer, maxSwipes, maxSteps, isVertical};

        return (boolean) requestActionWithResponse(UIAutomatorRequest.SCROLL_TO_DIRECTION, arguments);
    }

    /**
     * Starts a process on the UIAutomatorBridge that waits for a UI element to appear on the screen with a given
     * timeout.
     *
     * @param propertiesContainer
     *        - the properties container of the expected UI element
     * @param timeout
     *        - the given timeout
     * @return - returns <code>true</code> if the element exists or <code>false</code> if there isn't such element on
     *         the screen
     *
     * @throws CommandFailedException
     *         thrown when the request fails
     */
    public boolean waitForExists(UiElementPropertiesContainer propertiesContainer, Integer timeout)
        throws CommandFailedException {
        Object[] arguments = new Object[] {propertiesContainer, timeout};

        return (boolean) requestActionWithResponse(UIAutomatorRequest.WAIT_FOR_EXISTS, arguments);
    }

    /**
     * Starts a process on the UIAutomatorBridge that waits for a UI element to disappear on the screen with a given
     * timeout.
     *
     * @param propertiesContainer
     *        - the properties container of the UI element
     * @param timeout
     *        - the given timeout.
     * @return <code>true</code> if the element disappears or <code>false</code> if there is such element on the screen
     *         after the timeout
     *
     * @throws CommandFailedException
     *         throw when the request fails
     */
    public boolean waitUntilGone(UiElementPropertiesContainer propertiesContainer, Integer timeout)
        throws CommandFailedException {
        Object[] arguments = new Object[] {propertiesContainer, timeout};

        return (boolean) requestActionWithResponse(UIAutomatorRequest.WAIT_UNTIL_GONE, arguments);
    }

    /**
     * Starts a process on the UIAutomatorBridge that opens the notification bar on the device.
     *
     * @return <code>true</code> if the notification bar has been successfully opened, <code>false</code> otherwise
     * @throws CommandFailedException
     *         thrown when fails to open notification bar
     */
    public boolean openNotificationBar() throws CommandFailedException {
        return (boolean) requestActionWithResponse(UIAutomatorRequest.OPEN_NOTIFICATION_BAR, null);
    }

    /**
     * Starts a process on the UIAutomatorBridge that opens the quick settings on the device.
     *
     * @return <code>true</code> if the quick settings have been successfully opened, <code>false</code> otherwise
     * @throws CommandFailedException
     *         throw when the request fails
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
     *         thrown when the request fails
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
     *         if request fails
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
     *         if request fails
     */
    @SuppressWarnings("unchecked")
    public Tree<AccessibilityElement> getUiTree(boolean visibleOnly) throws CommandFailedException {
        Object[] arguments = new Object[] {visibleOnly};

        return (Tree<AccessibilityElement>) requestActionWithResponse(UIAutomatorRequest.GET_UI_TREE, arguments);
    }

    /**
     * Sends a request for obtaining the text of the last detected toast message of the device.
     *
     * @return the text of the last toast message or <code>null</code> if such is not detected yet
     * @throws CommandFailedException
     *         if request fails
     */
    public Object getLastToast() throws CommandFailedException {
        Object[] arguments = new Object[] {};

        return requestActionWithResponse(UIAutomatorRequest.GET_LAST_TOAST, arguments);
    }

    /**
     * Sends a request for getting all {@link AccessibilityElement UI elements} present on the screen and matching the
     * given selector.
     *
     * @param selector
     *        - the element's selector
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
     * Sends a request for getting all {@link AccessibilityElement child UI elements} of the {@link AccessibilityElement
     * element} passed as argument. Returned elements must match all properties contained in the given
     * {@link UiElementSelector selector}.
     *
     * @param parentElement
     *        - {@link AccessibilityElement accessibility element} whose children will be traversed
     * @param selector
     *        - {@link UiElementSelector element selector} containing properties used for matching the traversed
     *        children
     * @param directChildrenOnly
     *        - if <code>true</code> only direct children will be traversed, else all successors will be traversed
     * @param visibleOnly
     *        - if <code>true</code> only the visible elements will be searched; if <code>false</code> all elements will
     *        be searched
     * @return a list containing all children found matching the given selector
     * @throws CommandFailedException
     *         if the request fails
     */
    @SuppressWarnings("unchecked")
    public List<AccessibilityElement> getChildren(AccessibilityElement parentElement,
                                                  UiElementSelector selector,
                                                  Boolean directChildrenOnly,
                                                  Boolean visibleOnly)
        throws CommandFailedException {
        Object[] requestArguments = new Object[] {parentElement, selector, directChildrenOnly, visibleOnly};

        return (List<AccessibilityElement>) requestActionWithResponse(UIAutomatorRequest.GET_CHILDREN,
                                                                      requestArguments);
    }

    /**
     * Sends a request for validating an element presence on the screen.
     *
     * @param element
     *        - the wrapper around the searched element
     * @param visibleOnly
     *        - if <code>true</code> only the visible nodes will be used; if <code>false</code> all nodes will be used
     * @return <code>true</code> if the element is present on the screen; <code>false</code> otherwise
     * @throws CommandFailedException
     *         - if request fails
     */
    public boolean isElementPresent(AccessibilityElement element, boolean visibleOnly) throws CommandFailedException {
        Object[] arguments = new Object[] {element, visibleOnly};

        return (boolean) requestActionWithResponse(UIAutomatorRequest.CHECK_ELEMENT_PRESENCE, arguments);
    }

    /**
     * Sends an {@link UIAutomatorRequest UI automator request} for executing XPath queries on the screen hierarchy.
     *
     * @param xpathQuery
     *        - XPath query to be executed
     * @param visibleOnly
     *        - if <code>true</code> only the visible nodes will be used; if <code>false</code> all nodes will be used
     * @return {@link List list} of {@link AccessibilityElement elements} that matched the executed XPath query
     * @throws CommandFailedException
     *         if request fails
     */
    @SuppressWarnings("unchecked")
    public List<AccessibilityElement> executeXpathQuery(String xpathQuery, Boolean visibleOnly)
        throws CommandFailedException {
        Object[] arguments = new Object[] {xpathQuery, visibleOnly};

        return (List<AccessibilityElement>) requestActionWithResponse(UIAutomatorRequest.EXECUTE_XPATH_QUERY,
                                                                      arguments);
    }

    /**
     * Sends an {@link UIAutomatorRequest UI automator request} for executing XPath queries on the screen hierarchy by a
     * given local root.
     *
     * @param xpathQuery
     *        - XPath query to be executed
     * @param visibleOnly
     *        - if <code>true</code> only the visible nodes will be used; if <code>false</code> all nodes will be used
     * @param localRoot
     *        - local root relative to some {@link AccessibilityElement element} from which the query will be executed
     * @return {@link List list} of {@link AccessibilityElement elements} that matched the executed XPath query
     * @throws CommandFailedException
     *         if request fails
     */
    @SuppressWarnings("unchecked")
    public List<AccessibilityElement> executeXpathQueryOnLocalRoot(String xpathQuery,
                                                                   boolean visibleOnly,
                                                                   AccessibilityElement localRoot)
        throws CommandFailedException {
        Object[] arguments = new Object[] {xpathQuery, visibleOnly, localRoot};

        return (List<AccessibilityElement>) requestActionWithResponse(UIAutomatorRequest.EXECUTE_XPATH_QUERY_ON_LOCAL_ROOT,
                                                                      arguments);
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
     *         if the request fails
     */
    private Object requestActionWithResponse(UIAutomatorRequest requestType, Object[] arguments)
        throws CommandFailedException {
        Request<UIAutomatorRequest> automatorRequest = new Request<>(requestType);
        automatorRequest.setArguments(arguments);

        try {
            return requestSender.request(automatorRequest);
        } catch (ClassNotFoundException | IOException e) {
            String messageFormat = "Failed request %s.";
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
     *         thrown when the request fails
     */
    private void requestAction(UIAutomatorRequest requestType, Object[] arguments) throws CommandFailedException {
        Request<UIAutomatorRequest> automatorRequest = new Request<>(requestType);
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
            String errorMessage = String.format("Starting ATMOSPHERE gesture player failed for %s.",
                                                deviceSerialNumber);
            throw new OnDeviceComponentStartingException(errorMessage, e);
        }

    }

    @Override
    public void stopComponent() {
        Request<UIAutomatorRequest> automatorRequest = new Request<>(UIAutomatorRequest.STOP);

        try {
            requestSender.request(automatorRequest);
        } catch (ClassNotFoundException | IOException | CommandFailedException e) {
            // Redirect the exception to the server
            LOGGER.warn("Stoopping ATMOSPHERE gesture player failed.", e);
        }
    }

    @Override
    public void validateRemoteServer() {
        Request<UIAutomatorRequest> validationRequest = new Request<>(UIAutomatorRequest.VALIDATION);
        validateRemoteServer(validationRequest);
    }

}
