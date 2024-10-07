package org.study.redis.kafka.thread;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerWorker implements Runnable {

    private Properties prop;
    private String topic;
    private String threadName;

    /*
     * KafkaConsumer 클래스는 스레드 세이프하지 않다.
     * 이 때문에 스레드 별로 KafkaConsumer 인스턴스를 별개로 만들어서 운영해야만 한다.
     * 만약 KafkaConsumer 인스턴스를 여러 스레드에서 실행하면 ConcurrentModificationException 예외가 발생한다.
     * */
    private KafkaConsumer<String, String> consumer;

    /*
     * KafkaConsumer 인스턴스를 생성하기 위해 필요한 변수를 카프카 컨슈머 스레드 생성자 변수로 받는다.
     * 여기서는 KafkaConsumer 인스턴스에 들어갈 카프카 컨슈머 옵션을 담는 Properties 클래스와 토픽 이름, 스레드를 구별할 스레드 번호를 변수로 받았다.
     * */
    public ConsumerWorker(final Properties prop, final String topic, final int number) {
        this.prop = prop;
        this.topic = topic;
        this.threadName = "consumer-thread-" + number;
    }

    @Override
    public void run() {

        // KafkaConsumer 인스턴스 생성
        consumer = new KafkaConsumer<>(prop);

        // 생성자 변수로 받은 토픽을 명시적으로 구독하기 시작한다.
        consumer.subscribe(Collections.singletonList(topic));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

            for (ConsumerRecord<String, String> record : records) {
                // poll() 메서드를 통해 리턴받은 레코드들을 처리
                // 어떤 스레드가 어떤 레코드를 처리했는지 확인할 수 있다.
                log.info("{}", record);
            }

        }

    }

}
