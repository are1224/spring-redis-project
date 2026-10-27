package org.study.redis.config.kafka.producer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class DefaultKafkaProducerConfig implements CommandLineRunner {

    /*
     * KafkaTemplate 을 주입받아서 사용한다.
     * 사용자가 직접 선언하지 않은 bean 객체지만 스프링 카프카에서 제공하는 기본 KafkaTemplate 객체로 주입된다.
     * application.yml 에 선언한 옵션 값은 자동으로 주입된다.
     * */
    private final KafkaTemplate<String, String> kafkaTemplate;

    public DefaultKafkaProducerConfig(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DefaultKafkaProducerConfig.class);
        app.run(args);
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {

        for (int i = 0; i < 10; i++) {

            /*
             * send() 메서드를 사용해서 토픽 이름과 메시지 값을 넣어 전송한다.
             * 카프카 프로듀서의 send() 메서드와 유사한 것을 확인할 수 있다.
             */
            kafkaTemplate.send(TOPIC_NAME, "test " + i);
        }

        // 프로듀서로 데이터를 전송이 완료되면 종료
        System.exit(0);

    }
}
