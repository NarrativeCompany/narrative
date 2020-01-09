package org.narrative.common.persistence;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Dec 21, 2006
 * Time: 3:53:43 PM
 */
public interface Bitmask<T extends BitmaskType> {
    /**
     * bl: we used to use Long.MAX_VALUE to attempt to set all bits, but that doesn't include the most significant (sign) bit.
     * Long.MAX_VALUE is 0x7fffffffffffffffL (9223372036854775807).
     * Long.MIN_VALUE is 0x8000000000000000L (-9223372036854775808).
     * 0xffffffffffffffffL (decimal -1) is exactly what we want: all bits, including sign bit.
     */
    public static final long ALL_BITS = 0xffffffffffffffffL;

    public long getBitmask();

    public T getBitmaskType();
}
