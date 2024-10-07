package org.study.redis.config.kafka.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.study.redis.common.properties.KafkaProperties;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaProducerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {

        Map<String, Object> props = new HashMap<>();

        try {
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Class.forName(kafkaProperties.getKeySerializer()));
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    Class.forName(kafkaProperties.getValueSerializer()));
        } catch (ClassNotFoundException classNotFoundException) {
            log.error(classNotFoundException.getMessage(), classNotFoundException);
        }

        return new DefaultKafkaProducerFactory<>(props);

    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
