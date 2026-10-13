package com.nbsh.commerceapi.config;

import com.nbsh.commerceapi.common.cache.CacheNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig
        implements CachingConfigurer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    CacheConfig.class
            );

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory
    ) {

        RedisCacheConfiguration configuration =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .disableCachingNullValues()
                        .entryTtl(
                                Duration.ofMinutes(10)
                        );

        return RedisCacheManager
                .builder(connectionFactory)
                .cacheDefaults(configuration)
                .withInitialCacheConfigurations(
                        Map.of(
                                CacheNames.REVIEW_SUMMARIES,
                                configuration
                        )
                )
                .transactionAware()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {

        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(
                    RuntimeException exception,
                    Cache cache,
                    Object key
            ) {

                log.warn(
                        "Cache GET failed; falling back to source. cache={}, key={}",
                        cache.getName(),
                        key,
                        exception
                );
            }

            @Override
            public void handleCachePutError(
                    RuntimeException exception,
                    Cache cache,
                    Object key,
                    Object value
            ) {

                log.warn(
                        "Cache PUT failed; continuing without cache. cache={}, key={}",
                        cache.getName(),
                        key,
                        exception
                );
            }

            @Override
            public void handleCacheEvictError(
                    RuntimeException exception,
                    Cache cache,
                    Object key
            ) {

                log.warn(
                        "Cache EVICT failed. cache={}, key={}",
                        cache.getName(),
                        key,
                        exception
                );
            }

            @Override
            public void handleCacheClearError(
                    RuntimeException exception,
                    Cache cache
            ) {

                log.warn(
                        "Cache CLEAR failed. cache={}",
                        cache.getName(),
                        exception
                );
            }
        };
    }
}