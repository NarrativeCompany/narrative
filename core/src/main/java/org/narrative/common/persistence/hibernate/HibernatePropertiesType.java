package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.IPUtil;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

/**
 * Date: Jun 15, 2006
 * Time: 9:09:50 AM
 *
 * @author Brian
 */
public class HibernatePropertiesType implements UserType, Serializable {
    private static final long serialVersionUID = 6960958452466713570L;
    public static final String TYPE = "org.narrative.common.persistence.hibernate.HibernatePropertiesType";

    public int[] sqlTypes() {
        return new int[]{sqlType()};
    }

    public int sqlType() {
        return Types.CLOB;
    }

    public Class returnedClass() {
        return Properties.class;
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
            return null;
        }

        //Stored as a string in the database
        if (!(val instanceof String)) {
            throw new HibernateException("Database column '" + name + "' is not compatable with Properties");
        }

        return IPUtil.getPropertiesObjectFromSerializedPropertiesString((String) val);
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlType());
            return;
        }

        Properties properties = (Properties) value;
        st.setString(index, IPUtil.getSerializedPropertiesObject(properties));
    }

    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        Properties ret = new Properties();
        ret.putAll((Properties) value);
        return ret;
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
