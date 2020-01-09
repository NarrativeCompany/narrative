package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.SerializationUtil;
import org.hibernate.HibernateException;

import java.sql.Types;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/26/16
 * Time: 11:57 AM
 */
public class HibernateSetType extends SerializedHibernateType<Set> {
    private static final long serialVersionUID = 8773719305663406688L;

    private Class setObjectType;

    public static final String SET_OBJECT_TYPE_CLASS = "setObjectType";

    public static final String TYPE = "org.narrative.common.persistence.hibernate.HibernateSetType";

    @Override
    public void setParameterValues(Properties parameters) {
        setObjectType = HibernateEnumSetType.getClassFromParameters(parameters, SET_OBJECT_TYPE_CLASS);
    }

    @Override
    public int sqlType() {
        return Types.CLOB;
    }

    @Override
    protected Set deserialize(String value) {
        return SerializationUtil.deserializeCollection(value, Set.class, setObjectType);
    }

    @Override
    protected String serialize(Set object) {
        return SerializationUtil.serializeCollection(object);
    }

    @Override
    public Class returnedClass() {
        return setObjectType;
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        Set set = new HashSet();
        if (value == null) {
            return set;
        }
        set.addAll((Set) value);
        return set;
    }
}
