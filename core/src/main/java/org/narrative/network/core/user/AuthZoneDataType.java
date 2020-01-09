package org.narrative.network.core.user;

import org.narrative.common.persistence.OID;
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
 * Date: Sep 22, 2010
 * Time: 10:18:26 AM
 *
 * @author brian
 */
public class AuthZoneDataType implements UserType, Serializable {
    private static final long serialVersionUID = 1630957746599411048L;
    public static final String TYPE = "org.narrative.network.core.user.AuthZoneDataType";

    /**
     * @deprecated for hibernate use only
     */
    public AuthZoneDataType() {}

    @Override
    public int[] sqlTypes() {
        return new int[]{sqlType()};
    }

    private int sqlType() {
        return Types.BIGINT;
    }

    @Override
    public Class returnedClass() {
        return AuthZone.class;
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
        long val = rs.getLong(name);

        //Object is null
        if (rs.wasNull()) {
            return null;
        }

        return AuthZone.getAuthZone(val);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlType());
            return;
        }

        OID authZoneOid;
        if (value instanceof OID) {
            authZoneOid = (OID) value;
        } else {
            AuthZone authZone = (AuthZone) value;
            authZoneOid = authZone.getOid();
        }

        st.setLong(index, authZoneOid.getValue());
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        AuthZone authZone = (AuthZone) value;
        return AuthZone.getAuthZone(authZone.getOid().getValue());
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        if (value instanceof Serializable) {
            return (Serializable) value;
        } else {
            return null;
        }
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
