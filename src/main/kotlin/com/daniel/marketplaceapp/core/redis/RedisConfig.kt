package com.daniel.marketplaceapp.core.redis

import java.time.Duration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.cache.RedisCacheManager

@Configuration
@EnableCaching
class RedisConfig {
    @Bean
    fun redisCacheManager(factory: RedisConnectionFactory) =
        RedisCacheManager.builder(factory)
            .withCacheConfiguration(
                "userDetails",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
            ).build()
}
