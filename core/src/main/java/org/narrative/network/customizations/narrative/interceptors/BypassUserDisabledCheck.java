package org.narrative.network.customizations.narrative.interceptors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date: 9/29/18
 * Time: 3:00 PM
 *
 * @author brian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BypassUserDisabledCheck {

}
