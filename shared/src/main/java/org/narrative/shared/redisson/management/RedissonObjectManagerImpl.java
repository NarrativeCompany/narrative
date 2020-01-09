package org.narrative.shared.redisson.management;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RKeys;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RQueue;
import org.redisson.api.RType;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager for operations on Redisson objects for a Redisson client.
 */
@Slf4j
public class RedissonObjectManagerImpl implements RedissonObjectManager {
    private final RedissonClient redissonClient;
    private final RedissonRMapCacheManager redissonRMapCacheManager;

    public RedissonObjectManagerImpl(RedissonClient redissonClient, RedissonRMapCacheManager redissonRMapCacheManager) {
        this.redissonClient = redissonClient;
        this.redissonRMapCacheManager = redissonRMapCacheManager;
    }

    private List<String> getObjectNames(RType typeFilter) {
        List<String> resList = new ArrayList<>();
        RKeys rKeys = redissonClient.getKeys();

        rKeys.getKeys().forEach(key -> {
            RType rType = rKeys.getType(key);
            log.debug("Redisson Redis key name: {} Type is: {}", key, rType);
            if (typeFilter == null || typeFilter.equals(rType)) {
                if (!(key.contains("{"))) {
                    resList.add(key);
                }
            }
        });
        Collections.sort(resList);
        return  resList;
    }

    @Override
    public List<String> getAllNames() {
        return getObjectNames(null);
    }

    @Override
    public List<String> getAllMapNames() {
        return getObjectNames(RType.MAP);
    }

    @Override
    public List<String> getAllListNames() {
        return getObjectNames(RType.LIST);
    }

    @Override
    public List<String> getAllObjectNames() {
        return getObjectNames(RType.OBJECT);
    }

    @Override
    public List<String> getAllSetNames() {
        return getObjectNames(RType.SET);
    }

    @Override
    public List<String> getAllZSetNames() {
        return getObjectNames(RType.ZSET);
    }

    @Override
    public List<String> getAllLocalCacheObjectNames() {
        return redissonRMapCacheManager.getCacheNames().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public boolean deleteObject(String objectName) {
        RKeys rKeys = redissonClient.getKeys();
        return rKeys.delete(objectName) == 1;
    }

    @Override
    public boolean clearLocalCache(String cacheName) {
        RMapCache cache = redissonRMapCacheManager.getCache(cacheName);

        if (cache instanceof RLocalCachedMap) {
            ((RLocalCachedMap) cache).clearLocalCache();
            // Also try to clear the Redis cache
            clearRedisCache(cacheName);
            return true;
        } else {
            log.warn("Cache not found with name {}", cacheName);
            return false;
        }
    }

    @Override
    public boolean clearRedisCache(String cacheName) {
        RMapCache cache = redissonClient.getMapCache(cacheName);

        if (cache != null) {
            cache.clear();
            return true;
        } else {
            log.warn("Cache not found with name {}", cacheName);
            return false;
        }
    }

    @Override
    public boolean clearQueue(String queueName) {
        RQueue queue = redissonClient.getQueue(queueName);

        if (queue != null) {
            queue.clear();
            return true;
        } else {
            log.warn("Queue not found with name {}", queueName);
            return false;
        }
    }

    @Override
    public boolean clearFairLock(String fairLock) {
        RLock rLock = redissonClient.getFairLock(fairLock);

        if (rLock != null) {
            rLock.forceUnlock();
            return true;
        } else {
            log.warn("Fair lock not found with name {}", fairLock);
            return false;
        }
    }

    @Override
    public void clearAllCaches() {
        for (String localCacheObjectName : getAllLocalCacheObjectNames()) {
            clearLocalCache(localCacheObjectName);
        }
        // for the remote caches, the best we can do is get a list of all of the Maps and clear those.
        // this will at least exclude things like queues
        for (String objectName : getAllMapNames()) {
            clearRedisCache(objectName);
        }
    }
}
