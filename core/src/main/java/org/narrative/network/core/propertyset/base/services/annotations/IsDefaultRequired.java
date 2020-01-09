package org.narrative.network.core.propertyset.base.services.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate whether or not a default
 * value is required for a given property set field.
 * Date: Dec 6, 2005
 * Time: 2:02:41 PM
 *
 * @author Brian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IsDefaultRequired {
    boolean value() default true;
}
