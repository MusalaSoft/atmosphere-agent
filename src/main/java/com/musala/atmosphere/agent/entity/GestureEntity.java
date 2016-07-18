package com.musala.atmosphere.agent.entity;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.agent.util.GestureCreator;
import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.beans.SwipeDirection;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.geometry.Point;
import com.musala.atmosphere.commons.gesture.Gesture;
import com.musala.atmosphere.commons.util.Pair;

/**
 * Entity that contains the base gesture functionalities that can be executed on {@link UiElement}.
 *
 * @author yavor.stankov
 *
 */
public class GestureEntity {
    private static final Logger LOGGER = Logger.getLogger(GestureEntity.class.getCanonicalName());

    private DeviceInformation deviceInformation;

    private ShellCommandExecutor shellCommandExecutor;

    private ServiceCommunicator serviceCommunicator;

    private UIAutomatorCommunicator automatorCommunicator;

    GestureEntity(ShellCommandExecutor shellCommandExecutor,
                  ServiceCommunicator serviceCommunicator,
                  DeviceInformation deviceInformation,
                  UIAutomatorCommunicator automatorCommunicator) {
        this.shellCommandExecutor = shellCommandExecutor;
        this.serviceCommunicator = serviceCommunicator;
        this.deviceInformation = deviceInformation;
        this.automatorCommunicator = automatorCommunicator;
    }

    /**
     * Executes a simple tap on the screen of this device at a specified location point.
     *
     * @param tapPoint
     *        - {@link Point Point} on the screen to tap on
     *
     * @return <code>true</code> if tapping screen is successful, <code>false</code> if it fails
     */
    public boolean tapScreenLocation(Point tapPoint) {
        boolean isTapSuccessful = true;

        int tapPointX = tapPoint.getX();
        int tapPointY = tapPoint.getY();
        String query = "input tap " + tapPointX + " " + tapPointY;

        showTapLocation(tapPoint);

        try {
            shellCommandExecutor.execute(query);
        } catch (CommandFailedException e) {
            isTapSuccessful = false;
        }

        return isTapSuccessful;
    }

    /**
     * Executes long press on point on the screen with given coordinates and timeout for the gesture in ms.
     *
     * @param pressPoint
     *        - {@link Point point} on the screen where the long press should be executed
     * @param timeout
     *        - the time in ms, showing how long should the holding part of the gesture continue
     * @return - true, if operation is successful, and false otherwise
     */
    public boolean longPress(Point pressPoint, int timeout) {
        Gesture longPress = GestureCreator.createLongPress(pressPoint.getX(), pressPoint.getY(), timeout);

        return playGesture(longPress);
    }

    /**
     * Simulates a double tap on the specified point.
     *
     * @param point
     *        - the point to be tapped
     * @return <code>true</code> if the double tap is successful, <code>false</code> if it fails
     */
    public boolean doubleTap(Point point) {
        Gesture doubleTap = GestureCreator.createDoubleTap(point.getX(), point.getY());

        return playGesture(doubleTap);
    }

    /**
     * Simulates a pinch in having the initial coordinates of the fingers performing it.
     *
     * @param firstFingerInitial
     *        - the initial position of the first finger
     * @param secondFingerInitial
     *        - the initial position of the second finger
     * @return <code>true</code> if the pinch in is successful, <code>false</code> if it fails
     */
    public boolean pinchIn(Point firstFingerInitial, Point secondFingerInitial) {
        validatePointOnScreen(firstFingerInitial);
        validatePointOnScreen(secondFingerInitial);

        Gesture pinchIn = GestureCreator.createPinchIn(firstFingerInitial, secondFingerInitial);

        return playGesture(pinchIn);
    }

    /**
     * Simulates a pinch out having the positions of the fingers performing it in the end of the gesture.
     *
     * @param firstFingerEnd
     *        - the position of the first finger in the end of the gesture
     * @param secondFingerEnd
     *        - the position of the second finger in the end of the gesture
     * @return <code>true</code> if the pinch out is successful, <code>false</code> if it fails
     */
    public boolean pinchOut(Point firstFingerEnd, Point secondFingerEnd) {
        validatePointOnScreen(firstFingerEnd);
        validatePointOnScreen(secondFingerEnd);

        Gesture pinchOut = GestureCreator.createPinchOut(firstFingerEnd, secondFingerEnd);

        return playGesture(pinchOut);
    }

    /**
     * Simulates a swipe from a point to another unknown point.
     *
     * @param point
     *        - the starting point
     * @param swipeDirection
     *        - a direction for the swipe action
     * @return <code>true</code> if the swipe is successful, <code>false</code> if it fails
     */
    public boolean swipe(Point point, SwipeDirection swipeDirection) {
        validatePointOnScreen(point);

        Pair<Integer, Integer> resolution = deviceInformation.getResolution();
        Gesture swipe = GestureCreator.createSwipe(point, swipeDirection, resolution);

        return playGesture(swipe);
    }

    /**
     * Drags and drops from point (Point startPoint) to point (Point endPoint).
     *
     * @param startPoint
     *        - start point of the drag and drop gesture
     * @param endPoint
     *        - end point of the drag and drop gesture
     * @return <code>true</code>, if operation is successful, <code>false</code>otherwise
     */
    public boolean drag(Point startPoint, Point endPoint) {
        validatePointOnScreen(endPoint);
        Gesture drag = GestureCreator.createDrag(startPoint, endPoint);

        return playGesture(drag);
    }

    private boolean playGesture(Gesture gesture) {
        boolean isPlayGestureSuccessful = true;

        try {
            automatorCommunicator.playGesture(gesture);
        } catch (CommandFailedException e) {
            isPlayGestureSuccessful = false;
        }

        return isPlayGestureSuccessful;
    }

    /**
     * Shows the tap location on the current device screen.
     *
     * @param point
     *        - the point where the tap will be placed
     */
    private void showTapLocation(Point point) {
        try {
            serviceCommunicator.showTapLocation(new Object[] {point});
        } catch (CommandFailedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Checks whether the given point is inside the bounds of the screen, and throws an {@link IllegalArgumentException}
     * otherwise.
     *
     * @param point
     *        - the point to be checked
     */
    private void validatePointOnScreen(Point point) {
        Pair<Integer, Integer> resolution = deviceInformation.getResolution();

        boolean hasPositiveCoordinates = point.getX() >= 0 && point.getY() >= 0;
        boolean isOnScreen = point.getX() <= resolution.getKey() && point.getY() <= resolution.getValue();

        if (!hasPositiveCoordinates || !isOnScreen) {
            String exeptionMessageFormat = "The passed point with coordinates (%d, %d) is outside the bounds of the screen. Screen dimentions (%d, %d)";
            String message = String.format(exeptionMessageFormat,
                                           point.getX(),
                                           point.getY(),
                                           resolution.getKey(),
                                           resolution.getValue());
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
    }
}
