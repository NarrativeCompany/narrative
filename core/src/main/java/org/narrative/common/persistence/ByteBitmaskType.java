package org.narrative.common.persistence;

import java.util.Collection;

/**
 * Date: Jan 9, 2007
 * Time: 9:58:00 AM
 *
 * @author Brian
 */
public class ByteBitmaskType<T extends Bitmask> extends BitmaskType<T> {

    public ByteBitmaskType() {}

    public ByteBitmaskType(T... values) {
        super(values);
    }

    public ByteBitmaskType(Collection<T> values) {
        super(values);
    }

    public ByteBitmaskType(byte value) {
        super(value);
    }

    public int sqlType() {
        return java.sql.Types.TINYINT;
    }

    protected ByteBitmaskType<T> getBitmaskType(Number value) {
        return new ByteBitmaskType<T>(value.byteValue());
    }

    /**
     * Create a clone of the incoming object
     */
    @Override
    public Object clone(Object original) {
        return getBitmaskType(((ByteBitmaskType)original).value);
    }
}
