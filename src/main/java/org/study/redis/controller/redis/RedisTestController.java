package org.study.redis.controller.redis;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.study.redis.domain.RequestUserDto;
import org.study.redis.domain.User;
import org.study.redis.service.jpa.UserService;
import org.study.redis.service.redis.RedisService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/redis/v1")
public class RedisTestController {

    private final RedisService redisService;

    private final UserService userService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping(value = "/saveUser")
    public ResponseEntity saveUser(@Nullable @RequestBody User user) throws Exception {

        log.info("test");
        log.info(user.toString());

        //        redisService.setValues(user,
        //                                Duration.of(180000, TimeUnit.MILLISECONDS.toChronoUnit()));

        return new ResponseEntity(null, HttpStatus.OK);
    }

    // @PostMapping(value = "/login")
    // public ResponseEntity login(@Nullable @RequestBody User user, HttpServletRequest httpRequest,
    //         HttpServletResponse httpResponse) throws Exception {
    //
    //     log.info("test");
    //     log.info(user.toString());
    //
    //     String secretId = "";
    //
    //     User findUser = userService.findUser(user);
    //
    //     if (findUser != null) {
    //
    //         if (bCryptPasswordEncoder.matches(user.getPassword(), findUser.getPassword())) {
    //
    //             final HttpSession session = httpRequest.getSession();
    //             session.setAttribute("userId", user.getUserId());
    //             session.setMaxInactiveInterval(180000);
    //
    //             String sessionKey = Base64.getEncoder().encodeToString(session.getId().getBytes());
    //             redisService.setValues(sessionKey, user);
    //
    //             // 쿠키 생성
    //             Cookie cookie = new Cookie("secretId", sessionKey);
    //             cookie.setMaxAge(180000);
    //
    //             httpResponse.addCookie(cookie);
    //
    //             return new ResponseEntity(null, HttpStatus.OK);
    //
    //         }
    //
    //         return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
    //
    //     } else {
    //         return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
    //     }
    //
    // }

    @GetMapping(value = "/findUser")
    public ResponseEntity<User> findUser(@RequestHeader("session-key") String sessionKey) throws Exception {

        User findUser = redisService.getValues(sessionKey, User.class);

        if (findUser != null) {
            log.info(findUser.toString());
        }

        return new ResponseEntity<User>(findUser, HttpStatus.OK);
    }

    @GetMapping(value = "/findTest")
    public String findTest(HttpServletRequest httpRequest) throws Exception {

        return "/signUp";

    }

    @GetMapping(value = "/login")
    public String loginView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        String referer = httpRequest.getHeader("Referer");
        httpRequest.getSession().setAttribute("prevUrl", referer);
        return "/login";
    }

    @GetMapping(value = "/success")
    public String success() {
        return "/success";
    }

    @GetMapping(value = "/signup")
    public String signup() {
        return "/signUp";
    }

    @PostMapping(value = "/doSignUp")
    public String doSignUp(
            @RequestParam String userId,
            @RequestParam String userName,
            @RequestParam String password,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        userService.registerUser(RequestUserDto.builder().userId(userId).userName(userName).password(password).build());

        return "/success";
    }

    @GetMapping(value = "/failure")
    public String failure() throws Exception {

        return "/failure";

    }

}
