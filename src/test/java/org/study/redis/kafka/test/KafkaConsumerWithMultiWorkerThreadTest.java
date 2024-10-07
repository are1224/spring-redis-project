package org.study.redis.kafka.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.study.redis.RedisApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RedisApplication.class)
@ActiveProfiles("loc") // profile 설정
public class KafkaConsumerWithMultiWorkerThreadTest {

    // @Test
    // public void kafka_consumer_with_multi_worker_thread() {
    //
    //     KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);
    //     consumer.subscribe(Arrays.asList(TOPIC_NAME));
    //
    //     ExecutorService executorService = Executors.newCachedThreadPool();
    //
    //     while (true) {
    //
    //         ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
    //
    //         for (ConsumerRecord<String, String> record : records) {
    //             ConsumerWorker worker = new ConsumerWorker(record.value());
    //             executorService.execute(worker);
    //         }
    //
    //     }
    //
    // }

}
