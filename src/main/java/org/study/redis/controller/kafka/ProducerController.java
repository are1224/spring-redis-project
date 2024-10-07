package org.study.redis.controller.kafka;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.study.redis.domain.UserEventVO;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@RestController
// REST API 를 다른 도메인에서도 호출할 수 있도록 CORS (Cross-Origin Resource Sharing) 설정하기 위해 @CrossOrigin 애노테이션 추가
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ProducerController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ProducerController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /*
     * 브라우저 호출시 자동으로 들어가는 헤더 값인 user-agent 도 userAgentName 변수로 받는다.
     * */
    @GetMapping("/api/select")
    public void selectColor(
            @RequestHeader("user-agent") String userAgentName,
            @RequestParam(value = "color") String colorName,
            @RequestParam(value = "user") String userName) {

        // 최종 적재 시간을 위한 format
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

        Date now = new Date();
        Gson gson = new Gson();
        UserEventVO userEventVO = UserEventVO.builder()
                .timestamp(sdfDate.format(now))
                .userAgent(userAgentName)
                .colorName(colorName)
                .userName(userName)
                .build();

        // 자바 객체 -> JSON 포맷의 String 타입으로 변환
        String jsonColorLog = gson.toJson(userEventVO);

        /*
         * select-color 토픽에 데이터를 전송
         * 메시지 키를 지정하지 않으므로 send() 메서드에는 토픽과 메시지 값만 넣으면 된다.
         *
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
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("select-color", jsonColorLog);

        /*
         * 전송 성공 실패 내역을 로그로 남긴다.
         *
         * 프로듀서가 보낸 데이터의 브로커 적재 여부를 비동기적으로 확인할 수 있다.
         * 브로커에 정상 적재 되었다면 exception 이 발생하지 않음으로 아래 if 문 로직이 실행된다.
         */
        future.whenComplete((result, ex) -> {

            // result : SendResult<String, String>

            if (ex == null) {
                log.info(result.toString());
            } else {
                log.info(ex.getMessage(), ex);
            }

        });

    }

}
