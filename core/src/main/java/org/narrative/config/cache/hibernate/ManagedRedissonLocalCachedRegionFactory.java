package org.narrative.config.cache.hibernate;

import org.narrative.config.StaticConfig;
import org.narrative.config.cache.RedissonConfig;
import org.narrative.config.properties.NarrativeProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cfg.Settings;
import org.narrative.shared.redisson.management.RedissonRMapCacheManager;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.hibernate.RedissonLocalCachedRegionFactory;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.Resource;

import java.util.Properties;

/**
 * A Redisson region factory that wraps Spring managed beans in lieu of the statically
 * initialized {@link RedissonLocalCachedRegionFactory}
 */
@SuppressWarnings("serial")
@Slf4j
public class ManagedRedissonLocalCachedRegionFactory extends RedissonLocalCachedRegionFactory {
    private static final String CONFIG_DEFAULTS_RESOURCE_NAME = "classpath:cacheManager-redissonhibernate-defaults.yml";
    private RedissonRMapCacheManager cacheManager;

    /**
     * Override so we can set our shared Redisson client as the client to use for Hibernate caching
     */
    @Override
    public void start(SessionFactoryOptions settings, Properties properties) throws CacheException {
        this.redisson = StaticConfig.getBean(RedissonConfig.REDISSON_CLIENT_BEAN_NAME, Redisson.class);

        try {
            FieldUtils.writeField(this, "settings", new Settings(settings), true);
        } catch (IllegalAccessException e) {
            throw new CacheException("Unable to set settings value on " + this.getClass().getSimpleName(), e);
        }

        cacheManager = StaticConfig.getBean(RedissonRMapCacheManager.class);
    }

    /**
     * Use our custom yaml files here so we can keep all of our cache settings out of hibernate.cfg.xml
     *
     * Load the consolidated cache configuration properties.  Properties in #configResourcePath override
     * #CONFIG_DEFAULTS_RESOURCE_NAME
     */
    public static Properties loadCacheProperties() {
        NarrativeProperties narrativeProperties = StaticConfig.getBean(NarrativeProperties.class);
        String configResourcePath = narrativeProperties.getHibernateCacheConfig().getRedissonHibernateCacheConfigResourcePath();

        //Load the default config from the classpath
        Resource defaultConfigFileRes =  StaticConfig.getApplicationContextResource(CONFIG_DEFAULTS_RESOURCE_NAME);
        //Load the config from the classpath or a specified path
        Resource configFileRes = StaticConfig.getApplicationContextResource(configResourcePath);

        final YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResolutionMethod(YamlProcessor.ResolutionMethod.OVERRIDE);
        factory.setResources(defaultConfigFileRes, configFileRes);
        factory.setSingleton(true);
        factory.afterPropertiesSet();
        Properties props = factory.getObject();

        //Flatten all values to Strings - loading from YAML creates objects for primitive types
        Properties resProps = new Properties();
        if (props != null) {
            for (Object propName: props.keySet()) {
                resProps.setProperty((String) propName, props.get(propName).toString());
            }
        }
        return resProps;
    }

    /**
     * Keep track of Local cache objects so we can manage through {@link org.narrative.shared.redisson.management.RedissonObjectManager}
     * :/
     */
    @Override
    protected RMapCache<Object, Object> getCache(String regionName, Properties properties, String defaultKey) {
        RMapCache<Object, Object> res = super.getCache(regionName, properties, defaultKey);
        cacheManager.addCache(regionName, res);
        return res;
    }
}
