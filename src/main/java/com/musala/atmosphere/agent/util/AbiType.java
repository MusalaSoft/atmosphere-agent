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

package com.musala.atmosphere.agent.util;

/**
 * Contains the different abi types as seen in <i>com.android.sdklib.devices.Abi</i>.
 * 
 * @author yordan.petrov
 * 
 */
public enum AbiType {
    ARMEABI("armeabi"),
    ARMEABI_V7A("armeabi-v7a"),
    X86("x86"),
    MIPS("mips");

    private final String value;

    AbiType(String value) {
        this.value = value;
    }

    /**
     * Returns an {@link AbiType} element corresponding to the passed {@link String} representation.
     * 
     * @param value
     *        - the {@link String} representation of the {@link AbiType} element.
     * @return an {@link AbiType} element corresponding to the passed {@link String} representation.
     */
    public static AbiType getEnum(String value) {
        for (AbiType abiType : values()) {
            if (abiType.value.equals(value)) {
                return abiType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
