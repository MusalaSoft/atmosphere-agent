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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.musala.atmosphere.commons.DeviceInformation;

/**
 * Tests {@link EntityTypeResolver}.
 *
 * @author filareta.yordanova
 *
 */
// TODO: Add more cases when complex criteria are available, e.g more fields are added to @Restriction.
public class EntityTypeResolverTest {
    private static final String ERROR_MESSAGE = "Returned entity instance is not from expected type.";

    private EntityTypeResolver entityFactory;

    @Test
    public void testGetGpsLocationEntityForApiLevel() {
        DeviceInformation requiredInformation = new DeviceInformation();
        requiredInformation.setApiLevel(17);
        entityFactory = new EntityTypeResolver(requiredInformation);

        Class<?> entityClass = entityFactory.getEntityClass(GpsLocationEntity.class);
        assertEquals(ERROR_MESSAGE, entityClass, GpsLocationCheckBoxEntity.class);
    }

    @Test
    public void testGetGpsLocationEntityWhenNoMatchFound() {
        DeviceInformation requiredInformation = new DeviceInformation();
        requiredInformation.setApiLevel(21);
        entityFactory = new EntityTypeResolver(requiredInformation);

        Class<?> entityClass = entityFactory.getEntityClass(GpsLocationEntity.class);
        assertEquals(ERROR_MESSAGE, entityClass, GpsLocationSwitchViewEntity.class);
    }
}
