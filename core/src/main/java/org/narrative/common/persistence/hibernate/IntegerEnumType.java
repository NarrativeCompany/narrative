package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.enums.IntegerEnum;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Date: Dec 11, 2009
 * Time: 9:27:26 AM
 *
 * @author brian
 */
public class IntegerEnumType extends EnumTypeBase {
    private static final long serialVersionUID = -6875046536756691511L;
    public static final String TYPE = "org.narrative.common.persistence.hibernate.IntegerEnumType";


    @Override
    Class getExpectedClass() {
        return IntegerEnum.class;
    }

    @Override
    int sqlType() {
        return Types.INTEGER;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        int id = rs.getInt(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        return EnumRegistry.getForId((Class<? extends IntegerEnum>) getEnumClass(), id, true);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlType());
            return;
        }
        st.setObject(index, Integer.valueOf(((IntegerEnum) value).getId()), sqlType());
    }
}
