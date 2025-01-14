package org.study.redis.config.kafka.consumer;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ListenerContainerConfiguration {

    /*
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> customContainerFactory() {


        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-kakfa:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);


        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(props);


        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();


        factory.getContainerProperties().setConsumerRebalanceListener(
                new ConsumerAwareRebalanceListener() {

                    @Override
                    public void onPartitionsRevokedBeforeCommit(Consumer<?, ?> consumer,
                            Collection<TopicPartition> partitions) {
                        ConsumerAwareRebalanceListener.super.onPartitionsRevokedBeforeCommit(consumer, partitions);
                    }

                    @Override
                    public void onPartitionsRevokedAfterCommit(Consumer<?, ?> consumer,
                            Collection<TopicPartition> partitions) {
                        ConsumerAwareRebalanceListener.super.onPartitionsRevokedAfterCommit(consumer, partitions);
                    }

                    @Override
                    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
                        ConsumerAwareRebalanceListener.super.onPartitionsAssigned(consumer, partitions);
                    }

                    @Override
                    public void onPartitionsLost(Collection<TopicPartition> partitions) {
                        ConsumerAwareRebalanceListener.super.onPartitionsLost(partitions);
                    }
                }
        );


        factory.setBatchListener(false);


        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        factory.setConsumerFactory(consumerFactory);

        return factory;

    }

    */

}
