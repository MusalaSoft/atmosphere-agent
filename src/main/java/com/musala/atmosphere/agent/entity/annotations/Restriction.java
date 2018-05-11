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

package com.musala.atmosphere.agent.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.musala.atmosphere.commons.DeviceInformation;

/**
 * Annotation that will be used for denoting restrictions on a group of devices that supports a certain implementation
 * for a functionality defined as an entity. All criteria used for group separation might be added to this interface.
 *
 * @author filareta.yordanova
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Restriction {
    /**
     *
     * Manufacturer of the device that must be built with the annotated entity.
     *
     * @return String
     */
    String manufacturer() default DeviceInformation.FALLBACK_MANUFACTURER_NAME;

    /**
     * Api level of the device that must be built with the annotated entity.
     *
     * @return int[]
     */
    int[] apiLevel() default {};
}
