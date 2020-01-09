package org.narrative.network.core.propertyset.base.services;

/**
 * Date: Dec 6, 2005
 * Time: 4:18:11 PM
 *
 * @author Brian
 */
public interface PropertySetTypeDefaultValueProvider<T extends PropertySetTypeBase> {

    /**
     * get a Map of names to PropertySetTypeBase objects.
     * some types will only have one default set and some types
     * will have multiple.
     *
     * @return a Map of names to the default property sets for the given type
     */
    public T getDefaultPropertySet();
}
