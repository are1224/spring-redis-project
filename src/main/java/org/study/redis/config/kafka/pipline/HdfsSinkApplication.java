package org.study.redis.config.kafka.pipline;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.study.redis.config.kafka.pipline.consumer.ConsumerWorker;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HdfsSinkApplication implements ApplicationRunner {

    /*
     * 연동할 카프카 클러스터 정보, 토픽 이름, 컨슈머 그룹을 설정한다.
     * */
    private final static String BOOTSTRAP_SERVERS = "192.168.70.128:9094";
    private final static String TOPIC_NAME = "select-color";
    private final static String GROUP_ID = "color-hdfs-save-consumer-group";

    /*
     * 생성할 스레드 개수를 변수로 선언
     * 추후 파티션이 늘어날 경우 변수를 변경하고 재배포, 실행함으로써 파티션과 동일 개수의 컨슈머 스레드를 동적으로 운영할 수 있다.
     * */
    private final static int CONSUMER_COUNT = 3;

    private final static List<ConsumerWorker> workers = new ArrayList<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        main(null);
    }

    public static void main(String[] args) {

        System.setProperty("HADOOP_USER_NAME", "hadoop");
        //System.setProperty("hadoop.home.dir", "/opt/hadoop");

        /*
         * 안전한 컨슈머의 종료를 위해 셧다운 훅을 선언
         * 셧다운 훅이 발생했을 경우 각 컨슈머 스레드에 종료를 알리도록 명시적으로 stopAndWakeup() 메서드를 호출한다.
         */
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());

        /*
         * 컨슈머 설정
         * 메시지 키, 메시지 값은 JSON 포맷의 String 타입을 받으므로, String 타입의 역직렬화를 선언
         * */
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // 컨슈머 스레드를 스레드 풀로 관리
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            // 컨슈머 스레드 인스턴스들을 workers 에서 관리
            workers.add(new ConsumerWorker(props, TOPIC_NAME, i));
        }

        // execute 메서드로 컨슈머 스레드 인스턴스들을 스레드 풀에 포함시켜 실행
        workers.forEach(executorService::execute);

    }

    static class ShutdownThread extends Thread {

        @Override
        public void run() {
            log.info("Shutdown hook...");
            workers.forEach(ConsumerWorker::stopAndWakeup);
        }
    }

}
