package org.study.redis.config.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.study.redis.domain.User;
import org.study.redis.service.jpa.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public CustomAuthenticationProvider(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     *
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)authentication;

        // AuthenticationFilter 에서 생성된 토큰으로부터 아이디와 비밀버호를 조회함
        String userId = (String)token.getPrincipal();
        String password = (String)token.getCredentials();

        User findUser = userService.findUser(new User(userId, password));

        if (!bCryptPasswordEncoder.matches(password, findUser.getPassword())) {
            log.info("문제 발생 / password : {}, encrytion password : {}", password,
                    bCryptPasswordEncoder.encode(password));
            throw new BadCredentialsException(findUser.getUsername() + "Invalid password");
        }

        // List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        // // Add additional grant/authority
        // authorities.add(new SimpleGrantedAuthority("USER"));

        // findUser.setAuthorities(authorities);

        // 인자가 3개인 authentication 의 Authenticated 를 true 로 설정하네...
        return new UsernamePasswordAuthenticationToken(findUser, null, findUser.getAuthorities());
    }

    /**
     * 어떤 형식의 Authentication 객체에 대해서 인증을 제공하는지 boolean 리턴 (UsernamePasswordAuthenticationToken 에 대해서 지원)
     *  AuthenticationProvider 가 어떤 종류의 Authentication 객체를 지원하는지에 대해서
     *  authenticate() 메서드의 매개 변수로 어떤 형식이 제공될지에 따라서 달라짐
     * @param authentication
     * @return
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
