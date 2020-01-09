package org.narrative.shared.reputation.config.redisson;

import org.narrative.shared.redisson.codec.NarrativeJsonJacksonCodec;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redisson queue wrapper that manages queue instances
 */
public class ReputationRedissonQueueService {
    private final NarrativeJsonJacksonCodec codec = new NarrativeJsonJacksonCodec();
    private final RedissonClient redissonClient;
    private final Map<String, RQueue> queueMap = new ConcurrentHashMap<>();

    public ReputationRedissonQueueService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Get a reference to a {@link RQueue} for the specified queue name
     *
     * @param queueName The queue name
     * @return {@link RQueue} instance for the queue name
     */
    public <T> RQueue<T> getQueue(String queueName) {
        //noinspection unchecked
        return (RQueue<T>) queueMap.computeIfAbsent(queueName, (qName) -> redissonClient.getQueue(qName, codec));
    }

    /**
     * Push a message onto the queue
     *
     * @param queueName The queue name
     * @param message   The message to push
     */
    public <T> void pushMessage(String queueName, T message) {
        getQueue(queueName).add(message);
    }

    /**
     * Pop a message off of the queue
     *
     * @param queueName The queue name
     */
    public <T> T popMessage(String queueName) {
        //noinspection unchecked
        return (T) getQueue(queueName).poll();
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }
}
