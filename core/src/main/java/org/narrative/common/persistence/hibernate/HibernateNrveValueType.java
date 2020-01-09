package org.narrative.common.persistence.hibernate;

import org.narrative.network.customizations.narrative.NrveValue;
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
 * Date: 2/23/18
 * Time: 9:53 PM
 */
public class HibernateNrveValueType implements UserType, Serializable {
    private static final long serialVersionUID = -1012492444715896322L;
    public static final String TYPE = "org.narrative.common.persistence.hibernate.HibernateNrveValueType";

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.BIGINT};
    }

    @Override
    public Class returnedClass() {
        return NrveValue.class;
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
        Object neurons = rs.getObject(names[0]);
        if (neurons == null) {
            return null;
        }

        assert neurons instanceof Long : "Expected value to be Long object!";
        return new NrveValue((Long) neurons);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlTypes()[0]);
            return;
        }
        NrveValue nrve = (NrveValue) value;
        st.setLong(index, nrve.toNeurons());
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
        NrveValue nrve = (NrveValue) value;
        return new NrveValue(nrve.toNeurons());
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
