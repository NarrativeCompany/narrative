package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.SerializationUtil;
import org.hibernate.HibernateException;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/17/14
 * Time: 8:33 AM
 */
public class HibernateMapType extends SerializedHibernateType<Map> {
    private static final long serialVersionUID = 2196298441121797074L;

    // todo: it would be nice to have these types derived from the object property instead of having to explicitly
    //       define them, but until we can come up with a zero config option lets go with this explicit config option.
    private Class mapKeyType;
    private Class mapValueType;

    public static final String MAP_KEY_TYPE_CLASS = "mapKeyType";
    public static final String MAP_VALUE_TYPE_CLASS = "mapValueType";

    public static final String TYPE = "org.narrative.common.persistence.hibernate.HibernateMapType";

    @Override
    public void setParameterValues(Properties parameters) {
        mapKeyType = HibernateEnumSetType.getClassFromParameters(parameters, MAP_KEY_TYPE_CLASS);
        mapValueType = HibernateEnumSetType.getClassFromParameters(parameters, MAP_VALUE_TYPE_CLASS);
    }

    public int sqlType() {
        return Types.CLOB;
    }

    public Class returnedClass() {
        return Map.class;
    }

    @Override
    protected Map deserialize(String value) {
        return SerializationUtil.deserializeMap(value, mapKeyType, mapValueType);
    }

    @Override
    protected String serialize(Map map) {
        return SerializationUtil.serializeMap(map);
    }

    public Object deepCopy(Object value) throws HibernateException {
        Map map = new LinkedHashMap();
        if (value == null) {
            return map;
        }
        map.putAll((Map) value);
        return map;
    }
}
