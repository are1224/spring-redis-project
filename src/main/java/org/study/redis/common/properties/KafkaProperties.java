package org.study.redis.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private String bootstrapServers;
    private Integer acks;
    private String keySerializer;
    private String valueSerializer;

}