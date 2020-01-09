package org.narrative.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 21, 2007
 * Time: 11:30:28 AM
 * <p>
 * An LRU map that not only has a max entry size, but also a max user defined size.  Useful for
 * keeping the memory footprint of the total LRU down, where the user defined size is the size of
 * the object being stored in memory.
 */
public class SizedLRUMap extends org.apache.commons.collections4.map.LRUMap {

    private long maxUserDefinedSize;
    private long currentSize = 0;
    private final Map<Object, Long> sizes = new HashMap<Object, Long>();

    public SizedLRUMap(int maxSize, long maxUserDefinedSize) {
        super(maxSize);
        this.maxUserDefinedSize = maxUserDefinedSize;
    }

    public SizedLRUMap(int maxSize, float loadFactor, long maxUserDefinedSize) {
        super(maxSize, loadFactor);
        this.maxUserDefinedSize = maxUserDefinedSize;
    }

    /**
     * @deprecated use put with userDefinedSize parameter
     */
    public Object put(Object key, Object value) {
        assert false : "Method not supported.  Use put with userDefinedSize parameter";
        return null;
    }

    /**
     * Puts an object in the map, with a specified size.
     *
     * @param key             The key of the entry
     * @param value           The value of the entry.
     * @param userDefinedSize The user defined size of the entry.
     * @return
     */
    public Object put(Object key, Object value, long userDefinedSize) {
        Long originalSize = sizes.get(key);
        sizes.put(key, userDefinedSize);
        // bl: currentSize needs to account for the original size of this key in the map if it was already in the map!
        currentSize += (userDefinedSize - (originalSize == null ? 0 : originalSize));

        if (isOverCapacity()) {
            synchronized (this) {
                while (isOverCapacity() && !this.isEmpty()) {
                    this.remove(this.firstKey());
                }
            }
        }

        return super.put(key, value);
    }

    protected void removeEntry(HashEntry entry, int hashIndex, HashEntry previous) {
        removeEntrySize(entry);
        super.removeEntry(entry, hashIndex, previous);
    }

    protected void updateEntry(HashEntry entry, Object newValue) {
        removeEntrySize(entry);
        super.updateEntry(entry, newValue);
    }

    private void removeEntrySize(HashEntry entry) {
        Long size = sizes.remove(entry.getKey());
        if (size != null) {
            currentSize -= size;
        }
    }

    public void clear() {
        super.clear();
        sizes.clear();
        currentSize = 0;
    }

    /**
     * bl: this method used to override isFull, but that would cause lots of issues internally with LRUMap
     * which relies on isFull actually returning true only when it's full (not when it's "over capacity"
     * which is what we are really using it for). so, i've redefined this to isOverCapacity so that we
     * can do our capacity removals completely independently of LRUMap's isFull implementation.
     * that should fix the super annoying issues with the map breaking and never fixing until servlet restart.
     */
    public boolean isOverCapacity() {
        return currentSize >= maxUserDefinedSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }
}
