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

import java.lang.annotation.Annotation;
import java.util.Set;

import org.reflections.Reflections;

import com.musala.atmosphere.agent.entity.annotations.Restriction;
import com.musala.atmosphere.commons.DeviceInformation;

/**
 * Class responsible for resolving the correct implementation of the entities, defined for all device specific
 * operations, depending on the provided {@link DeviceInformation}.
 *
 * @author filareta.yordanova
 *
 */
public class EntityTypeResolver {
    private static final String ENTITIES_PACKAGE = "com.musala.atmosphere.agent.entity";

    private DeviceInformation deviceInformation;

    private Reflections reflections;

    public EntityTypeResolver(DeviceInformation information) {
        this.deviceInformation = information;
        reflections = new Reflections(ENTITIES_PACKAGE);
    }

    /**
     * Finds entity implementation for a device specific operation depending on the {@link DeviceInformation device
     * information} and the hierarchy type given.
     *
     * @param baseEntityClass
     *        - base class of the entity hierarchy for a device specific operation
     * @return {@link Class} of the entity that matches the required {@link DeviceInformation device information} and is
     *         from type baseEntityClass
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Class<?> getEntityClass(Class baseEntityClass) {
        Set<Class<?>> subClasses = reflections.getSubTypesOf(baseEntityClass);
        Class<?> defaultImplementation = null;

        for (Class<?> subClass : subClasses) {
            Annotation annotation = subClass.getAnnotation(Restriction.class);
            if (annotation == null) {
                defaultImplementation = subClass;
            } else if (isApplicable((Restriction) annotation)) {
                return subClass;
            }
        }

        return defaultImplementation;

    }

    /**
     * Checks if a certain implementation annotated with {@link @Restriction} is applicable for a device with the
     * provided {@link DeviceInformation information}.
     *
     * @param restriction
     *        - restrictions provided for a certain entity implementation
     * @return <code>true</code> if the given restrictions are compatible with the {@link DeviceInformation information}
     *         for the current device, <code>false</code> otherwise
     */
    // TODO: Check for default values in the annotation methods, if the parameter has default value and is not present
    // in the annotation it is not considered when checking for applicability.
    private boolean isApplicable(Restriction restriction) {
        String manufacturer = restriction.manufacturer();

        if (!manufacturer.equals(DeviceInformation.FALLBACK_MANUFACTURER_NAME)
                && !manufacturer.equalsIgnoreCase(deviceInformation.getManufacturer())) {
            return false;
        }

        boolean isApplicable = true;
        int[] apiLevels = restriction.apiLevel();

        if (apiLevels.length > 0) {
            int deviceApiLevel = deviceInformation.getApiLevel();
            isApplicable = false;

            for (int applicableApiLevel : apiLevels) {
                if (applicableApiLevel == deviceApiLevel) {
                    isApplicable = true;
                    break;
                }
            }
        }

        return isApplicable;
    }
}
