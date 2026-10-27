package org.study.redis.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.study.redis.common.properties.RedisServerProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableRedisRepositories // redis 를 사용한다고 명시해주는 annotation
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 600)
// web application 에 대한 session 저장소로 redis 를 사용하게 설정 / maxInactiveIntervalInSeconds 을 통해 session time out 설정 가능 (10분 으로 설정)
public class RedisConfig {

    /* redis server properties */
    private final RedisServerProperties redisServerProperties;

    // 쿠키내 세션 관련 attribute 명 변경
    private static final String SESSION_NAME = "SESSIONS";

    /*
     * redisServerProperties 로 application.yml 에 저장한 host, port 를 연결
     * RedisConnectionFactory : Spring Data Redis 에서 제공하는 인터페이스, Redis 커넥션을 생성하고 관리
     * */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        /*
         *   spring-boot-starter-data-redis 라이브러리 내에 저장되어 있으며 Java 환경에서 Redis 를 이용하기 위한 Redis 클라이언트다.
         *   Redis 명령어를 비동기적으로 실행 가능 (Redis  서버와 비 동기적 통신을 수행하는데 사용되는 인터페이스)
         * */
        return new LettuceConnectionFactory(redisServerProperties.getHost(), redisServerProperties.getPort());

    }

    /*
     *   RedisTemplate : Redis 데이터 베이스에 접근하고 데이터를 처리하는데 사용된다. (Spring Data Redis 에서 제공하는 클래스)
     *   타입 안정성을 제공하고 직렬화 및 역직렬화를 처리하며 Redis 의 기본적인 데이터 구조를 지원한다.
     * */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        /* Redis 를 연결 */
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        /* Key-Value 형태롤 직렬화를 수행 : 이외의 직렬화가 늘어나면 setDefaultSerializer 를 통해서 기본적인 직렬화를 설정해줘야함 */
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        /* hash 관련 */
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        redisTemplate.afterPropertiesSet();

        return redisTemplate;

    }

    @Bean
    public DefaultCookieSerializer getDefaultCookieSerializer() {

        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();

        cookieSerializer.setDomainName("localhost");
        cookieSerializer.setCookieName(SESSION_NAME);
        cookieSerializer.setCookiePath("/");
        cookieSerializer.setUseBase64Encoding(false);
        cookieSerializer.setSameSite(null);

        return cookieSerializer;

    }

}
