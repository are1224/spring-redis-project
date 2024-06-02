package org.study.redis.service.redis;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

import java.time.Duration;

public interface RedisService {

    public String setValues(Object data) throws JsonProcessingException;

    public String setValues(Object data, Duration duration) throws JsonProcessingException;

    public <T> T getValues(String key, Class<T> resultClassType) throws JsonProcessingException;

    public void deleteValues(String key);

    public void expireValues(String key, int timeout);

    public void setHashOps(String key, Map<String, String> data);

    public String getHashOps(String key, String hashKey);

    public void deleteHashOps(String key, String hashKey);

    public boolean checkExistsValue(String value);

}
