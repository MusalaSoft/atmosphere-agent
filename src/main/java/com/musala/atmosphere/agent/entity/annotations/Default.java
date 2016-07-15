package com.musala.atmosphere.agent.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that will be used to denote default implementations for a certain functionality defined as an entity.
 *
 * @author filareta.yordanova
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Default {
}
