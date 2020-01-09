package org.narrative.config.cache.spring;

import com.google.common.collect.Sets;
import org.narrative.config.properties.NarrativeProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.jooq.lambda.Unchecked;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.LocalCachedCacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.redisson.spring.cache.RedissonSpringLocalCachedCacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Spring {@link org.springframework.cache.CacheManager} configuration.
 */
@Slf4j
@Configuration
public class CacheManagerConfig {
    /**
     * Composite cache manager for Redisson regular caches and read/write through caches.
     */
    @Primary
    @Bean
    public CacheManager cacheManager(@Qualifier("redissonClient") RedissonClient redissonClient, NarrativeProperties narrativeProperties, ConfigurableListableBeanFactory beanFactory) {
        //Build the cache manager
        Map<String, CacheConfig> cacheManagerConfig = buildRedissonCacheManagerConfig(narrativeProperties.getSpring().getCacheManager().getConfigFilePath());
        RedissonSpringCacheManager cacheManager = new RedissonSpringCacheManager(redissonClient, cacheManagerConfig, redissonClient.getConfig().getCodec());
        // set the cacheNames so that caches are not created dynamically on-the-fly. the dynamic behavior
        // actually prevents the proper cache configs from being used for remote caches due to implementation
        // details of how CompositeCacheManager works.
        cacheManager.setCacheNames(cacheManagerConfig.keySet());

        //Build the local cache manager
        Map<String, LocalCachedCacheConfig> localCacheManagerConfig = buildRedissonLocalCacheManagerConfig(narrativeProperties.getSpring().getCacheManager().getLocalCacheConfigFilePath());
        RedissonSpringCacheManager localCacheManager = new RedissonSpringLocalCachedCacheManager(redissonClient, localCacheManagerConfig, redissonClient.getConfig().getCodec());
        // set the cacheNames so that dynamic creation of caches is disabled, just like we do with local caches above.
        localCacheManager.setCacheNames(localCacheManagerConfig.keySet());

        Set<String> configuredCacheNameSet = new HashSet<>(cacheManagerConfig.keySet());
        Set<String> configuredLocalCacheNameSet = new HashSet<>(localCacheManagerConfig.keySet());

        //Validate that cache names and local cache names are disjoint
        validateCacheNameSetsAreDisjoint(configuredCacheNameSet, configuredLocalCacheNameSet);

        //Validate that all {@link Cacheable} methods have a cache configuration
        Set<String> allCacheNames = new HashSet<>(configuredCacheNameSet);
        allCacheNames.addAll(configuredLocalCacheNameSet);
        validateCacheConfigAgainstAnnotatedMethods(allCacheNames, beanFactory);

        return new CompositeCacheManager(localCacheManager, cacheManager);
    }

    /**
     * Redisson cache manager configuration builder
     */
    private Map<String, CacheConfig> buildRedissonCacheManagerConfig(String primaryConfigPath) {
        //Build the default configuration first
        Map<String, CacheConfig> resultConfig = CacheManagerDefaultConfig.Config.buildCacheConfigMap();

        //Load the primary cache configuration
        @SuppressWarnings("unchecked")
        Map<String, CacheConfig> primaryConfig = loadPrimaryCacheConfig(primaryConfigPath, "cache", Unchecked.function((File file) -> (Map<String, CacheConfig>) CacheConfig.fromYAML(file)));

        //Merge the configs if necessary
        if (MapUtils.isNotEmpty(primaryConfig)) {
            resultConfig.putAll(primaryConfig);
        }

        return resultConfig;
    }

    /**
     * Redisson ###local cache### cache manager configuration builder - caches that are configurable to be read and/or write through.
     */
    private Map<String, LocalCachedCacheConfig> buildRedissonLocalCacheManagerConfig(String primaryConfigPath) {
        //Build the default configuration first
        Map<String, LocalCachedCacheConfig> resultConfig = CacheManagerDefaultConfig.Config.buildLocalCacheConfigMap();

        //Load the primary cache configuration
        Map<String, LocalCachedCacheConfig> primaryConfig = loadPrimaryCacheConfig(primaryConfigPath, "local cache", Unchecked.function(LocalCachedCacheConfig::fromYAML));

        //Merge the configs if necessary
        if (MapUtils.isNotEmpty(primaryConfig)) {
            resultConfig.putAll(primaryConfig);
        }

        return resultConfig;
    }

    /**
     * Make sure there aren't overlapping cache names in the local and regular cache manager
     */
    private void validateCacheNameSetsAreDisjoint(Set<String> cacheNameSet, Set<String> localCacheNameSet) {
        if (!Collections.disjoint(cacheNameSet, localCacheNameSet)) {
            Set<String> intersectingSet = Sets.intersection(cacheNameSet, localCacheNameSet);
            throw new IllegalStateException("The set of regular cache names and local cache names are not disjoint!  This represents a configuration error: cache names that overlap: " + StringUtils.join(intersectingSet, ','));
        }
    }

    /**
     * Validate that all {@link Cacheable} annotated methods have a configured cache - if not throw an exception to prevent application start up
     */
    @SneakyThrows
    private void validateCacheConfigAgainstAnnotatedMethods(Set<String> configuredCacheNameSet, ConfigurableListableBeanFactory beanFactory) {
        Set<String> unconfiguredNameSet = Sets.difference(findCacheableCacheNames(beanFactory), configuredCacheNameSet);
        if (unconfiguredNameSet.size() > 0) {
            throw new IllegalStateException("There are @Cacheable methods configured that don't have corresponding cache definitions for the following cache names: " + StringUtils.join(unconfiguredNameSet, ','));
        }
    }

    /**
     * Find cache names for all methods annotated with {@link Cacheable}
     */
    @SneakyThrows
    private Set<String> findCacheableCacheNames(ConfigurableListableBeanFactory beanFactory) {
        Set<String> cacheNameSet = new HashSet<>();
        int unknownIdx = 1;

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);

            if (StringUtils.isNotEmpty(beanDef.getBeanClassName())) {
                Class<?> beanClass = Class.forName(beanDef.getBeanClassName());

                for (Method m : beanClass.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(Cacheable.class)) {
                        Cacheable cacheable = m.getAnnotation(Cacheable.class);
                        String[] cacheNames = cacheable.cacheNames();
                        if (cacheNames.length == 0) {
                            cacheNameSet.add("UNKNOWN_CACHE_NAME_" + unknownIdx++);
                        } else {
                            cacheNameSet.addAll(Arrays.asList(cacheable.cacheNames()));
                        }
                    }
                }
            }
        }
        return cacheNameSet;
    }

    /**
     * Load the "primary" cache configuration if specified.  Any cache configuration specified in this file will override the default cache configuration.
     */
    @SneakyThrows
    private <T  extends CacheConfig> Map<String, T> loadPrimaryCacheConfig(String filePath, String cacheTypeDesc, Function<File, Map<String, T>> loadFn) {
        Map<String, T> res = null;
        if (StringUtils.isNotEmpty(filePath)) {
            try {
                File file = new File(filePath);
                res = loadFn.apply(file);
                log.info("Using primary " + cacheTypeDesc + " configuration found at " + filePath);
            } catch (Exception e) {
                log.error("Unable to load primary " + cacheTypeDesc + " configuration from " + filePath, e);
                throw e;
            }
        } else {
            log.info("No primary " + cacheTypeDesc + " config file specified - falling back to defaults");
        }

        return res;
    }
}
