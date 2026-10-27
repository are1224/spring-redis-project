package org.study.redis.service.jpa;

import org.study.redis.domain.RequestUserDto;
import org.study.redis.domain.User;

public interface UserService {

    public User findUser(User user);

    void registerUser(RequestUserDto userDto);

    User findUserByUserId(String userId);
}
