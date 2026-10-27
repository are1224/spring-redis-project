package org.study.redis.config.kafka.consumer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class ListenerContainerConfiguration {

    /*
     * KafkaListenerContainerFactory 빈 객체를 리턴하는 메서드를 생성한다.
     * 이 메서드 이름은 커스텀 리스너 컨테이너 팩토리로 선언할 때 사용된다.
     * */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> customContainerFactory() {

        /*
         * 카프카 컨슈머를 실행할 때 필요한 옵션 값들을 선언한다.
         * group.id 는 리스너 컨테이너에도 선언하므로 지금 바로 선언하지 않아도 된다.
         * */
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-kakfa:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        /*
         * 컨슈머 옵션 값을 파라미터로 받는 DefaultKafkaConsumerFactory 인스턴스를 생성한다.
         * DefaultKafkaConsumerFactory 는 리스너 컨테이너 팩토리를 생성할 때 컨슈머 기본 옵션을 설정하는 용도로 사용된다.
         * */
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(props);

        /*
         * ConcurrentKafkaListenerContainerFactory 는 리스너 컨테이너를 만들기 위해 사용된다.
         * 이름에서 알 수 있다시피 2개 이상의 컨슈머 리스너를 만들 때 사용되며 concurrency 를 1로 설정할 경우 1개 컨슈머 스레드로 실행된다.
         * */
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

        /*
         * 리밸런스 리스너를 선언하기 위해 setConsumerRebalanceListener 메서드를 호출한다.
         * setConsumerRebalanceListener 는 스프링 카프카에서 제공하는 메서드로 기존에 사용되는 카프카 컨슈머 리밸런스 리스너에 2개의 메서드를 호출한다.
         * onPartitionsRevokedBeforeCommit 는 커밋이 되기 전에 리밸런스가 발생했을 때, onPartitionsRevokedAfterCommit 는 커밋이 일어난 이후에 리밸런스가 발생했을 때 호출된다.
         * */
        factory.getContainerProperties().setConsumerRebalanceListener(
                new ConsumerAwareRebalanceListener() {

                    /**
                     *
                     * @param consumer
                     * @param partitions
                     */
                    @Override
                    public void onPartitionsRevokedBeforeCommit(Consumer<?, ?> consumer,
                            Collection<TopicPartition> partitions) {
                        ConsumerAwareRebalanceListener.super.onPartitionsRevokedBeforeCommit(consumer, partitions);
                    }

                    /**
                     *
                     * @param consumer
                     * @param partitions
                     */
                    @Override
                    public void onPartitionsRevokedAfterCommit(Consumer<?, ?> consumer,
                            Collection<TopicPartition> partitions) {
                        ConsumerAwareRebalanceListener.super.onPartitionsRevokedAfterCommit(consumer, partitions);
                    }

                    /**
                     *
                     * @param consumer
                     * @param partitions
                     */
                    @Override
                    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
                        ConsumerAwareRebalanceListener.super.onPartitionsAssigned(consumer, partitions);
                    }

                    /**
                     *
                     * @param partitions
                     */
                    @Override
                    public void onPartitionsLost(Collection<TopicPartition> partitions) {
                        ConsumerAwareRebalanceListener.super.onPartitionsLost(partitions);
                    }
                }
        );

        /*
         * 레코드 리스너를 사용함을 명시하기 위해 setBatchListener() 메서드에 false 를 파라미터로 넣는다.
         * 만약 배치 리스너를 사용하고 싶다면 true 로 설정하면 된다.
         * */
        factory.setBatchListener(false);

        /*
         * AckMode 를 설정한다.
         * 여기서는 레코드 단위로 커밋하기 위해 AckMode 를 RECORD 로 설정했다.
         * */
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        
        /*
         * 컨슈머 설정 값을 가지고 있는 DefaultKafkaConsumerFactory 인스턴스를 ConcurrentKafkaListenerContainerFactory 의 컨슈머 팩토리에 설정한다.
         * */
        factory.setConsumerFactory(consumerFactory);

        return factory;

    }

}
