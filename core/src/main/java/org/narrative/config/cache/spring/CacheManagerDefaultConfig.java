package org.narrative.config.cache.spring;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.LocalCachedCacheConfig;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.narrative.config.cache.spring.CacheConfigFactory.*;
import static org.narrative.config.cache.spring.CacheManagerDefaultConfig.Config.*;

/**
 * Default cache configuration for Spring/Redisson cache manager.
 * The main method in this class serves as a utility to dump the default config to yml files to enable easy
 * overrides of default configuration.
 */
public class CacheManagerDefaultConfig {
    private static final String CACHE_NAME_PREFIX = "springCache.";

    /**
     * Since we need to share cache names between this config and to be able to use these same names in annotations,
     * create this stupid inner class that is associated with the configurations.  Enum name won't work for annotation
     * attributes since the names are not considered constant by the compiler.
     */
    public static class CacheName {
        public static final String CACHE_NICHESERVICE_SIMILAR_NICHES_BY_OID = CACHE_NAME_PREFIX + "NicheService#findSimilarNicheOids";
        public static final String CACHE_STATSSERVICE_STATS_OVERVIEW = CACHE_NAME_PREFIX + "StatsService#getStatsOverview";
        public static final String CACHE_REWARDSSERVICE_REWARD_PERIOD_STATS = CACHE_NAME_PREFIX + "RewardsService#getRewardPeriodStats";
        public static final String CACHE_STATSSERVICE_NICHE_STATS = CACHE_NAME_PREFIX + "StatsService#getNicheStats";
        public static final String CACHE_NICHESERVICE_TRENDING_NICHE_OIDS = CACHE_NAME_PREFIX + "NicheService#getTrendingNicheOids";
        public static final String CACHE_PUBLICATIONSERVICE_TOP_NICHES = CACHE_NAME_PREFIX + "PublicationService#getTopNiches";
        public static final String CACHE_CONTENTSTREAMSERVICE_FEATURED_CHANNEL_POSTS = CACHE_NAME_PREFIX + "ContentStreamService#getFeaturedChannelPostOids";
        public static final String CACHE_CONTENTSTREAMSERVICE_CHANNEL_WIDGET_POSTS = CACHE_NAME_PREFIX + "ContentStreamService#getChannelContentWidgetPostOids";
        public static final String CACHE_CONTENTSTREAMSERVICE_FEATURED_NETWORK_POSTS = CACHE_NAME_PREFIX + "NicheService#getFeaturedNetworkPostOids";
        public static final String CACHE_CANONDATASVC_COUNTRIES_LIST = CACHE_NAME_PREFIX + "CanonicalDataService#getCountriesList";
    }

    public enum Config {
        /**
         * Default cache configurations go here
         * Non-local caches pull/push directly from/to Redisson without any local read/write through caching.
         *
         * This type of cache would be appropriate for expensive operations that aren't fetched/updated often.
         */

        /**
         * Default *local* cache configurations go here
         * Local caches support local read/write through caching in-memory
         *
         * This type of cache is appropriate for high traffic operations in order to avoid repeated brokering
         * of cached objects and the associated network traffic
         */
        SIMILAR_NICHES_BY_OID(CacheName.CACHE_NICHESERVICE_SIMILAR_NICHES_BY_OID, () -> buildLocalCacheConfig(5, 0)),
        NETWORK_STATS_OVERVIEW(CacheName.CACHE_STATSSERVICE_STATS_OVERVIEW, () -> buildCacheConfig(5, 0, 1)),
        REWARD_PERIOD_STATS(CacheName.CACHE_REWARDSSERVICE_REWARD_PERIOD_STATS, () -> buildCacheConfig(60, 0, 12)),
        NICHE_STATS(CacheName.CACHE_STATSSERVICE_NICHE_STATS, () -> buildCacheConfig(5, 0, 1)),
        TRENDING_NICHE_OIDS(CacheName.CACHE_NICHESERVICE_TRENDING_NICHE_OIDS, () -> buildCacheConfig(5, 0, 5)),
        PUBLICATION_TOP_NICHES(CacheName.CACHE_PUBLICATIONSERVICE_TOP_NICHES, () -> buildCacheConfig(15, 0, 100)),
        FEATURED_CHANNEL_POSTS(CacheName.CACHE_CONTENTSTREAMSERVICE_FEATURED_CHANNEL_POSTS, () -> buildCacheConfig(10, 0, 1000)),
        CHANNEL_WIDGET_POSTS(CacheName.CACHE_CONTENTSTREAMSERVICE_CHANNEL_WIDGET_POSTS, () -> buildCacheConfig(10, 0, 1000)),
        FEATURED_NETWORK_POSTS(CacheName.CACHE_CONTENTSTREAMSERVICE_FEATURED_NETWORK_POSTS, () -> buildCacheConfig(10, 0, 10)),
        COUNTRIES_LIST(CacheName.CACHE_CANONDATASVC_COUNTRIES_LIST, () -> buildCacheConfig(0, 0, 1));

        /**
         * Need to capture the configuration type since it can not be inferred from the supplier
         */
        private final Class<? extends CacheConfig> configType;
        /**
         * Name of the cache (used by Redisson)
         */
        public final String cacheName;
        /**
         * Lazy configuration supplier
         */
        private final Supplier<? extends CacheConfig> configSupplier;

        Config(String cacheName, Supplier<? extends CacheConfig> configSupplier) {
            this.configType = configSupplier.get().getClass();
            this.cacheName = cacheName;
            this.configSupplier = configSupplier;
        }

        Config(Class<? extends CacheConfig> configType, String cacheName, Supplier<? extends CacheConfig> configSupplier) {
            this.configType = configType;
            this.cacheName = cacheName;
            this.configSupplier = configSupplier;
        }

        /**
         * Build a map of local cache configurations
         */
        public static Map<String, LocalCachedCacheConfig> buildLocalCacheConfigMap() {
            return Arrays.stream(values())
                    .filter(value -> LocalCachedCacheConfig.class.equals(value.configType))
                    .collect(Collectors.toMap(value -> value.cacheName, value -> (LocalCachedCacheConfig) value.configSupplier.get()));
        }

        /**
         * Build a map of cache configurations
         */
        public static Map<String, CacheConfig> buildCacheConfigMap() {
            return Arrays.stream(values())
                    .filter(value -> CacheConfig.class.equals(value.configType))
                    .collect(Collectors.toMap(value -> value.cacheName, value -> (CacheConfig) value.configSupplier.get()));
        }
    }

    /**
     * Generate yml files representing the default cache configuration
     *
     * @param args Arg 0 is the destination path for the generated files
     */
    public static void main(String[] args) {
        assert StringUtils.isNotEmpty(args[0]) : "You must pass a path as the first argument for file generation";

        String destPath = args[0];
        File destPathFile = new File(destPath);
        destPathFile.mkdirs();

        try {
            String header = IOUtils.toString(CacheManagerDefaultConfig.class.getResource("/cache/redissonHeader.txt"));
            String localHeader = IOUtils.toString(CacheManagerDefaultConfig.class.getResource("/cache/redissonLocalCacheHeader.txt"));

            String fileContent = header + org.redisson.spring.cache.CacheConfig.toYAML(buildCacheConfigMap());
            FileUtils.write(new File(destPathFile, "defaultCacheConfig.yml"), fileContent);
            fileContent = header + localHeader + org.redisson.spring.cache.CacheConfig.toYAML(buildLocalCacheConfigMap());
            FileUtils.write(new File(destPathFile, "defaultLocalCacheConfig.yml"), fileContent);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.exit(0);
    }
}


