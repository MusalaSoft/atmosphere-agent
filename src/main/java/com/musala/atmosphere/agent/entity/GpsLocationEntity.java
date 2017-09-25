package com.musala.atmosphere.agent.entity;

import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.exceptions.UiElementFetchingException;
import com.musala.atmosphere.commons.geometry.Bounds;
import com.musala.atmosphere.commons.geometry.Point;
import com.musala.atmosphere.commons.ui.UiElementPropertiesContainer;
import com.musala.atmosphere.commons.ui.selector.CssAttribute;
import com.musala.atmosphere.commons.ui.selector.UiElementSelector;
import com.musala.atmosphere.commons.ui.tree.AccessibilityElement;

/**
 * Base entity responsible for handling GPS location state changing.
 *
 * @author yavor.stankov
 *
 */
public abstract class GpsLocationEntity {
    private static final Logger LOGGER = Logger.getLogger(GpsLocationEntity.class.getCanonicalName());

    private static final String AGREE_BUTTON_RESOURCE_ID = "android:id/button1";

    private static final int AGREE_BUTTON_TIMEOUT = 3000;

    protected static final int CHANGE_STATE_WIDGET_TIMEOUT = 5000;

    private static final int UI_ELEMENT_OPERATION_WAIT_TIME = 500;

    protected ServiceCommunicator serviceCommunicator;

    protected UIAutomatorCommunicator automatorCommunicator;

    protected HardwareButtonEntity hardwareButtonEntity;

    protected GestureEntity gestureEntity;

    GpsLocationEntity(ServiceCommunicator serviceCommunicator,
                      UIAutomatorCommunicator automatorCommunicator,
                      HardwareButtonEntity hardwareButtonEntity,
                      GestureEntity gestureEntity) {
        this.serviceCommunicator = serviceCommunicator;
        this.automatorCommunicator = automatorCommunicator;
        this.hardwareButtonEntity = hardwareButtonEntity;
        this.gestureEntity = gestureEntity;
    }

    /**
     * Gets the right widget that is responsible for setting the GPS location state.
     *
     * @return the widget that should be used for setting the GPS location state.
     * @throws UiElementFetchingException
     *         if the required widget is not present on the screen
     * @throws CommandFailedException
     *         if getting the widget failed
     */
    protected abstract UiElementPropertiesContainer getChangeStateWidget()
        throws UiElementFetchingException, CommandFailedException;

    /**
     * Enables the GPS location on this device.
     *
     * @return <code>true</code> if the GPS location enabling is successful, <code>false</code> if it fails
     * @throws CommandFailedException
     *         when failed to enable the GPS location
     */
    public boolean enableGpsLocation() throws CommandFailedException {
        return setGpsLocationState(true);
    }

    /**
     * Disables the GPS location on this device.
     *
     * @return <code>true</code> if the GPS location disabling is successful, <code>false</code> if it fails
     * @throws CommandFailedException
     *         when failed to disable the GPS location
     */
    public boolean disableGpsLocation() throws CommandFailedException {
        return setGpsLocationState(false);
    }

    /**
     * Check if the GPS location is enabled on this device.
     *
     * @return <code>true</code> if the GPS location is enabled, <code>false</code> if it's disabled
     * @throws CommandFailedException
     *         when failed to get the GPS location status
     */
    public boolean isGpsLocationEnabled() throws CommandFailedException {
        return serviceCommunicator.isGpsLocationEnabled();
    }

    private boolean setGpsLocationState(boolean state) throws CommandFailedException {
        if (isGpsLocationEnabled() == state) {
            return true;
        }

        openLocationSettings();

        try {
            UiElementPropertiesContainer changeStateWidget = getChangeStateWidget();
            if (tap(changeStateWidget)) {
                pressAgreeButton();
            }
        } catch (UiElementFetchingException e) {
            LOGGER.error("Failed to get the wanted widget, or there are more than one widgets on the screen that are matching the given selector.",
                         e);
            return false;
        }

        // TODO: If needed, move the HardwareButton enumeration from com.musala.atmosphere.client.device to atmosphere-commons,
        // so the enumeration could be used here instead of an integer.
        hardwareButtonEntity.pressButton(4); // Back button

        return true;
    }

    public void openLocationSettings() throws CommandFailedException {
        serviceCommunicator.openLocationSettings();
    }

    private void pressAgreeButton() throws UiElementFetchingException, CommandFailedException {
        UiElementSelector agreeButtonSelector = new UiElementSelector();
        agreeButtonSelector.addSelectionAttribute(CssAttribute.RESOURCE_ID, AGREE_BUTTON_RESOURCE_ID);

        if (automatorCommunicator.waitForExists(agreeButtonSelector, AGREE_BUTTON_TIMEOUT)) {
            List<AccessibilityElement> elementsList = automatorCommunicator.getUiElements(agreeButtonSelector, true);

            AccessibilityElement agreeButton = elementsList.get(0);
            tap(agreeButton);
        }
    }

    private boolean tap(UiElementPropertiesContainer element) {
        Bounds elementBounds = element.getBounds();
        Point centerPoint = elementBounds.getCenter();
        Point point = elementBounds.getRelativePoint(centerPoint);
        Point tapPoint = elementBounds.getUpperLeftCorner();
        tapPoint.addVector(point);

        boolean tapSuccessful = false;
        if (elementBounds.contains(tapPoint)) {
            tapSuccessful = gestureEntity.tapScreenLocation(tapPoint);
            finalizeUiElementOperation();
        }  else {
            String message = String.format("Point %s not in element bounds.", point.toString());
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }

        return tapSuccessful;
    }

    private void finalizeUiElementOperation() {
        // Should be invoked exactly once in the end of all element-operating
        // methods, whether its directly or indirectly invoked.
        try {
            Thread.sleep(UI_ELEMENT_OPERATION_WAIT_TIME);
        } catch (InterruptedException e) {
            LOGGER.info(e);
        }
    }
}
