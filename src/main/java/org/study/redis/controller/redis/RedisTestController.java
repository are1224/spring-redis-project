package org.study.redis.controller.redis;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.study.redis.domain.User;
import org.study.redis.service.redis.RedisService;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/redis/v1")
public class RedisTestController {

    private final RedisService redisService;

    @PostMapping(value = "/saveUser")
    public ResponseEntity saveUser(@Nullable @RequestBody User user) throws Exception {

        log.info("test");
        log.info(user.toString());

        redisService.setValues(user,
                                Duration.of(180000, TimeUnit.MILLISECONDS.toChronoUnit()));

        return new ResponseEntity(null, HttpStatus.OK);
    }

    @PostMapping(value = "/login")
    public ResponseEntity login(@Nullable @RequestBody User user, HttpServletResponse response) throws Exception {

        log.info("test");
        log.info(user.toString());

        String secretId = "";

        String sessionKey = redisService.setValues(user);

        // 쿠키 생성
        Cookie cookie = new Cookie("secretId", sessionKey);
        cookie.setMaxAge(30000);

        response.addCookie(cookie);

        return new ResponseEntity(null, HttpStatus.OK);
    }

    @GetMapping(value = "/findUser")
    public ResponseEntity<User> findUser(@RequestHeader("session-key") String sessionKey) throws Exception {

        User findUser = redisService.getValues(sessionKey, User.class);

        if(findUser != null) {
            log.info(findUser.toString());
        }

        return new ResponseEntity<User>(findUser, HttpStatus.OK);
    }



}
