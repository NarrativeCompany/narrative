package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.enums.IntegerEnum;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Apr 29, 2009
 * Time: 9:20:42 AM
 *
 * @author brian
 */
public class HibernateEnumSetType implements UserType, ParameterizedType, Serializable {
    private static final long serialVersionUID = -3155172908443370478L;

    private Class<? extends Enum> enumClass;

    public static final String ENUM_CLASS = "enumClass";

    public static final String TYPE = "org.narrative.common.persistence.hibernate.HibernateEnumSetType";

    @Override
    public void setParameterValues(Properties parameters) {
        Class cls = getClassFromParameters(parameters, ENUM_CLASS);

        if (!Enum.class.isAssignableFrom(cls)) {
            throw UnexpectedError.getRuntimeException("Enum class must be a sub-class of Enum! cls/" + cls.getName());
        }
        if (!IntegerEnum.class.isAssignableFrom(cls)) {
            throw UnexpectedError.getRuntimeException("Enum class must be a sub-class of IntegerEnum! cls/" + cls.getName());
        }
        if (cls.getEnumConstants().length > 63) {
            throw UnexpectedError.getRuntimeException("Don't support EnumSets in Hibernate for enums that have more than 63 enum values. cls/" + cls.getName());
        }
        enumClass = (Class<? extends Enum>) cls;

        // bl: make sure that there are no duplicate ids in the enum
        Set<Integer> uniqueIds = newHashSet();
        for (Enum anEnum : enumClass.getEnumConstants()) {
            int id = ((IntegerEnum) anEnum).getId();
            if (uniqueIds.contains(id)) {
                throw UnexpectedError.getRuntimeException("Can't have duplicate ids in an IntegerEnum! id/" + id + " cls/" + enumClass);
            }
            if (id < 0 || id > 62) {
                throw UnexpectedError.getRuntimeException("Currently only supporting ids for EnumSets between 0 and 62! id/" + id + " cls/" + enumClass);
            }
            uniqueIds.add(id);
        }
    }

    public static Class getClassFromParameters(Properties params, String name) {
        String classStr = params.getProperty(name);
        Class cls = null;
        try {
            cls = Class.forName(classStr);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (cls == null) {
            throw UnexpectedError.getRuntimeException("Failed getting class for parameter: " + name + " string: " + classStr);
        }

        return cls;
    }

    public Class<? extends Enum> getEnumClass() {
        assert enumClass != null : "Should not be possible to get an EnumSet type without a valid enumClass!";
        return enumClass;
    }

    public int[] sqlTypes() {
        return new int[]{sqlType()};
    }

    public int sqlType() {
        return Types.BIGINT;
    }

    public Class returnedClass() {
        return EnumSet.class;
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
        EnumSet enumSet = EnumSet.noneOf(getEnumClass());
        if (val == null || rs.wasNull()) {
            return enumSet;
        }

        //Stored as a long in the database
        if (!(val instanceof Long)) {
            // jw: we need to try and support smaller values from the database
            if (val instanceof Number) {
                Number number = (Number) val;
                val = number.longValue();
            } else {
                throw new HibernateException("Database column '" + name + "' is not compatible with EnumSet");
            }
        }

        long bitmask = (Long) val;

        return parseEnumSet(getEnumClass(), bitmask);
    }

    public static <T extends Enum<T>> EnumSet<T> parseEnumSet(Class<T> enumClass, long bitmask) {
        EnumSet enumSet = EnumSet.noneOf(enumClass);

        if (bitmask > 0) {
            Enum[] enums = enumClass.getEnumConstants();
            for (Enum anEnum : enums) {
                long currentBit = EnumRegistry.getBitForIntegerEnum((IntegerEnum) anEnum);
                if ((bitmask & currentBit) != 0) {
                    enumSet.add(anEnum);
                    bitmask -= currentBit;
                    if (bitmask <= 0) {
                        break;
                    }
                }
            }
        }
        return enumSet;
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlType());
            return;
        }

        EnumSet<? extends IntegerEnum> enumSet = (EnumSet<? extends IntegerEnum>) value;
        st.setLong(index, createBitmask(enumSet));
    }

    public static long createBitmask(Set<? extends IntegerEnum> enums) {
        long bitmask = 0;
        for (IntegerEnum enumVal : enums) {
            bitmask |= EnumRegistry.getBitForIntegerEnum(enumVal);
        }

        return bitmask;
    }

    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        return EnumSet.copyOf((EnumSet) value);
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
