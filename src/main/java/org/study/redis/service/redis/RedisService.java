package org.study.redis.service.redis;

import java.time.Duration;
import java.util.Map;

import org.springframework.security.core.context.SecurityContext;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface RedisService {

    public String setValues(String sessionKey, SecurityContext securityContext) throws JsonProcessingException;

    public String setValues(String sessionKey, SecurityContext securityContext, Duration duration) throws
            JsonProcessingException;

    public <T> T getValues(String key, Class<T> resultClassType) throws JsonProcessingException;

    public void deleteValues(String key);

    public void expireValues(String key, int timeout);

    public void setHashOps(String key, Map<String, String> data);

    public String getHashOps(String key, String hashKey);

    public void deleteHashOps(String key, String hashKey);

    public boolean checkExistsValue(String value);

}
