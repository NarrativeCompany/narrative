package org.narrative.common.web.struts;

import com.opensymphony.xwork2.ActionSupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate that if there is an exception during method
 * execution, the input method should be used to render the error instead of the
 * usual error page.
 * <p>
 * NOTE: This annotation should only be used in conjunction with non-AJAX POST requests.
 * <p>
 * Date: Jul 24, 2007
 * Time: 1:55:15 PM
 *
 * @author Brian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseInputForExceptions {
    /**
     * the input result type to use to render the page.  defaults to "input".
     */
    String value() default ActionSupport.INPUT;
}