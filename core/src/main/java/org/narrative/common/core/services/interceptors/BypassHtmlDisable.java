package org.narrative.common.core.services.interceptors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be used to indicate that a property setter on an action should
 * bypass the default behavior of disabling HTML.  Since HTML disabling behavior is only
 * applicable to Strings, this annotation is only meaningful when applied to a setter
 * for a String.
 * <p>
 * Date: Feb 8, 2006
 * Time: 11:51:20 AM
 *
 * @author Brian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BypassHtmlDisable {}
