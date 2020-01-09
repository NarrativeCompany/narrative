package org.narrative.shared.redisson.management;

import org.redisson.api.RMapCache;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Component that manage Redisson cache regions (i.e. {@link RMapCache)s
 */
public class RedissonRMapCacheManager {
    private ConcurrentMap<String, RMapCache<Object, Object>> cacheMap = new ConcurrentHashMap<>();

    public void setCacheMap(ConcurrentMap<String, RMapCache<Object, Object>> cacheMap) {
        this.cacheMap = cacheMap;
    }

    public void addCache(String cacheName, RMapCache<Object, Object> cache) {
        cacheMap.putIfAbsent(cacheName, cache);
    }

    public Set<String> getCacheNames() {
        return cacheMap.keySet();
    }

    public RMapCache getCache(String regionName) {
        return cacheMap.get(regionName);
    }
}
