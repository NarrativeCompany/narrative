package org.narrative.config.cache.spring;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.MapOptions;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.LocalCachedCacheConfig;

import java.util.concurrent.TimeUnit;

/**
 * Builders for Redisson configuration objects.
 */
public class CacheConfigFactory {
    /**
     * Build a non-local cache configuration
     *
     * @param ttl Time to live in minutes
     * @param maxIdleTime Max idle time in seconds
     * @return {@link LocalCachedCacheConfig} instance
     */
    public static org.redisson.spring.cache.CacheConfig buildCacheConfig(long ttl, long maxIdleTime) {
        return buildCacheConfig(ttl, maxIdleTime, 0);
    }

    /**
     * Build a non-local cache configuration
     *
     * @param ttlMinutes Time to live in minutes
     * @param maxIdleTimeMinutes Max idle time in seconds
     * @param size Maximum number of items to cache
     * @return {@link LocalCachedCacheConfig} instance
     */
    public static org.redisson.spring.cache.CacheConfig buildCacheConfig(long ttlMinutes, long maxIdleTimeMinutes, int size){
        return buildCacheConfig(TimeUnit.MINUTES, ttlMinutes, maxIdleTimeMinutes, size);
    }

    /**
     * Build a non-local cache configuration
     *
     * @param timeUnit The TimeUnit for the TTL values
     * @param ttl The TTL for items in the cache (in the TimeUnit specified)
     * @param maxIdleTime The max idle time for items in the cache (in the TimeUnit specified)
     * @param size Maximum number of items to cache
     * @return {@link LocalCachedCacheConfig} instance
     */
    public static org.redisson.spring.cache.CacheConfig buildCacheConfig(TimeUnit timeUnit, long ttl, long maxIdleTime, int size){
        CacheConfig res = new CacheConfig(timeUnit.toMillis(ttl),
                                          timeUnit.toMillis(maxIdleTime));
        res.setMaxSize(size);
        return res;
    }

    /**
     * Build a local cache configuration
     *
     * @param ttlMinutes Time to live in minutes
     * @param maxIdleTimeMinutes Max idle time in minutes
     * @return {@link LocalCachedCacheConfig} instance
     */
    public static LocalCachedCacheConfig buildLocalCacheConfig(long ttlMinutes, long maxIdleTimeMinutes) {
        return buildLocalCacheConfig(ttlMinutes, maxIdleTimeMinutes, 0);
    }

    /**
     * Build a local cache configuration
     *
     * @param ttlMinutes Time to live in minutes
     * @param maxIdleTimeMinutes Max idle time in minutes
     * @param size Maximum number of items to cache
     * @return {@link LocalCachedCacheConfig} instance
     */
    public static LocalCachedCacheConfig buildLocalCacheConfig(long ttlMinutes, long maxIdleTimeMinutes, int size) {
        return buildLocalCacheConfig(TimeUnit.MINUTES,
                ttlMinutes,
                maxIdleTimeMinutes,
                size,
                LocalCachedMapOptions.ReconnectionStrategy.CLEAR,
                LocalCachedMapOptions.SyncStrategy.INVALIDATE,
                LocalCachedMapOptions.EvictionPolicy.LRU,
                MapOptions.WriteMode.WRITE_THROUGH,
                size,
                ttlMinutes,
                maxIdleTimeMinutes
        );
    }

    /**
     * Build a local cache configuration
     *
     * @param timeUnit Time units to use
     * @param ttl Time to live in the {@link TimeUnit} specified
     * @param maxIdleTime Max idle time in the {@link TimeUnit} specified
     * @param size Maximum number of items to cache
     * @param localReconnectionStrategy The reconnection strategy to use
     * @param localSyncStrategy The sync strategy to use
     * @param evictionPolicy The eviction policy to use
     * @param writeMode The write mode to use
     * @return {@link LocalCachedCacheConfig} instance
     */
    public static LocalCachedCacheConfig buildLocalCacheConfig(TimeUnit timeUnit,
                                                                long ttl,
                                                                long maxIdleTime,
                                                                int size,
                                                                LocalCachedMapOptions.ReconnectionStrategy localReconnectionStrategy,
                                                                LocalCachedMapOptions.SyncStrategy localSyncStrategy,
                                                                LocalCachedMapOptions.EvictionPolicy evictionPolicy,
                                                                MapOptions.WriteMode writeMode,
                                                                int localCacheSize,
                                                                long localTimeToLive,
                                                                long localMaxIdle){
        LocalCachedMapOptions options = LocalCachedMapOptions.defaults()
                .cacheSize(localCacheSize)
                .timeToLive(localTimeToLive, timeUnit)
                .maxIdle(localMaxIdle, timeUnit)
                .reconnectionStrategy(localReconnectionStrategy)
                .syncStrategy(localSyncStrategy)
                .writeMode(writeMode)
                .evictionPolicy(evictionPolicy);

        LocalCachedCacheConfig res = new LocalCachedCacheConfig(timeUnit.toMillis(ttl),
                timeUnit.toMillis(maxIdleTime),
                options);
        res.setMaxSize(size);

        return res;
    }
}
