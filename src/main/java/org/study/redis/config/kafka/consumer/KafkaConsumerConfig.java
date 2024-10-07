package org.study.redis.config.kafka.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.study.redis.common.properties.KafkaProperties;
import org.study.redis.domain.kafka.message.KafkaDefaultMessage;

@Configuration
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, KafkaDefaultMessage> kafkaConsumer() {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group_1");

        // 패키지 신뢰 문제로 아래와 같이 직접 설정
        ErrorHandlingDeserializer errorKeyHandlingDeserializer = new ErrorHandlingDeserializer(
                new StringDeserializer());

        JsonDeserializer<KafkaDefaultMessage> jsonDeserializer = new JsonDeserializer<>(KafkaDefaultMessage.class);
        jsonDeserializer.setRemoveTypeHeaders(false);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeMapperForKey(true);

        ErrorHandlingDeserializer errorValueHandlingDeserializer = new ErrorHandlingDeserializer(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(props, errorKeyHandlingDeserializer, errorValueHandlingDeserializer);

    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaDefaultMessage> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, KafkaDefaultMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumer());

        return factory;

    }

}
