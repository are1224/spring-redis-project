package org.study.redis.domain.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class KafkaDefaultMessage {
    private String messageName;
    private String messageContent;
}

