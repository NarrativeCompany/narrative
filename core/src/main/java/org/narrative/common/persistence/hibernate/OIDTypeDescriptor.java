package org.narrative.common.persistence.hibernate;

import org.narrative.common.persistence.OID;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OIDTypeDescriptor extends AbstractTypeDescriptor<OID> {
    public static OIDTypeDescriptor INSTANCE = new OIDTypeDescriptor();

    public OIDTypeDescriptor() {
        super(OID.class, new ImmutableMutabilityPlan<>());
    }

    @Override
    public String toString(OID value) {
        return value.toString();
    }

    @Override
    public OID fromString(String string) {
        return OID.valueOf(string);
    }

    /**
     * Unwrap an instance of our handled Java type into the requested type.
     * <p/>
     * As an example, if this is a {@code JavaTypeDescriptor<Integer>} and we are asked to unwrap
     * the {@code Integer value} as a {@code Long} we would return something like
     * <code>Long.valueOf( value.longValue() )</code>.
     * <p/>
     * Intended use is during {@link PreparedStatement} binding.
     *
     * @param value   The value to unwrap
     * @param type    The type as which to unwrap
     * @param options The options
     * @return The unwrapped value.
     */
    @Override
    public <X> X unwrap(OID value, Class<X> type, WrapperOptions options) {
        assert Long.class.equals(type) : "Type should always be long";
        //noinspection unchecked
        return (X) Long.valueOf(value.getValue());
    }

    /**
     * Wrap a value as our handled Java type.
     * <p/>
     * Intended use is during {@link ResultSet} extraction.
     *
     * @param value   The value to wrap.
     * @param options The options
     * @return The wrapped value.
     */
    @Override
    public <X> OID wrap(X value, WrapperOptions options) {
        return OID.valueOf((Number) value);
    }
}
