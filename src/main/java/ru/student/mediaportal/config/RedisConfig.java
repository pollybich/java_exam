package ru.student.mediaportal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import ru.student.mediaportal.dto.ArticleResponse;
import ru.student.mediaportal.dto.PodcastResponse;
import ru.student.mediaportal.dto.VideoResponse;
import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper mapper) {
        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("articles", cache(mapper, ArticleResponse.class))
                .withCacheConfiguration("videos", cache(mapper, VideoResponse.class))
                .withCacheConfiguration("podcasts", cache(mapper, PodcastResponse.class))
                .transactionAware()
                .build();
    }

    private RedisCacheConfiguration cache(ObjectMapper mapper, Class<?> type) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new Jackson2JsonRedisSerializer<>(mapper, type)));
    }
}
