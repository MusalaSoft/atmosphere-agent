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
