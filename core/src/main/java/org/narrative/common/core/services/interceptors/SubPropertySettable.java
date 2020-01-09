package org.narrative.common.core.services.interceptors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate that a given parameter allows
 * its sub-properties to be set.  Without this annotation, sub-properties
 * will not be settable for action properties.
 * <p>
 * Date: Feb 9, 2006
 * Time: 5:41:15 PM
 *
 * @author Brian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubPropertySettable {
    /**
     * the depth of elements for which sub-properties can be set.
     * defaults to 1 so that only properties immediately on this object
     * will be settable (and not those properties' sub-properties).
     */
    int depth() default 1;

    /**
     * any subclasses of the return type that should be included.
     * useful when a getter returns a generic base class and you want
     * to allow properties from a subclass to be included.
     *
     * @return an array of subclasses to check for further sub-properties
     */
    Class[] subclasses() default {};
}
