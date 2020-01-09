package org.narrative.network.core.propertyset.base;

import org.narrative.network.core.propertyset.base.services.PropertySetTypeBase;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:39:04 PM
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PropertySetType {

    @EqualsAndHashCode.Include
    private final String type;

    private final boolean isGlobal;
    private final Map<String, PropertyType> propertyTypes = new HashMap<>();

    private static final Map<String, PropertySetType> PROPERTY_SET_TYPES = new HashMap<>();

    private static final Map<Class<? extends PropertySetTypeBase>, PropertySetType> BASE_INTERFACE_TO_PROPERTY_SET_TYPE = new HashMap<>();

    public PropertySetType(String type, boolean isGlobal, Class<? extends PropertySetTypeBase> baseClass) {
        this.type = type;
        this.isGlobal = isGlobal;
        PROPERTY_SET_TYPES.put(type, this);
        BASE_INTERFACE_TO_PROPERTY_SET_TYPE.put(baseClass, this);
    }

    public String getType() {
        return type;
    }

    public Map<String, PropertyType> getPropertyTypes() {
        return propertyTypes;
    }

    /**
     * get the default property set for this type.
     *
     * @return the default PropertySet for this type.
     */
    @NotNull
    public PropertySet getDefaultPropertySet() {
        PropertySet defaultPropertySet = PropertySet.dao().getDefaultPropertySetByType(this);
        assert exists(defaultPropertySet) : "No default PropertySet found for type " + getType() + ". Must be a coding/configuration error with the default PropertySets.";
        return defaultPropertySet;
    }

    public PropertyType addPropertyType(String name, boolean isRequired) {
        PropertyType propertyType = new PropertyType(name, isRequired, this);
        propertyTypes.put(propertyType.getName(), propertyType);
        return propertyType;
    }

    public PropertyType getPropertyTypeByName(String name) {
        assert propertyTypes.containsKey(name) : "Should never try to get a PropertType that doesn't exist! Has init completed? pt/" + name + " pst/" + type;
        return propertyTypes.get(name);
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public String toString() {
        return type;
    }

    public static Collection<PropertySetType> getAll() {
        return PROPERTY_SET_TYPES.values();
    }

    public static PropertySetType getPropertySetTypeByType(String type) {
        //assert PROPERTY_SET_TYPES.containsKey(type) : "Should never attempt to get PropertySetType for a type that does not exist! Has init completed? type/" + type;
        return PROPERTY_SET_TYPES.get(type);
    }

    /**
     * get a PropertySetType object based on the interface you are looking for.
     * requires an interface that both extends PropertySetTypeBase and has
     * a PropertySetTypeDef annotation.
     *
     * @param cls the class for which you want to get the PropertySetType
     * @return the PropertySetType for the given interface
     */
    public static <T extends PropertySetTypeBase> PropertySetType getPropertySetTypeByInterface(Class<T> cls) {
        return BASE_INTERFACE_TO_PROPERTY_SET_TYPE.get(cls);
    }
}
