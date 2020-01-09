package org.narrative.config.cache;

import org.narrative.shared.redisson.config.RedissonClientConfigProperties;
import org.narrative.shared.redisson.management.RedissonObjectManager;
import org.narrative.shared.redisson.management.RedissonObjectManagerImpl;
import org.narrative.shared.redisson.management.RedissonRMapCacheManager;
import org.narrative.shared.reputation.config.redisson.ReputationRedissonQueueService;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.metrics.MeterRegistryProvider;
import org.redisson.config.metrics.PrometheusMeterRegistryProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Date: 8/11/18
 * Time: 11:16 AM
 *
 * @author brian
 */
@Configuration
public class RedissonConfig {
    public static final String REDISSON_CLIENT_CONFIGPROPS_BEAN_NAME = "redissonClientConfigProperties";
    public static final String REDISSON_CLIENT_BEAN_NAME = "redissonClient";

    /**
     * Redisson provider for Prometheus meter registry
     */
    @Bean
    public PrometheusMeterRegistryProvider redissonPrometheusMeterRegistryProvider() {
        return new PrometheusMeterRegistryProvider();
    }

    @Bean(name=REDISSON_CLIENT_CONFIGPROPS_BEAN_NAME)
    @ConfigurationProperties(prefix = "redisson.client")
    public RedissonClientConfigProperties redissonClientConfigProperties(){
        RedissonClientConfigProperties redissonClientConfigProperties = new RedissonClientConfigProperties();
        return redissonClientConfigProperties;
    }

    @Primary
    @Bean(name=REDISSON_CLIENT_BEAN_NAME, destroyMethod="shutdown")
    public RedissonClient redissonClient(@Qualifier(REDISSON_CLIENT_CONFIGPROPS_BEAN_NAME) RedissonClientConfigProperties redissonClientConfigProperties, MeterRegistryProvider meterRegistryProvider) {
        Config config = redissonClientConfigProperties.buildConfig();

        //Set up metrics collection
        config.setMeterRegistryProvider(meterRegistryProvider);

        return Redisson.create(redissonClientConfigProperties.buildConfig());
    }

    @Bean
    public ReputationRedissonQueueService redissonQueueService(@Qualifier(REDISSON_CLIENT_BEAN_NAME) RedissonClient redissonClient) {
        return new ReputationRedissonQueueService(redissonClient);
    }

    @Bean
    public RedissonRMapCacheManager redissonRMapCacheManager() {
        return new RedissonRMapCacheManager();
    }

    @Bean
    public RedissonObjectManager redissonObjectManager(@Qualifier(REDISSON_CLIENT_BEAN_NAME) RedissonClient redissonClient, RedissonRMapCacheManager redissonRMapCacheManager) {
        return new RedissonObjectManagerImpl(redissonClient, redissonRMapCacheManager);
    }
}
