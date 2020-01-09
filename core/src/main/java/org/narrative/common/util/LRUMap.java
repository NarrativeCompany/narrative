package org.narrative.common.util;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jun 12, 2005
 * Time: 1:51:49 PM
 */
public class LRUMap<K, V> {

    public static final int DEFAULT_CACHE_SIZE = 1000;

    private int maxSize;
    private long accesses = 0;
    private long hits = 0;
    private long dropoffs = 0;
    private long puts = 0;

    private final Map<K, V> map;
    private final EvictionListener<K, V> listener;

    public LRUMap() {
        this(DEFAULT_CACHE_SIZE);
    }

    public LRUMap(int maxSize) {
        this(maxSize, null);
    }

    public LRUMap(int maxSize, EvictionListener<K, V> listener) {
        this.maxSize = maxSize;
        this.listener = listener;
        map = new ConcurrentLinkedHashMap.Builder<K, V>().maximumWeightedCapacity(maxSize).listener(new MapEvictionListener()).build();
    }

    public void put(K key, V object) {
        puts++;
        map.put(key, object);
    }

    public V get(K key) {
        accesses++;
        V object = map.get(key);
        if (object != null) {
            hits++;
        }
        return object;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public V remove(K key) {
        return map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public long getPuts() {
        return puts;
    }

    public long getDropoffs() {
        return dropoffs;
    }

    public float getDropoffRate() {
        return puts > 0 ? (dropoffs / (float) puts) : 0;
    }

    public float getHitRate() {
        return accesses > 0 ? (hits / (float) accesses) : 0;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public long getAccesses() {
        return accesses;
    }

    public long getHits() {
        return hits;
    }

    public Map<K, V> getMap() {
        return map;
    }

    private class MapEvictionListener implements EvictionListener<K, V> {
        @Override
        public void onEviction(K key, V value) {
            dropoffs++;
            if (listener != null) {
                listener.onEviction(key, value);
            }
        }
    }
}