package org.narrative.common.persistence.hibernate.legacy;

import org.dom4j.Node;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.type.AbstractType;
import org.hibernate.type.StringRepresentableType;
import org.jboss.logging.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mostly a clone of Hibernate 3.x NullableType which has been removed from Hibernate 4.x+.
 * <p>
 */
@Deprecated
public abstract class NullableType<T> extends AbstractType implements StringRepresentableType {
    private static final CoreMessageLogger LOG = Logger.getMessageLogger(CoreMessageLogger.class, NullableType.class.getName());

    private final Size dictatedSize = new Size();

    /**
     * A convenience form of {@link #sqlTypes(org.hibernate.engine.spi.Mapping)}, returning
     * just a single type value since these are explicitly dealing with single column
     * mappings.
     *
     * @return The {@link java.sql.Types} mapping value.
     */
    public abstract int sqlType();

    /**
     * A convenience form of {@link #dictatedSizes}, returning just a single size since we are explicitly dealing with
     * single column mappings here.
     *
     * @return The {@link java.sql.Types} mapping value.
     */
    public Size dictatedSize() {
        return dictatedSize;
    }

    /**
     * A convenience form of {@link #defaultSizes}, returning just a single size since we are explicitly dealing with
     * single column mappings here.
     *
     * @return The {@link java.sql.Types} mapping value.
     */
    public Size defaultSize() {
        return LEGACY_DEFAULT_SIZE;
    }

    /**
     * Get a column value from a result set, without worrying about the
     * possibility of null values.  Called from {@link #nullSafeGet} after
     * nullness checks have been performed.
     *
     * @param rs The result set from which to extract the value.
     * @param name The name of the value to extract.
     *
     * @return The extracted value.
     *
     * @throws org.hibernate.HibernateException Generally some form of mismatch error.
     * @throws java.sql.SQLException Indicates problem making the JDBC call(s).
     */
    public abstract Object get(ResultSet rs, String name) throws HibernateException, SQLException;

    /**
     * Set a parameter value without worrying about the possibility of null
     * values.  Called from {@link #nullSafeSet} after nullness checks have
     * been performed.
     *
     * @param st The statement into which to bind the parameter value.
     * @param value The parameter value to bind.
     * @param index The position or index at which to bind the param value.
     *
     * @throws org.hibernate.HibernateException Generally some form of mismatch error.
     * @throws java.sql.SQLException Indicates problem making the JDBC call(s).
     */
    public abstract void set(PreparedStatement st, Object value, int index) throws HibernateException, SQLException;

    /**
     * A null-safe version of {@link #toString(Object)}.  Specifically we are
     * worried about null safeness in regards to the incoming value parameter,
     * not the return.
     *
     * @param value The value to convert to a string representation; may be null.
     * @return The string representation; may be null.
     * @throws HibernateException Thrown by {@link #toString(Object)}, which this calls.
     */
    public String nullSafeToString(Object value) throws HibernateException {
        return value == null ? null : toString( value );
    }

    public abstract String toString(Object value) throws HibernateException;

    public abstract Object fromStringValue(String xml) throws HibernateException;

    public final void nullSafeSet(
            PreparedStatement st,
            Object value,
            int index,
            boolean[] settable,
            SessionImplementor session)
            throws HibernateException, SQLException {
        if ( settable[0] ) nullSafeSet(st, value, index);
    }

    public final void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
            throws HibernateException, SQLException {
        nullSafeSet(st, value, index);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (settable[0]) {
            nullSafeSet(st, value, index);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        nullSafeSet(st, value, index);
    }

    public final void nullSafeSet(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        try {
            if ( value == null ) {
                if (LOG.isTraceEnabled()) LOG.trace("Binding null to parameter: " + index);

                st.setNull( index, sqlType() );
            }
            else {
                if (LOG.isTraceEnabled()) LOG.trace("Binding '" + toString(value) + "' to parameter: " + index);

                set( st, value, index );
            }
        }
        catch ( RuntimeException re ) {
            LOG.unableToBindValueToParameter(nullSafeToString(value), index, re.getMessage());
            throw re;
        }
        catch ( SQLException se ) {
            LOG.unableToBindValueToParameter(nullSafeToString(value), index, se.getMessage());
            throw se;
        }
    }

    public final Object nullSafeGet(
            ResultSet rs,
            String[] names,
            SessionImplementor session,
            Object owner)
            throws HibernateException, SQLException {
        return nullSafeGet(rs, names[0]);
    }

    public final Object nullSafeGet(ResultSet rs, String[] names)
            throws HibernateException, SQLException {
        return nullSafeGet(rs, names[0]);
    }

    public final Object nullSafeGet(ResultSet rs, String name)
            throws HibernateException, SQLException {
        try {
            Object value = get(rs, name);
            if ( value == null || rs.wasNull() ) {
                if (LOG.isTraceEnabled()) LOG.trace("Returning null as column " + name);
                return null;
            }
            if (LOG.isTraceEnabled()) LOG.trace("Returning '" + toString(value) + "' as column " + name);
            return value;
        }
        catch ( RuntimeException re ) {
            LOG.unableToReadColumnValueFromResultSet(name, re.getMessage());
            throw re;
        }
        catch ( SQLException se ) {
            LOG.unableToReadColumnValueFromResultSet(name, se.getMessage());
            throw se;
        }
    }

    public final Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        return nullSafeGet( rs, name );
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String name, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        return nullSafeGet( rs, name );
    }

    public final String toXMLString(Object value, SessionFactoryImplementor pc)
            throws HibernateException {
        return toString(value);
    }

    public final Object fromXMLString(String xml, Mapping factory) throws HibernateException {
        return xml==null || xml.length()==0 ? null : fromStringValue(xml);
    }

    public final int getColumnSpan(Mapping session) {
        return 1;
    }

    public final int[] sqlTypes(Mapping session) {
        return new int[] { sqlType() };
    }

    @Override
    public Size[] dictatedSizes(Mapping mapping) throws MappingException {
        return new Size[] { dictatedSize() };
    }

    @Override
    public Size[] defaultSizes(Mapping mapping) throws MappingException {
        return new Size[] { defaultSize() };
    }

    @Override
    public boolean isEqual(Object x, Object y) {
        return EqualsHelper.equals(x, y);
    }

    public String toLoggableString(Object value, SessionFactoryImplementor factory) {
        return value == null ? "null" : toString(value);
    }

    public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
        return fromXMLString( xml.getText(), factory );
    }

    public void setToXMLNode(Node xml, Object value, SessionFactoryImplementor factory)
            throws HibernateException {
        xml.setText( toXMLString(value, factory) );
    }

    public boolean[] toColumnNullness(Object value, Mapping mapping) {
        return value==null ? ArrayHelper.FALSE : ArrayHelper.TRUE;
    }

    public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session)
            throws HibernateException {
        return checkable[0] && isDirty(old, current, session);
    }

    /**
     * Create a clone of the incoming object
     */
    public abstract Object clone(Object original);
}