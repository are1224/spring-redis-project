package org.study.redis.service.jpa.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.study.redis.domain.RequestUserDto;
import org.study.redis.domain.User;
import org.study.redis.domain.entity.UserEntity;
import org.study.redis.repository.UserRepository;
import org.study.redis.service.jpa.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * @param user
     * @return
     */
    @Transactional(transactionManager = "mysqlJpaTransactionManager", propagation = Propagation.REQUIRED)
    @Override
    public User findUser(User user) {

        UserEntity findUserEntity = userRepository.findUser(user);

        return new User(findUserEntity.getId(), findUserEntity.getPassword(), "USER");
    }

    /**
     *
     * @param userDto
     */
    @Transactional(transactionManager = "mysqlJpaTransactionManager", propagation = Propagation.REQUIRED)
    @Override
    public void registerUser(RequestUserDto userDto) {

        userRepository.registerUser(UserEntity.builder()
                .id(userDto.getUserId())
                .name(userDto.getUserName())
                .password(bCryptPasswordEncoder.encode(userDto.getPassword()))
                .build());
    }

    /**
     * @param userId
     * @return
     */
    @Transactional(transactionManager = "mysqlJpaTransactionManager", propagation = Propagation.REQUIRED)
    @Override
    public User findUserByUserId(String userId) {

        UserEntity findUserEntity = userRepository.findUser(new User(userId, null));

        return new User(findUserEntity.getId(), findUserEntity.getPassword(), "USER");
    }
}
