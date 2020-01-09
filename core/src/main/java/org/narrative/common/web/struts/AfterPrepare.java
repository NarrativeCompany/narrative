package org.narrative.common.web.struts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be used to indicate that a property (getter & setter) on an action should
 * be invoked only after the prepare method has been called.
 * <p>
 * Date: Feb 8, 2006
 * Time: 11:51:20 AM
 *
 * @author Brian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterPrepare {}
