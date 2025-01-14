package org.study.redis.config.kafka.producer.runner;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import lombok.extern.slf4j.Slf4j;

//@SpringBootApplication
@Slf4j
public class SpringProducerApplication implements CommandLineRunner {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public SpringProducerApplication(
            @Qualifier("customKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringProducerApplication.class);
        app.run(args);
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {

        /*
         * 커스텀 카프카 템플릿을 주입받은 이후에 KafkaTemplate 인스턴스를 사용하는 것은 기본 카프카 템플릿을 사용하는 것과 동일하다.
         * send() 메서드를 사용하여 특정 토픽으로 데이터를 전송할 수 있다.
         * 만약 전송한 이후에 정상 적재됐는지 여부를 확인하고 싶다면 ListenableFuture 메서드를 사용하면 된다.
         *
         * 주의 : spring boot 2 -> spring boot 3 이 되면서 ListenerFuture 이 deprecated 되었다.
         *       ListenerFuture 대신 CompletableFuture 를 사용하면 된다.
         *
         * CompletableFuture : none blocking (async)
         *
         * */
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("TOPIC_NAME", "test");

        /*
         * 프로듀서가 보낸 데이터의 브로커 적재 여부를 비동기적으로 확인할 수 있다.
         * 브로커에 정상 적재 되었다면 exception 이 발생하지 않음으로 아래 if 문 로직이 실행된다.
         */
        future.whenComplete((result, ex) -> {

            // result : SendResult<String, String>

            if (ex == null) {
                log.info("Sent : {}, Offset : {}", result.getProducerRecord().value(),
                        result.getRecordMetadata().offset());
            } else {
                log.info("Failure : {}, due to : {}", result.getProducerRecord().value(),
                        ex.getMessage());
            }

        });

    }
}
