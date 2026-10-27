package org.study.redis.config.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.study.redis.domain.kafka.message.KafkaDefaultMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TestConsumer {

    @KafkaListener(topics = "mytopic", groupId = "group_1")
    public void listen(KafkaDefaultMessage data) {

        log.info("kafka consumer listen - start");

        log.info("kafka consumer received: {}", data);

        log.info("message name : {}", data.getMessageName());

        log.info("message content : {}", data.getMessageContent());

        log.info("kafka consumer listen - end");

    }

}