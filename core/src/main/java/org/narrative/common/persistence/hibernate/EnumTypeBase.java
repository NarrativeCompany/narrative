package org.narrative.common.persistence.hibernate;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.EnumRegistry;
import org.hibernate.HibernateException;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.util.Properties;

/**
 * Date: Dec 11, 2009
 * Time: 10:50:25 AM
 *
 * @author brian
 */
public abstract class EnumTypeBase implements UserType, DynamicParameterizedType, Serializable {
    private static final long serialVersionUID = -8082553239315604142L;

    private Class<? extends Enum> enumClass;

    @Override
    @SuppressWarnings("unchecked")
    public void setParameterValues(Properties parameters) {
        ParameterType parameterType = (ParameterType) parameters.get( DynamicParameterizedType.PARAMETER_TYPE );
        assert parameterType != null : "ParameterType should never be null for an enum type";
        enumClass = parameterType.getReturnedClass();
        try {
            if (!getExpectedClass().isAssignableFrom(enumClass)) {
                throw UnexpectedError.getRuntimeException("All EnumTypeBase classes must implement proper interface! cls/" + enumClass.getName() + " doesn't implement " + getExpectedClass());
            }
        } catch (Exception cnfe) {
            throw UnexpectedError.getRuntimeException("Enum class not found cls/" + enumClass.getName(), cnfe);
        }

        EnumRegistry.registerEnumClass(enumClass);
    }

    abstract Class getExpectedClass();

    Class<? extends Enum> getEnumClass() {
        return enumClass;
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{sqlType()};
    }

    abstract int sqlType();

    @Override
    public Class returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
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
