package com.musala.atmosphere.agent.entity;

import java.util.List;

import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.agent.entity.annotations.Restriction;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.exceptions.UiElementFetchingException;
import com.musala.atmosphere.commons.ui.UiElementPropertiesContainer;
import com.musala.atmosphere.commons.ui.selector.CssAttribute;
import com.musala.atmosphere.commons.ui.selector.UiElementSelector;
import com.musala.atmosphere.commons.ui.tree.AccessibilityElement;

/**
 * {@link GpsLocationEntity} responsible for setting the GPS location state on all Samsung devices.
 *
 * @author yavor.stankov
 *
 */
@Restriction(apiLevel = {17, 18})
public class GpsLocationCheckBoxEntity extends GpsLocationEntity {
    private static final String ANDROID_WIDGET_CHECK_BOX_CLASS_NAME = "android.widget.CheckBox";

    GpsLocationCheckBoxEntity(ServiceCommunicator serviceCommunicator,
                      UIAutomatorCommunicator automatorCommunicator,
                      HardwareButtonEntity hardwareButtonEntity,
                      GestureEntity gestureEntity) {
        super(serviceCommunicator, automatorCommunicator, hardwareButtonEntity, gestureEntity);
    }

    @Override
    protected UiElementPropertiesContainer getChangeStateWidget() throws UiElementFetchingException, CommandFailedException {
        UiElementSelector checkBoxWidgetSelector = new UiElementSelector();
        checkBoxWidgetSelector.addSelectionAttribute(CssAttribute.CLASS_NAME, ANDROID_WIDGET_CHECK_BOX_CLASS_NAME);

        automatorCommunicator.waitForExists(checkBoxWidgetSelector, CHANGE_STATE_WIDGET_TIMEOUT);

        List<AccessibilityElement> widgetList = automatorCommunicator.getUiElements(checkBoxWidgetSelector, true);

        if (!widgetList.isEmpty()) {
            // There are more than one check box on the screen, but only the first one is for setting the GPS location
            // state.
            return widgetList.get(0);
        }

        return null;
    }
}
