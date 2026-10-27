package org.study.redis.config.datasource;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.MySQLDialect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "org.study.redis.repository",
        entityManagerFactoryRef = "mysqlJpaEntityManagerFactory",
        transactionManagerRef = "mysqlJpaTransactionManager"
)
public class MysqlJpaDatasourceConfig {

    /**
     * @Method  : fbrJpaDataSource
     * @Date  :  2024.07.21
     * @description  : mysql db datasource
     * @return the data source
     */
    @Bean(name = "mysqlJpaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource mysqlJpaDataSource() {

        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * @Method  : mysqlJpaEntityManagerFactory
     * @Date  :  2024.07.21
     * @description  : EntityManagerFactory 생성 (mybatis 의 SesssionFactory 와 유사함)
     * @param builder
     * @param dataSource
     * @return the local container entity manager factory bean
     */
    @Bean(name = "mysqlJpaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean fbrJpaEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mysqlJpaDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factoryBean = builder
                .dataSource(dataSource)
                .packages("org.study.redis.domain.entity")
                .persistenceUnit("mysqljpa")
                .build();

        //방언 설정 (기본 방언이 sybase 관련 방언으로 설정되어 있으므로 Postgresql 방언으로는 아래와 같이 변경해줘야 함)
        factoryBean.setJpaProperties(getMysqlDialectProperties());

        return factoryBean;
    }

    /**
     * @Method  : getmysqlDialectProperties
     * @Date  :  2024.07.21
     * @description  : mysql db 관련 properties 설정
     * @return the data source
     */
    private Properties getMysqlDialectProperties() {
        Properties properties = new Properties();
        properties.setProperty(AvailableSettings.DIALECT, MySQLDialect.class.getName());
        return properties;
    }

    /**
     * @Method  : mysqlJpaTransactionManager
     * @Date  : 2024.07.21
     * @description  : transactionManager 생성
     * @param entityManagerFactory
     * @return the platform transaction manager
     */
    @Bean(name = "mysqlJpaTransactionManager")
    public PlatformTransactionManager writerTransactionManager(
            @Qualifier("mysqlJpaEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * @Method  : mysqlJpaQueryFactory
     * @Date  :  2024.06.10
     * @author  : jinseok0702@kyobobook.com
     * @description  :  QueyrDSL 사용시 필요한 JPAQueryFactory 생성
     * @param entityManager
     * @return the jpa query factory
     */
    @Bean(name = "mysqlJpaQueryFactory")
    public JPAQueryFactory mysqlJpaQueryFactory(
            @Qualifier("mysqlJpaEntityManagerFactory") EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

}
