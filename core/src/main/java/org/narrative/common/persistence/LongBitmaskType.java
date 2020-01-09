package org.narrative.common.persistence;

import java.util.Collection;

/**
 * Date: Jan 9, 2007
 * Time: 9:58:00 AM
 *
 * @author Brian
 */
public class LongBitmaskType<T extends Bitmask> extends BitmaskType<T> {

    public LongBitmaskType() {}

    public LongBitmaskType(T... values) {
        super(values);
    }

    public LongBitmaskType(Collection<T> values) {
        super(values);
    }

    public LongBitmaskType(long value) {
        super(value);
    }

    public int sqlType() {
        return java.sql.Types.BIGINT;
    }

    public LongBitmaskType<T> getBitmaskType(Number value) {
        return new LongBitmaskType<T>(value.longValue());
    }

    /**
     * Create a clone of the incoming object
     */
    @Override
    public Object clone(Object original) {
        return getBitmaskType(((LongBitmaskType)original).value);
    }
}
