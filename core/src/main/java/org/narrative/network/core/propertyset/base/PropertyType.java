package org.narrative.network.core.propertyset.base;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:39:11 PM
 * To change this template use File | Settings | File Templates.
 */

public class PropertyType {
    private final PropertySetType propertySetType;
    private final String name;
    private final boolean required;

    PropertyType(String name, boolean required, PropertySetType propertySetType) {
        this.name = name;
        this.required = required;
        this.propertySetType = propertySetType;
    }

    @JsonIgnore
    public PropertySetType getPropertySetType() {
        return propertySetType;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }
}
