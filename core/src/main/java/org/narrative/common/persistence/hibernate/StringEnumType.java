package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.enums.StringEnum;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Date: Dec 11, 2009
 * Time: 9:27:33 AM
 *
 * @author brian
 */
public class StringEnumType extends EnumTypeBase {
    private static final long serialVersionUID = 4912446447302821704L;
    public static final String TYPE = "org.narrative.common.persistence.hibernate.StringEnumType";

    @Override
    Class getExpectedClass() {
        return StringEnum.class;
    }

    @Override
    int sqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        String id = rs.getString(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        return EnumRegistry.getForId((Class<? extends StringEnum>) getEnumClass(), id, true);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlType());
            return;
        }
        st.setObject(index, ((StringEnum) value).getIdStr(), sqlType());
    }
}
