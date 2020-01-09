package org.narrative.network.core.propertyset.base.services.annotations;

import org.narrative.network.core.propertyset.base.services.PropertySetTypeBase;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeDefaultValueProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date: Dec 6, 2005
 * Time: 3:52:08 PM
 *
 * @author Brian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PropertySetTypeDef {
    String name();

    Class<? extends PropertySetTypeDefaultValueProvider<? extends PropertySetTypeBase>> defaultProvider();

    /**
     * true if this is a "global" property set, which essentially means that
     * overrides are not supported.
     */
    boolean global() default false;
}
