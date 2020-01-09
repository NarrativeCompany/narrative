package org.narrative.common.persistence.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/26/16
 * Time: 11:43 AM
 */
public abstract class SerializedHibernateType<T> implements UserType, ParameterizedType, Serializable {
    private static final long serialVersionUID = 7237953864520546900L;

    public abstract int sqlType();

    protected abstract T deserialize(String value);

    protected abstract String serialize(T object);

    public int[] sqlTypes() {
        return new int[]{sqlType()};
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return EqualsHelper.equals(x, y);
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        String name = names[0];
        Object val = rs.getObject(name);

        //Object is null
        if (val == null || rs.wasNull()) {
            return new LinkedHashMap();
        }

        //Stored as a string in the database
        if (!(val instanceof String)) {
            throw new HibernateException("Database column '" + name + "' is not compatable with SerializedHibernateType");
        }

        return deserialize((String) val);
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlType());
            return;
        }

        T object = (T) value;
        st.setString(index, serialize(object));
    }

    public boolean isMutable() {
        return true;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        if (value instanceof Serializable) {
            return (Serializable) value;
        } else {
            return null;
        }
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
