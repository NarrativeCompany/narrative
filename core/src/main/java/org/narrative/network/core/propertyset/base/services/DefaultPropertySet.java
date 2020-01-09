package org.narrative.network.core.propertyset.base.services;

import java.util.HashMap;
import java.util.Map;

/**
 * This class exists purely for bootstrapping the default property sets.
 * Used internally by the PropertySetTypeUtil.
 * Date: Dec 6, 2005
 * Time: 4:06:30 PM
 *
 * @author Brian
 */
class DefaultPropertySet implements PropertyMap {

    private final Map<String, String> properties = new HashMap<String, String>();

    public void setPropertyValue(String name, String value) {
        properties.put(name, value);
    }

    public String getPropertyValueByName(String name) {
        return properties.get(name);
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}                                               
