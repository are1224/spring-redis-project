package org.study.redis.controller.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.study.redis.domain.User;
import org.study.redis.service.redis.RedisService;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
@RequestMapping("/redis/v1")
public class RedisTestController {

    private final RedisService redisService;

    @PostMapping(value = "/saveUser")
    public ResponseEntity getRecentFrngPlorRlnc1(@Nullable @RequestBody User user) throws Exception {

        System.out.println("test");
        System.out.println(user.toString());

        redisService.setValues("session1",
                user,
                Duration.of(180000, TimeUnit.MILLISECONDS.toChronoUnit()));

        return new ResponseEntity(null, HttpStatus.OK);
    }

    @GetMapping(value = "/findUser")
    public ResponseEntity findUser(@Nullable @RequestParam String userId) throws Exception {

        User findUser = redisService.getValues(userId, User.class);

        if(findUser != null) {
            System.out.println(findUser.toString());
        }

        return new ResponseEntity(findUser, HttpStatus.OK);
    }

}
