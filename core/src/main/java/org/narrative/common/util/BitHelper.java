package org.narrative.common.util;

import java.util.BitSet;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jun 27, 2007
 * Time: 2:55:15 PM
 * This class encapsulates a int or long, and makes it easy to inspect and change individual bits.  It also overrides
 * toString() which will display it as 0's and 1's
 */
public class BitHelper {
    private final BitSet bitSet;

    public BitHelper(byte value) {
        this(value, Byte.SIZE);
    }

    public BitHelper(int value) {
        this(value, Integer.SIZE);
    }

    private BitHelper(int value, int length) {
        this((long) value, length);
    }

    public BitHelper(long value) {
        this(value, Long.SIZE);
    }

    public BitHelper(long value, int size) {
        this.bitSet = new BitSet(size);

        for (int i = 0; i < size; i++) {
            long bitmask = 1L << i;
            if ((bitmask & value) == bitmask) {
                bitSet.set(i);
            }
        }
    }

    public boolean getBit(int pos) {
        return bitSet.get(pos);
    }

    public boolean isAllOn(int... pos) {
        for (int bit : pos) {
            if (!bitSet.get(bit)) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllOff(int... pos) {
        for (int bit : pos) {
            if (bitSet.get(bit)) {
                return false;
            }
        }
        return true;
    }

    public void setBit(int pos, boolean on) {
        bitSet.set(pos);
    }

    public boolean flipBit(int pos) {
        if (bitSet.get(pos)) {
            bitSet.clear(pos);
        } else {
            bitSet.set(pos);
        }
        return getBit(pos);
    }

    public List<Long> getOnBits() {
        List<Long> ret = newLinkedList();
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
            long value = 1L << i;
            ret.add(value);
        }
        return ret;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = bitSet.size() - 1; i > -1; i--) {
            sb.append(bitSet.get(i) ? "1" : "0");
        }
        return sb.toString();
    }
}
