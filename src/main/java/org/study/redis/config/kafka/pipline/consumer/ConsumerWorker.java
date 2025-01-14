package org.study.redis.config.kafka.pipline.consumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerWorker implements Runnable {

    /*
     * ConcurrentHashMap
     *
     * MultiThread 환경에서 사용되는 공용 (static) Map
     * 읽기 작업 대해서는 synchronized 가 붙지 않음
     * 쓰기 작업에 대해서는 메서드 내 일부 부분에 synchronized 블럭 처리되어 있음
     *
     * 컨슈머의 poll() 메서드를 통해 전달받은 데이터를 임시 저장하는 버퍼를 bufferString 으로 선언
     * static 변수로 선언하여 다수의 스레드가 만들어지더라도 동일 변수에 접근한다.
     * 다수 스레드가 동시 접근할 수 있는 변수이므로 멀티 스레드 환경에서 안전하게 사용할 수 있는 ConcurrentHsahMap<> 으로 구현했다.
     * bufferString 에는 파티션 번호와 메시지 값들이 들어간다.
     *
     * currentOffset 은 오프셋 값을 저장하고 파일 이름을 지정할 때 오프셋 번호를 붙이는 데에 사용된다.
     *
     * */
    private static Map<Integer, List<String>> bufferString = new ConcurrentHashMap<Integer, List<String>>();
    private static Map<Integer, Long> currentFileOffset = new ConcurrentHashMap<>();

    private final static int FLUSH_RECORD_COUNT = 3;
    private Properties props;
    private String topic;
    private String threadName;
    private KafkaConsumer<String, String> consumer;

    public ConsumerWorker(Properties props, String topic, int number) {

        log.info("Generate ConsumerWorker");

        this.props = props;
        this.topic = topic;
        this.threadName = "consumer-thread-" + number;
    }

    @Override
    public void run() {

        Thread.currentThread().setName(threadName);

        /*
         * 스레드를 생성하는 HdfsSinkApplication 에서 설정한 컨슈머 설정을 가져와서 KafkaConsumer 인스턴스를 생성하고 토픽을 구독한다.
         *
         * */
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));

        try {

            while (true) {

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                for (ConsumerRecord<String, String> record : records) {
                    /*
                     * poll() 메서드로 가져온 데이터 들을 버퍼에 쌓기 위해 addHdfsFileBuffer() 메서드 call
                     * */

                    log.info("kafka 에서 전달 받음");

                    addHdfsFileBuffer(record);
                }

                /*
                 * 한번 polling 이 완료되고 버퍼에 쌓이고 나면 saveBufferToHdfsFile() 을 호출하여 버퍼에 쌓인 데이터가 일정 개수 이상 쌓였을 경우 HDFS 에 저장하는 로직을 수행하도록 한다.
                 * 여기서 consumer.assignment() 값을 파라미터로 넘겨주는데, 현재 컨슈머 스레드에 할당된 파티션에 대한 버퍼 데이터만 적재할 수 있도록 하기 위함이다.
                 *
                 * */
                saveBufferToHdfsFile(consumer.assignment());

            }

        } catch (WakeupException wakeupException) {
            /*
             * 안전한 종료를 위해 WakeUpException 을 받아서 컨슈머 종료 과정을 수행
             * */
            log.warn("Wakeup Consumer");
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        } finally {
            consumer.close();
        }

    }

    /*
     * 레코드를 받아서 메시지 값을 버퍼에 넣는 코드
     *
     * 만약 버퍼 크기가 1이라면 버퍼의 가장 처음 오프셋이라는 뜻이므로 currentFileOffset 변수에 넣는다.
     * currentFileOffset 변수에서 오프셋 번호를 관리하면 추후 파일을 저장할 때 파티션 이름과 오프셋 번호를 붙여서 저장할 수 있기 때문에 이슈 발생 시 파티션과 오프셋에 대한 정보를 알 수 있다는 장점이 있다.
     * */
    private void addHdfsFileBuffer(ConsumerRecord<String, String> record) {

        List<String> buffer = bufferString.getOrDefault(record.partition(), new ArrayList<>());
        buffer.add(record.value());
        bufferString.put(record.partition(), buffer);

        /*
         * 각 파티션 별로 처음으로 구독한 경우에는 파티션 초기 offset 을 currentFileOffset 에 저장
         * */
        if (buffer.size() == 1) {
            currentFileOffset.put(record.partition(), record.offset());
        }

    }

    /*
     * 버퍼의 데이터가 flush 될 만큼 개수가 충족되었는지 확인하는 checkFlushCount 메서드를 호출한다.
     * 컨슈머로부터 Set<TopicPartition> 정보를 받아서 컨슈머 스레드에 할당된 파티션에만 접근한다.
     * */
    private void saveBufferToHdfsFile(Set<TopicPartition> partitions) {
        partitions.forEach(p -> checkFlushCount(p.partition()));
    }

    /*
     * 파티션 번호의 버퍼를 확인하여 flush 를 수행할 만큼 레코드 개수가 찼는지 확인한다.
     * 만약 일정 개수 이상이면 HDFS 적재 로직인 save() 메서드를 호출한다.
     * 여기서는 FLUSH_RECORD_COUNT 를 10으로 설정했기 때문에 버퍼에 10개 이상의 데이터가 쌓이면 save() 가 호출된다.
     * */
    private void checkFlushCount(int partitionNo) {

        if (bufferString.get(partitionNo) != null) {

            if (bufferString.get(partitionNo).size() > FLUSH_RECORD_COUNT - 1) {
                save(partitionNo);
            }

        }

    }

    /*
     * 실질적인 HDFS 적재를 수행하는 로직
     * */
    private void save(int partitionNo) {

        if (!bufferString.get(partitionNo).isEmpty()) {

            try {

                /*
                 * HDFS 에 저장할 파일 이름은 color-{파티션 번호}-{오프셋 번호}.log 이다.
                 * 이렇게 저장해 두면 파티션 번호, 오프셋 번호를 파일 이름만 보고도 바로 알 수 있기 때문에 컨슈머 장애시 복구 시점을 명확히 알 수 있다는 장점이 있다.
                 * */
                String fileName =
                        "/data/color-" + partitionNo + "-" + currentFileOffset.get(partitionNo) + ".log";

                /*
                 * HDFS 적재를 위한 설정을 수행
                 *
                 *  하둡 네임 노드 주소 설정 : hdfs://192.168.70.128:9870
                 *  위 주소를 대상으로 FileSystem 인스턴스를 생성
                 *
                 * */
                Configuration configuration = new Configuration();
                configuration.set("fs.defaultFS", "hdfs://192.168.70.128:8020");
                configuration.set("hadoop.home.dir", "/opt/hadoop");

                FileSystem hdfsFileSystem = FileSystem.get(configuration);

                /*
                 *  버퍼 데이터를 파일로 저장하기 위해 FSDataOutputStream 인스턴스 (HDFS 에 데이터를 파일로 저장하기 위해 자바의 DataOuputStream 클래스를 상속받는 클래스) 를 생성한다.
                 *  bufferString 에 쌓인 버퍼 데이터를 fileOutputStream 에 저장한다.
                 *  저장이 완료된 이후에는 close() 메서드를 통해 안전하게 종료
                 * */
                FSDataOutputStream fileOutputStream = hdfsFileSystem.create(new Path(fileName));

                // bufferString 내용을 개행문자(\n) 을 구분자로 해서 하나의 문자열로 저장
                fileOutputStream.writeBytes(StringUtils.join(bufferString.get(partitionNo), "\n"));

                fileOutputStream.close();

                // 버퍼에 데이터 적재 이후 버퍼 데이터 초기화 (bufferString 내 특정 partitionNo 의 value 값을 새로운 ArrayList 로 초기화)
                bufferString.put(partitionNo, new ArrayList<>());

            } catch (Exception e) {
                // HDFS 파일 적재시 Exception 이 발생했을 경우 에러 로그 출력
                log.error(e.getMessage(), e);
            }

        }

    }

    /*
     * 버퍼에 남아있는 모든 데이터를 저장하기 위한 메서드이다.
     * 컨슈머 스레드 종료 시에 호출된다.
     * */
    private void saveRemainBufferToHdfsFile() {
        // 이래서 save() 에서 한번 더 bufferString 내 크기를 확인했구나..
        bufferString.forEach((partitionNo, v) -> this.save(partitionNo));
    }

    /*
     * 셧다운 훅이 발생했을 때 안전한 종료를 위해 consumer 에 wakeup() 메서드를 호출한다.
     * 남은 버퍼의 데이터를 모두 저장하기 위해 saveRemainBufferToHdfsFile() 메서드도 호출한다.
     * */
    public void stopAndWakeup() {

        log.info("stopAndWakeup");
        consumer.wakeup();
        saveRemainBufferToHdfsFile();

    }

}
