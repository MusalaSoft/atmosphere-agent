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
