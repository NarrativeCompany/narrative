package org.narrative.network.core.propertyset.base.services;

import org.narrative.common.util.IPBeanUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.Task;
import org.narrative.network.core.propertyset.base.PropertySetType;
import org.narrative.network.core.propertyset.base.services.annotations.IsDefaultRequired;
import org.narrative.network.core.propertyset.base.services.annotations.PropertySetTypeDef;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Date: Dec 6, 2005
 * Time: 3:47:35 PM
 *
 * @author Brian
 */
public class InitializePropertySetType extends Task<Object> {
    private final Class<? extends PropertySetTypeBase> propertySetTypeDefClass;

    public InitializePropertySetType(Class<? extends PropertySetTypeBase> propertySetTypeDefClass) {
        this.propertySetTypeDefClass = propertySetTypeDefClass;
    }

    /**
     * not force writable.  this task doesn't even do anything with Hibernate.
     *
     * @return false.  this init task is not force writable.
     */
    public boolean isForceWritable() {
        return false;
    }

    protected Object doMonitoredTask() {
        PropertySetTypeDef defAnnotation = propertySetTypeDefClass.getAnnotation(PropertySetTypeDef.class);
        assert defAnnotation != null : "Must specify a PropertySetTypeDef annotation on interfaces extending PropertySetTypeBase";
        PropertySetType propertySetType = new PropertySetType(defAnnotation.name(), defAnnotation.global(), propertySetTypeDefClass);
        setupPropertySetType(propertySetType, propertySetTypeDefClass, "");

        return null;
    }

    private static void setupPropertySetType(PropertySetType propertySetType, Class<? extends PropertySetTypeBase> propertySetTypeDefClass, String propertyPrefix) {
        for (Method method : propertySetTypeDefClass.getMethods()) {
            // only looking for getters
            if (!IPBeanUtil.isGetter(method)) {
                continue;
            }
            String propertyName = IPBeanUtil.getPropertyNameFromGetter(method.getName());
            if (IPStringUtil.isEmpty(propertyName)) {
                continue;
            }
            propertyName = propertyPrefix + propertyName;

            // bl: skip any concrete impl
            if (!Modifier.isAbstract(method.getModifiers())) {
                continue;
            }

            if (PropertySetTypeBase.class.isAssignableFrom(method.getReturnType())) {
                Class<? extends PropertySetTypeBase> nestedPropertySetTypeClass = (Class<? extends PropertySetTypeBase>) method.getReturnType();
                setupPropertySetType(propertySetType, nestedPropertySetTypeClass, propertyName + ".");
            } else {
                IsDefaultRequired isRequiredAnnotation = method.getAnnotation(IsDefaultRequired.class);
                // all fields default to being required.
                boolean isRequired = isRequiredAnnotation == null || isRequiredAnnotation.value();
                // bl: actually, let's allow for this scenario.  PropertySetTypeUtil properly handles
                // this already.  it will return a null value and that null value will automatically be
                // converted into the default value for the primitive.
                //assert !method.getReturnType().isPrimitive() || isRequired : "Can't have a non-required property with a primitive type: " + propertyName + " of " + propertySetTypeDefClass;
                propertySetType.addPropertyType(propertyName, isRequired);
            }
        }
    }
}
