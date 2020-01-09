package org.narrative.common.web.struts;

import org.narrative.common.web.HttpMethodType;
import org.narrative.common.web.HttpRequestType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Eclipse.
 * User: benjamin
 * Date: Jul 10, 2009
 * Time: 1:53:42 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodDetails {
    HttpMethodType httpMethodType() default HttpMethodType.UNSPECIFIED;

    HttpRequestType requestType() default HttpRequestType.STANDARD;

    ReadOnly readOnly() default ReadOnly.UNSPECIFIED;

    boolean isSSLOnly() default false;

    boolean preventDoublePosting() default false;
}
