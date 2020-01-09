package org.narrative.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * User: Brian
 * Date: Jun 16, 2005
 * Time: 1:37:12 AM
 * Basic Iterator implementation to simplify the task of iterating over chunks of a Map
 */
public class SubMapIterator<K, V> implements Iterator<Map<K, V>> {

    private final int chunkSize;

    private Iterator<Map.Entry<K, V>> iter;

    /**
     * Creates a new SubMapIterator initialized at the beginning of the provided map
     * and allows the creator to set the chunk size during instantiation
     *
     * @param map       The Map that we want to iterate over chunks of its elements
     * @param chunkSize The Maximum number of elements to return in each sub map (asseted to be > 0)
     */
    public SubMapIterator(Map<K, V> map, int chunkSize) {
        assert chunkSize > 0 : "A SubMapIterator must be instantiated to read a positive number of elements!";

        this.chunkSize = chunkSize;
        this.iter = map.entrySet().iterator();
    }

    /**
     * Retrieves the maximum amount of items that will be contained within
     * each sub map created
     *
     * @return The maximum number of elements in the next resulting map
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Determines if there are still elements in the map to return sub maps for
     *
     * @return Whether or not there are more sub maps
     */
    public boolean hasNext() {
        return iter.hasNext();
    }

    /**
     * Retrieves the next sub map and prepares to read the next sub map
     *
     * @return The Map containing the next chunk of key/value pairs
     */
    public Map<K, V> next() {
        if (!iter.hasNext()) {
            throw new NoSuchElementException("No more elements in SubMapIterator");
        }

        Map<K, V> ret = new HashMap<K, V>();
        for (int i = 0; i < chunkSize && iter.hasNext(); i++) {
            Map.Entry<K, V> entry = iter.next();
            ret.put(entry.getKey(), entry.getValue());
        }

        return ret;
    }

    /**
     * Removes the last Sub Map of results from the underlying Map object
     * that we are reading from
     */
    public void remove() {
        throw new UnsupportedOperationException("remove not supported for SubMapIterator!");
    }
}