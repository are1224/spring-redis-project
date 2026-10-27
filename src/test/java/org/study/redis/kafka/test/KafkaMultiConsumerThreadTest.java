package org.study.redis.kafka.test;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.study.redis.RedisApplication;
import org.study.redis.kafka.thread.ConsumerWorker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RedisApplication.class)
@ActiveProfiles("loc") // profile 설정
public class KafkaMultiConsumerThreadTest {

    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private final static String GROUP_ID = "my-group";

    // 스레드 갯수를 지정하기 위해, 생성할 스레드 개수 정의
    private final static int CONSUMER_COUNT = 3;

    @Test
    public void Kafka_multi_consumer_thread_test() {

        // KafkaConsumer 인스턴스를 생성하기 위한 컨슈머 옵션들을 Properties 로 지정한다.
        Properties configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        /*
         * 컨슈머 스레드를 생성하기 위해 newCachedThreadPool 을 사용했다.
         * newCachedThreadPool 은 내부 작업이 완료되면 스레드를 종료하므로 데이터 처리가 끝난 컨슈머 스레드를 종료할 때 사용하면 편리하다.
         * */
        ExecutorService executorService = Executors.newCachedThreadPool();

        // 3개의 컨슈머 스레드를 execute() 메소드를 통해 실행한다.
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            ConsumerWorker worker = new ConsumerWorker(configs, TOPIC_NAME, i);
            executorService.execute(worker);
        }

    }

}
