package com.moneytransfer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Redis caching.
 * This configuration is active for all profiles except "test".
 * It defines the cache manager and cache configurations for the application.
 */
@Configuration
@Profile("!test")
public class CacheConfig {
    private static final String[] cacheNames = {"moneyTransferRequestsCache", "exchangeRatesCache"};

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = getCacheConfigurations();
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
                .withInitialCacheConfigurations(cacheConfigurations).build();
    }

    private Map<String, RedisCacheConfiguration> getCacheConfigurations() {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(cacheNames[0], RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(1)));
        cacheConfigurations.put(cacheNames[1], RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(10)));
        return cacheConfigurations;
    }
}