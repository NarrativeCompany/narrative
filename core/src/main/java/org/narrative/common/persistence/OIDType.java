package org.narrative.common.persistence;

import org.narrative.common.persistence.hibernate.OIDTypeDescriptor;
import org.narrative.common.util.IPStringUtil;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.sql.BigIntTypeDescriptor;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OIDType extends AbstractSingleColumnStandardBasicType<OID> implements DiscriminatorType<OID> {
    public static final OIDType INSTANCE = new OIDType();

    public OIDType() {
        super(BigIntTypeDescriptor.INSTANCE, OIDTypeDescriptor.INSTANCE);
    }

    @Override
    protected boolean registerUnderJavaType() {
        return true;
    }

    @Override
    public OID stringToObject(String xml) throws Exception {
        return OID.valueOf(xml);
    }

    @Override
    public String objectToSQLString(OID value, Dialect dialect) throws Exception {
        return IPStringUtil.nullSafeToString(value);
    }

    @Override
    public String getName() {
        return "oid";
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        return nullSafeGet(rs, names, null, owner);
    }

    public void set(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        // jw: there are times when a BigInteger will be passed in, when a raw OID is fetched via a hibernate sql-query, so
        //     lets just naturally handle that here! Taking this approach to reduce object overhang, since creating OID's
        //     for each of these is wasteful.
        if (value instanceof BigInteger) {
            st.setLong(index, ((BigInteger) value).longValue());
            return;
        }
        OID oidVal = (OID) value;
        if (oidVal == null) {
            st.setObject(index, null);
        } else {
            st.setLong(index, oidVal.oid);
        }
    }

    public String toString(OID value) throws HibernateException {
        return value != null ? value.toString() : null;
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public Serializable disassemble(Object value) throws HibernateException {
        if (value instanceof Serializable) {
            return (Serializable) value;
        } else {
            return null;
        }
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return EqualsHelper.equals(x, y);
    }

    public String toString(OID value, SessionFactoryImplementor factory) throws HibernateException {
        return IPStringUtil.nullSafeToString(value);
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    public Object get(ResultSet rs, String name) throws HibernateException, SQLException {
        Object val = rs.getObject(name);

        //Object is null
        if (val == null) {
            return null;
        }

        //stored as a long in the database
        if (val instanceof Number) {
            return OID.valueOf((Number) val);
        }

        //Stored as a string in the database
        if (val instanceof String) {
            return OID.valueOf((String) val);
        }

        throw new HibernateException("Database column '" + name + "' is not compatable with OIDs");
    }
}
