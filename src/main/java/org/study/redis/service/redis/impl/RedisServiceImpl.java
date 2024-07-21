package org.study.redis.service.redis.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.study.redis.domain.User;
import org.study.redis.service.redis.RedisService;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    private final String SESSION_KEY_PREFIX = "spring:session:sessions:";
    private final long SESSION_EXPIRED_TIME = 180000;


    /**
     * key와 data를 Redis에 저장
     *
     * @param sessionKey
     */
    @Override
    public String setValues(String sessionKey, User user) throws JsonProcessingException {

        log.info("redis input - 시작");

        StringBuilder sb = new StringBuilder();
        sb.append(SESSION_KEY_PREFIX);
        sb.append(Base64.getEncoder().encodeToString(sessionKey.getBytes()));

        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(sb.toString(), objectMapper.writeValueAsString(user), Duration.of(SESSION_EXPIRED_TIME, TimeUnit.MILLISECONDS.toChronoUnit()));

        log.info("redis input - 종료");

        return sb.toString();

    }

    /**
     * key, value 저장과 동시에 유효 기간 설정
     *
     * @param sessionKey
     * @param user
     * @param duration
     */
    @Override
    public String setValues(String sessionKey, User user, Duration duration) throws JsonProcessingException {

        log.info("redis input - 시작");

        StringBuilder sb = new StringBuilder();
        sb.append(SESSION_KEY_PREFIX);
        sb.append(sessionKey);

        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(sb.toString(), objectMapper.writeValueAsString(user), duration);

        log.info("redis input - 종료");

        return sb.toString();
        
    }

    /**
     * key 를 이용해서 redis 로 부터 값을 get
     *
     * @param key
     * @param resultClassType
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public <T> T getValues(String key, Class<T> resultClassType) throws JsonProcessingException {

        ValueOperations<String, Object> values = redisTemplate.opsForValue();

        if (values.get(key) == null) {
            return null;
        }

        String valueOfString = (String) values.get(key);

        return objectMapper.readValue(valueOfString, resultClassType);

    }

    /**
     * @param key
     */
    @Override
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    /**
     * @param key
     * @param timeout
     */
    @Override
    public void expireValues(String key, int timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * @param key
     * @param data
     */
    @Override
    public void setHashOps(String key, Map<String, String> data) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.putAll(key, data);
    }

    /**
     * @param key
     * @param hashKey
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public String getHashOps(String key, String hashKey) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        return Boolean.TRUE.equals(values.hasKey(key, hashKey)) ? (String) redisTemplate.opsForHash().get(key, hashKey) : "";
    }

    /**
     * @param key
     * @param hashKey
     */
    @Override
    public void deleteHashOps(String key, String hashKey) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.delete(key, hashKey);
    }

    /**
     * @param value
     * @return
     */
    @Override
    public boolean checkExistsValue(String value) {
        return !value.equals("false");
    }
}
