package org.narrative.common.persistence.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/22/18
 * Time: 1:26 PM
 */
public class HibernateInstantType implements UserType, Serializable {
    private static final long serialVersionUID = -469252250507526659L;
    public static final String TYPE = "org.narrative.common.persistence.hibernate.HibernateInstantType";

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.BIGINT};
    }

    @Override
    public Class returnedClass() {
        return Instant.class;
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
        Object instantMs = rs.getObject(name);
        if (instantMs == null) {
            return null;
        }

        assert instantMs instanceof Long : "Expected value to be Long object!";
        return Instant.ofEpochMilli((Long) instantMs);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlTypes()[0]);
            return;
        }
        Instant instant = (Instant) value;
        st.setLong(index, instant.toEpochMilli());
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        return Instant.from((Instant) value);
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        if (value instanceof Serializable) {
            return (Serializable) value;
        }
        return null;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
