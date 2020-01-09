package org.narrative.common.persistence;

import org.narrative.common.persistence.hibernate.legacy.NullableType;
import org.narrative.common.util.UnexpectedError;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Dec 21, 2006
 * Time: 3:52:59 PM
 */
public abstract class BitmaskType<T extends Bitmask> extends NullableType<T> implements UserType, Serializable {

    protected long value;
    /*private transient Set<T> bitmasks;
    private Class<? extends Enum> enumClass;

    private static Map<Class, Bitmask[]> enumValues = new HashMap<Class, Bitmask[]>();
    private static final String ENUM = "enumClass";*/

    public BitmaskType() {}

    public BitmaskType(T... values) {
        for (T value : values) {
            this.value |= value.getBitmask();
        }
    }

    public BitmaskType(Collection<T> values) {
        for (T value : values) {
            this.value |= value.getBitmask();
        }
    }

    protected BitmaskType(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public Long getLongValue() {
        return Long.valueOf(value);
    }

    public boolean isThis(T bitmask) {
        if (bitmask.getBitmask() == 0) {
            return value == 0;
        } else {
            return ((bitmask.getBitmask() & value) != 0);
        }
    }

    public boolean isOnlyThis(T bitmask) {
        return bitmask.getBitmask() == value;
    }

    public void turnOn(T bitmask) {
        if (bitmask.getBitmask() == 0) {
            value = 0;
        } else {
            value |= bitmask.getBitmask();
        }
    }

    public void turnOff(T bitmask) {
        if (bitmask.getBitmask() != 0) {
            value &= ~bitmask.getBitmask();
        }
    }

    public void turnOffAll() {
        value = 0;
    }

    public <T extends Bitmask> Set<T> getItems(Class<T> clzz) {
        Set<T> ret = new LinkedHashSet<T>();
        for (T method : clzz.getEnumConstants()) {
            if ((value & method.getBitmask()) != 0) {
                ret.add(method);
            }
        }
        return ret;
    }

    @Override
    public Object get(ResultSet rs, String name) throws HibernateException, SQLException {
        Number val = (Number) rs.getObject(name);
        if (val == null) {
            return null;
        }
        return getBitmaskType(val.longValue());
    }

    protected abstract BitmaskType<T> getBitmaskType(Number value);

    @Override
    public void set(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setObject(index, null);
        } else {
            int sqlType = sqlType();
            switch (sqlType) {
                case (java.sql.Types.BIGINT):
                    st.setLong(index, ((BitmaskType) value).getValue());
                    break;
                case (java.sql.Types.INTEGER):
                    st.setInt(index, ((BitmaskType) value).getLongValue().intValue());
                    break;
                case (java.sql.Types.SMALLINT):
                    st.setShort(index, ((BitmaskType) value).getLongValue().shortValue());
                    break;
                case (java.sql.Types.TINYINT):
                    st.setByte(index, ((BitmaskType) value).getLongValue().byteValue());
                    break;
                default:
                    throw UnexpectedError.getRuntimeException("Found a BitmaskType with an unrecognized type! type/" + sqlType);
            }
        }
    }

    @Override
    public String toString(Object value) throws HibernateException {
        return value != null ? value.toString() : null;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public Object fromStringValue(String xml) throws HibernateException {
        return getBitmaskType(Long.parseLong(xml));
    }

    @Override
    public Class getReturnedClass() {
        return getClass();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
        return target;
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{sqlType()};
    }

    @Override
    public Class returnedClass() {
        return getClass();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BitmaskType that = (BitmaskType) o;

        if (value != that.value) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o1, Object o2) {
        return (o1 != null && o1.equals(o2));
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public int hashCode() {
        return Long.valueOf(value).hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        return nullSafeGet(rs, names, null, owner);
    }

    @Override
    public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
        if (value == null) {
            return null;
        }
        return getBitmaskType(((BitmaskType) value).getValue());
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, SharedSessionContractImplementor session, Object owner, Map copyCache) throws HibernateException {
        return target;
    }

    public void set(T status, boolean isOn) {
        if (isOn) {
            turnOn(status);
        } else {
            turnOff(status);
        }
    }

    /**
     * Should the parent be considered dirty, given both the old and current value?
     *
     * @param oldState     the old value
     * @param currentState the current value
     * @param checkable    An array of booleans indicating which columns making up the value are actually checkable
     * @param session      The session from which the request originated.
     * @return true if the field is dirty
     * @throws HibernateException A problem occurred performing the checking
     */
    @Override
    public final boolean isDirty(Object oldState, Object currentState, boolean[] checkable, SharedSessionContractImplementor session) {
        return checkable[0] && isDirty(oldState, currentState, session);
    }

    /**
     * A convenience form of {@link #sqlTypes(Mapping)}, returning
     * just a single type value since these are explicitly dealing with single column
     * mappings.
     *
     * @return The {@link Types} mapping value.
     */
    @Override
    public int sqlType() {
        return sqlTypes()[0];
    }

    /**
     * During merge, replace the existing (target) value in the entity we are merging to
     * with a new (original) value from the detached entity we are merging. For immutable
     * objects, or null values, it is safe to simply return the first parameter. For
     * mutable objects, it is safe to return a copy of the first parameter. For objects
     * with component values, it might make sense to recursively replace component values.
     *
     * @param original the value from the detached entity being merged
     * @param target   the value in the managed entity
     * @param owner
     * @return the value to be merged
     */
    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    /**
     * Return a deep copy of the persistent state, stopping at entities and at
     * collections. It is not necessary to copy immutable objects, or null
     * values, in which case it is safe to simply return the argument.
     *
     * @param value the object to be cloned, which may be null
     * @return Object a copy
     */
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return deepCopy(value, null);
    }
}
