package org.narrative.shared.redisson.management;

import java.util.List;

/**
 * Manager for operations on Redisson objects for a Redisson client.
 */
public interface RedissonObjectManager {
    /**
     * Get the {@link List} of all Redis object names
     *
     * @return {@link List} of names
     */
    List<String> getAllNames();

    /**
     * Get the {@link List} of all object of type Object names
     *
     * @return {@link List} of object names
     */
    List<String> getAllObjectNames();

    /**
     * Get the {@link List} of all Map names
     *
     * @return {@link List} of Map names
     */
    List<String> getAllMapNames();

    /**
     * Get the {@link List} of all List names
     *
     * @return {@link List} of List names
     */
    List<String> getAllListNames();

    /**
     * Get the {@link List} of all Set names
     *
     * @return {@link List} of Set names
     */
    List<String> getAllSetNames();

    /**
     * Get the {@link List} of all ZSet names
     *
     * @return {@link List} of ZSet names
     */
    List<String> getAllZSetNames();

    /**
     * Get the {@link List} of all local cache object names
     *
     * @return {@link List} of object names
     */
    List<String> getAllLocalCacheObjectNames();

    /**
     * Delete a Redisson object
     *
     * @param objectName The object name of interest
     */
    boolean deleteObject(String objectName);

    /**
     * Clear a Redisson local cache across the cluster if one is present
     *
     * @param cacheName The cache name of interest
     */
    boolean clearLocalCache(String cacheName);

    /**
     * Clear a Redis cache
     *
     * @param cacheName The cache name of interest
     */
    boolean clearRedisCache(String cacheName);

    /**
     * Clear a Redisson queue across the cluster if one is present
     *
     * @param queueName The queue name of interest
     */
    boolean clearQueue(String queueName);

    /**
     * Clear a Redisson fair lock across the cluster if one is present
     *
     * @param fairLock The fair lock name of interest
     */
    boolean clearFairLock(String fairLock);

    /**
     * Clear all Redisson caches, both locally and remotely in Redis
     */
    void clearAllCaches();
}
