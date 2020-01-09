package org.narrative.common.web.struts;

import com.opensymphony.xwork2.Action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date: Mar 10, 2006
 * Time: 2:51:51 PM
 *
 * @author Brian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ErrorResponseCode {
    /**
     * the error response code to use in the event that this method action has validation errors
     */
    String value() default Action.INPUT;
}
