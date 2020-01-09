package org.narrative.network.core.propertyset.base;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 11/24/15
 * Time: 5:53 PM
 */
public class PropertySetTypeDataType implements UserType, Serializable {
    private static final long serialVersionUID = 6143330948835617987L;
    public static final String TYPE = "org.narrative.network.core.propertyset.base.PropertySetTypeDataType";


    /**
     * @deprecated for hibernate use only
     */
    public PropertySetTypeDataType() {}

    @Override
    public int[] sqlTypes() {
        return new int[]{sqlType()};
    }

    private int sqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class returnedClass() {
        return PropertySetType.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return EqualsHelper.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        String name = names[0];
        String val = rs.getString(name);

        //Object is null
        if (rs.wasNull()) {
            return null;
        }

        return PropertySetType.getPropertySetTypeByType(val);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlType());
            return;
        }

        PropertySetType type;
        if (value instanceof String) {
            type = PropertySetType.getPropertySetTypeByType((String) value);
        } else {
            type = (PropertySetType) value;
        }

        st.setString(index, type.getType());
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }

        return (PropertySetType) value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        if (value != null) {
            return ((PropertySetType) value).getType();
        } else
            return null;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        if (cached != null) {
            return PropertySetType.getPropertySetTypeByType((String) cached);
        } else {
            return null;
        }
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
