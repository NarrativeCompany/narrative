package org.narrative.common.web.struts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date: Mar 10, 2006
 * Time: 2:51:51 PM
 * <p>
 * This annotation should be used to bypass the validate function for a given request.
 * Useful for non-input requests that don't wish to have normal validation.
 *
 * @author Brian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BypassValidate {}
