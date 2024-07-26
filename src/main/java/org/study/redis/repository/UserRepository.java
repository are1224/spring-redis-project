package org.study.redis.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.study.redis.domain.User;
import org.study.redis.domain.entity.UserEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class UserRepository {

    @PersistenceContext(unitName = "mysqljpa")
    EntityManager entityManager;

    private final JPAQueryFactory jpaQueryFactory;

    public UserRepository(@Qualifier("mysqlJpaQueryFactory") JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public UserEntity findUser(User user) {
        return entityManager.find(UserEntity.class, user.getUsername());
    }

    public void registerUser(UserEntity userEntity) {
        entityManager.persist(userEntity);
    }
}
