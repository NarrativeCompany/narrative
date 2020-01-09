package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.enums.IntegerEnum;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/23/18
 * Time: 9:31 AM
 */
public class BitIntegerEnumType extends EnumTypeBase {
    private static final long serialVersionUID = -329970611730833806L;
    public static final String TYPE = "org.narrative.common.persistence.hibernate.BitIntegerEnumType";

    @Override
    Class getExpectedClass() {
        return IntegerEnum.class;
    }

    @Override
    int sqlType() {
        return Types.BIT;
    }

    public void setParameterValues(Properties parameters) {
        // jw: this will setup the enumClass, and validate it.
        super.setParameterValues(parameters);

        // jw: let's validate that this IntegerEnum has values between 0 and 1.
        for (IntegerEnum enumValue : (IntegerEnum[]) getEnumClass().getEnumConstants()) {
            if (enumValue.getId() < 0 || enumValue.getId() > 1) {
                throw UnexpectedError.getRuntimeException("Enums used for BitIntegerEnumTypes should have values of 0 or 1! " + getEnumClass().getSimpleName() + "." + enumValue);
            }
        }
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        boolean isOne = rs.getBoolean(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        return EnumRegistry.getForId((Class<? extends IntegerEnum>) getEnumClass(), isOne ? 1 : 0, true);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlType());
            return;
        }
        IntegerEnum enumValue = (IntegerEnum) value;
        st.setObject(index, enumValue.getId() == 1 ? Boolean.TRUE : Boolean.FALSE, sqlType());
    }
}
