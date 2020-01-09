package org.narrative.network.core.propertyset.base.services;

/**
 * Marker interface to join all property set-like classes together.
 * Date: Dec 6, 2005
 * Time: 3:37:37 PM
 *
 * @author Brian
 */
public interface PropertySetTypeBase {
    /**
     * get the PropertyMap that is wrapped by this PropertySetTypeBase object.
     * can't start with "get" or else it will be (incorrectly) interpreted as
     * an actual property (for use in this database) of the PropertySet.
     *
     * @return the PropertyMap wrapped by this PropertySetTypeBase object.
     */
    PropertyMap wrappedPropertyMap();
}
