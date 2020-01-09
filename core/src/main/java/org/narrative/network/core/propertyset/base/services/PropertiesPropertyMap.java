package org.narrative.network.core.propertyset.base.services;

import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: May 25, 2007
 * Time: 10:37:30 AM
 */
public class PropertiesPropertyMap implements PropertyMap {
    private static final long serialVersionUID = 8638723671642732367L;
    private final Map<String, String> map;

    public PropertiesPropertyMap(Map<String, String> map) {
        this.map = map;
    }

    public PropertiesPropertyMap(Properties properties) {
        Map map = properties;
        this.map = (Map<String, String>) map;
    }

    public void setPropertyValue(String name, String value) {
        // bl: need to support null values. Properties extends Hashtable, which doesn't support null keys or values.
        // thus, check if the value supplied is null, and if so, just remove the property instead of trying to
        // set the value to null, which results in an NPE.
        if (value == null) {
            map.remove(name);
        } else {
            map.put(name, value);
        }
    }

    public String getPropertyValueByName(String name) {
        return map.get(name);
    }

    public Map<String, String> getMap() {
        return map;
    }
}
