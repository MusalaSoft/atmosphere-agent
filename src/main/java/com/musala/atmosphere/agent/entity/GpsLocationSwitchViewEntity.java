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

package com.musala.atmosphere.agent.entity;

import java.util.List;

import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.UIAutomatorCommunicator;
import com.musala.atmosphere.agent.entity.annotations.Default;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.exceptions.UiElementFetchingException;
import com.musala.atmosphere.commons.ui.UiElementPropertiesContainer;
import com.musala.atmosphere.commons.ui.selector.CssAttribute;
import com.musala.atmosphere.commons.ui.selector.UiElementSelector;
import com.musala.atmosphere.commons.ui.tree.AccessibilityElement;

/**
 * {@link GpsLocationEntity} responsible for all devices that are using switch widgets for setting the GPS location
 * state.
 *
 * @author yavor.stankov
 *
 */
@Default
public class GpsLocationSwitchViewEntity extends GpsLocationEntity {
    private static final String ANDROID_WIDGET_SWITCH_RESOURCE_ID = "com.android.settings:id/switch_widget";

    GpsLocationSwitchViewEntity(ServiceCommunicator serviceCommunicator,
                                UIAutomatorCommunicator automatorCommunicator,
                                HardwareButtonEntity hardwareButtonEntity,
                                GestureEntity gestureEntity) {
        super(serviceCommunicator, automatorCommunicator, hardwareButtonEntity, gestureEntity);
    }

    @Override
    protected UiElementPropertiesContainer getChangeStateWidget() throws UiElementFetchingException, CommandFailedException {

        UiElementSelector switchWidgetSelector = new UiElementSelector();
        switchWidgetSelector.addSelectionAttribute(CssAttribute.RESOURCE_ID, ANDROID_WIDGET_SWITCH_RESOURCE_ID);

        automatorCommunicator.waitForExists(switchWidgetSelector, CHANGE_STATE_WIDGET_TIMEOUT);

        List<AccessibilityElement> widgetList = automatorCommunicator.getUiElements(switchWidgetSelector, true);

        if (!widgetList.isEmpty()) {
            return widgetList.get(0);
        }

        return null;
    }
}
