package org.study.redis.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.study.redis.common.properties.RedisServerProperties;

@RequiredArgsConstructor
@Configuration
@EnableRedisRepositories // redis 를 사용한다고 명시해주는 annotation
public class RedisConfig {

    /* redis server properties */
    private final RedisServerProperties redisServerProperties;

    /*
    * redisServerProperties 로 application.yml 에 저장한 host, port 를 연결
    * */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        return new LettuceConnectionFactory(redisServerProperties.getHost(), redisServerProperties.getPort());

    }

    /*
    *
    * */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        return redisTemplate;

    }

}
