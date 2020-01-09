package org.narrative.network.core.propertyset.base.services;

import java.io.Serializable;

/**
 * This interface exists to define the core functionality provided by
 * the interface supplied to PropertySetTypeUtil.
 * Date: Dec 6, 2005
 * Time: 4:07:24 PM
 *
 * @author Brian
 */
public interface PropertyMap extends Serializable {
    public void setPropertyValue(String name, String value);

    public String getPropertyValueByName(String name);
}
