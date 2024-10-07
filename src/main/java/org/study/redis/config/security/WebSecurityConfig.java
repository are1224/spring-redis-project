package org.study.redis.config.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.study.redis.config.filter.RedisAuthenticationFilter;
import org.study.redis.config.handler.CustomLoginFailureHandler;
import org.study.redis.config.provider.CustomAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;

    private final CustomLoginFailureHandler customLoginFailureHandler;

    private final RedisAuthenticationFilter redisAuthenticationFilter;

    public WebSecurityConfig(
            CustomAuthenticationProvider customAuthenticationProvider,
            RedisAuthenticationFilter redisAuthenticationFilter,
            CustomLoginFailureHandler customLoginFailureHandler) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.redisAuthenticationFilter = redisAuthenticationFilter;
        this.customLoginFailureHandler = customLoginFailureHandler;
    }

    /**
     * 정적 자원에 대해서는 Security 설정을 적용하지 않게 설정
     * */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {

        return new WebSecurityCustomizer() {

            @Override
            public void customize(WebSecurity web) {
                web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
            }

        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // requestCache 를 통해서 저장하는 사용자의 인증전 처음 요청 url 에 쓸데없이 추가된 continue 문 제거
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);

        // http basic
        http.httpBasic(HttpBasicConfigurer::disable);

        // 사이트 위변조 요청 방지
        http.csrf(AbstractHttpConfigurer::disable);

        // session 설정
        http.sessionManagement(configurer -> {
            configurer.maximumSessions(1).maxSessionsPreventsLogin(false);
        });

        // 인증 (접근권한) 설정
        http.authorizeHttpRequests(
                (authorizeRequests) ->
                        authorizeRequests.requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui-custom.html",
                                        "/templates/thymeleaf/**", "/error", "/redis/v1/login")
                                .permitAll()
                                .anyRequest()
                                .authenticated()
        );

        // http.authorizeHttpRequests(
        //         (authorizeRequests) ->
        //                 authorizeRequests.requestMatchers("/redis/v1/findTest")
        //                         .authenticated()
        //                         .anyRequest()
        //                         .permitAll()
        // );

        // requestCache 설정
        http.requestCache(request -> request.requestCache(requestCache));

        // 로그인 설정
        http.formLogin(form -> form.loginPage("/redis/v1/login") // 로그인 페이지 (get)
                .loginProcessingUrl("/redis/v1/login")
                .defaultSuccessUrl("/redis/v1/success")// 로그인 처리 url (post)
                // .successHandler(
                //         customLoginSuccessHandler) // @EnableRedisHttpSession 싹다 알아서 해줌 (처음 요청한 url 로 리다이렉트 처리 및 security context 관련 처리해줌
                .failureHandler(customLoginFailureHandler) // provider 를 통해서 인증에 실패한 경우 처리 handler
                .permitAll());

        // Custom AuthenticationManager 설정
        http.authenticationProvider(customAuthenticationProvider);

        http.addFilterBefore(redisAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // UsernamePasswordAuthenticationFilter
        // SpringSessionRepositoryFilter

        return http.build();

    }

}
