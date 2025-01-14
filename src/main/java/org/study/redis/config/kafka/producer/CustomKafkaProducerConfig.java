package org.study.redis.config.kafka.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class CustomKafkaProducerConfig {

    //@Bean
    public KafkaTemplate<String, String> customKafkaTemplate() {

        /*
         * ProducerFactory 를 사용하여 KafkaTemplate 객체를 만들 때는 프로듀서 옵션을 직접 넣는다.
         * 이때는 기본 카프카 템플릿을 사용하는 것과 다르게 직접 프로듀서 옵션 들을 선언한다.
         * 프로듀서 설정에 필요한 필수 옵션 (bootstrap.servers, key.serializer, value.serializer) 을 포함한 옵션들은 선언한다.
         * */
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // KafkaTemplate 객체를 만들기 위한 ProducerFactory 를 초기화
        ProducerFactory<String, String> factory = new DefaultKafkaProducerFactory<>(props);

        /*
         * 빈 객체로 사용할 KafkaTemplate 인스턴스를 초기화하고 리턴한다.
         * 스프링 카프카에서는 KafkaTemplate 외에도 ReplyingKafkaTemplate 과 RoutingKafkaTemplate 도 제공한다.
         * ReplyingKafkaTemplate 는 컨슈머가 특정 데이터를 전달받았는지 여부를 확인할 수 있다.
         * RoutingKafkaTemplate 은 전송하는 토픽별로 옵션을 다르게 설정할 수 있다.
         * ReplyingKafkaTemplate 또는 RoutingKafkaTemplate 을 사용하고 싶다면 여기서 인스턴스를 리턴하여 빈 객체로 등록할 수 있다.
         * */
        return new KafkaTemplate<>(factory);

    }

}
