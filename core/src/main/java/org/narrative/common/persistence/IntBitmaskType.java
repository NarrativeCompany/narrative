package org.narrative.common.persistence;

import java.util.Collection;

/**
 * Date: Jan 9, 2007
 * Time: 9:58:00 AM
 *
 * @author Brian
 */
public class IntBitmaskType<T extends Bitmask> extends BitmaskType<T> {

    public IntBitmaskType() {}

    public IntBitmaskType(T... values) {
        super(values);
    }

    public IntBitmaskType(Collection<T> values) {
        super(values);
    }

    public IntBitmaskType(int value) {
        super(value);
    }

    public int sqlType() {
        return java.sql.Types.INTEGER;
    }

    public IntBitmaskType<T> getBitmaskType(Number value) {
        return new IntBitmaskType<T>(value.intValue());
    }

    /**
     * Create a clone of the incoming object
     */
    @Override
    public Object clone(Object original) {
        return getBitmaskType(((IntBitmaskType)original).value);
    }
}
