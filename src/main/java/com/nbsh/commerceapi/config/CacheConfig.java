package com.nbsh.commerceapi.config;

import com.nbsh.commerceapi.common.cache.CacheNames;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory
    ) {

        RedisCacheConfiguration defaultConfiguration =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .disableCachingNullValues()
                        .entryTtl(
                                Duration.ofMinutes(10)
                        );

        RedisCacheConfiguration reviewSummaryConfiguration =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .disableCachingNullValues()
                        .entryTtl(
                                Duration.ofMinutes(10)
                        );

        return RedisCacheManager
                .builder(connectionFactory)
                .cacheDefaults(
                        defaultConfiguration
                )
                .withInitialCacheConfigurations(
                        Map.of(
                                CacheNames.REVIEW_SUMMARIES,
                                reviewSummaryConfiguration
                        )
                )
                .transactionAware()
                .build();
    }
}