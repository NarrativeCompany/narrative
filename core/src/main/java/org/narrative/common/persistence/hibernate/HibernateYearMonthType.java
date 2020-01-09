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
import java.time.YearMonth;

/**
 * Date: 2019-05-14
 * Time: 14:54
 *
 * @author jonmark
 */
public class HibernateYearMonthType implements UserType, Serializable {
    private static final long serialVersionUID = -6488299644697399891L;
    public static final String TYPE = "org.narrative.common.persistence.hibernate.HibernateYearMonthType";

    private static final int YEAR_SHIFT = 100;

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.INTEGER};
    }

    @Override
    public Class returnedClass() {
        return YearMonth.class;
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
        Object yearMonthRaw = rs.getObject(name);
        if (yearMonthRaw == null) {
            return null;
        }

        assert yearMonthRaw instanceof Integer : "Expected value to be Long object!";
        int yearMonth = (Integer) yearMonthRaw;
        int month = yearMonth % YEAR_SHIFT;
        int year = (yearMonth - month) / YEAR_SHIFT;
        return YearMonth.of(year, month);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlTypes()[0]);
            return;
        }
        YearMonth yearMonth = (YearMonth) value;
        st.setInt(index, (yearMonth.getYear() * YEAR_SHIFT) + yearMonth.getMonthValue());
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
        return YearMonth.from((YearMonth) value);
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
