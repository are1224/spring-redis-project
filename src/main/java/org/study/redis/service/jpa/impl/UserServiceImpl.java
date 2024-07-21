package org.study.redis.service.jpa.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.study.redis.domain.User;
import org.study.redis.domain.entity.UserEntity;
import org.study.redis.repository.UserRepository;
import org.study.redis.service.jpa.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param user
     * @return
     */
    @Transactional(transactionManager = "mysqlJpaTransactionManager", propagation = Propagation.REQUIRED)
    @Override
    public User findUser(User user) {

        UserEntity findUserEntity = userRepository.findUser(user);

        return User.builder()
                .userId(findUserEntity.getId())
                .password(findUserEntity.getPassword())
                .build();
    }
}
