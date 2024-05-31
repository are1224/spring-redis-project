package org.study.redis.service.redis.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.asm.TypeReference;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.study.redis.service.redis.RedisService;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;


    /**
     * key와 data를 Redis에 저장
     *
     * @param key
     * @param data
     */
    @Override
    public void setValues(String key, Object data) throws JsonProcessingException {

        System.out.println("redis input - 시작");

        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, objectMapper.writeValueAsString(data));

        System.out.println("redis input - 종료");

    }

    /**
     * key, value 저장과 동시에 유효 기간 설정
     *
     * @param key
     * @param data
     * @param duration
     */
    @Override
    public void setValues(String key, Object data, Duration duration) throws JsonProcessingException {

        System.out.println("redis input - 시작");

        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, objectMapper.writeValueAsString(data), duration);

        System.out.println("redis input - 종료");
        
    }

    /**
     * key 를 이용해서 rdis 로 부터 값을 get
     *
     * @param key
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
