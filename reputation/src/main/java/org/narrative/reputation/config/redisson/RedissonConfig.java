package org.narrative.reputation.config.redisson;

import org.narrative.shared.redisson.config.RedissonClientConfigProperties;
import org.narrative.shared.reputation.config.redisson.ReputationRedissonQueueService;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RedissonConfig {
    public static final String REDISSON_CLIENT_BEAN_NAME = "redissonClient";

    @Bean
    @ConfigurationProperties(prefix = "redisson.client")
    public RedissonClientConfigProperties redissonClientConfigProperties(){
        return new RedissonClientConfigProperties();
    }

    @Primary
    @Bean(name = REDISSON_CLIENT_BEAN_NAME, destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedissonClientConfigProperties redissonClientConfigProperties) {
        return Redisson.create(redissonClientConfigProperties.buildConfig());
    }

    @Bean
    public ReputationRedissonQueueService redissonQueueService(@Qualifier(REDISSON_CLIENT_BEAN_NAME) RedissonClient redissonClient) {
        return new ReputationRedissonQueueService(redissonClient);
    }
}
