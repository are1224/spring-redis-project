package org.study.redis.config.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.study.redis.config.handler.CustomLoginFailureHandler;
import org.study.redis.config.handler.CustomLoginSuccessHandler;
import org.study.redis.config.provider.CustomAuthenticationProvider;
import org.study.redis.service.redis.RedisService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;

    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    private final CustomLoginFailureHandler customLoginFailureHandler;

    private final RedisService redisService;

    public WebSecurityConfig(
            CustomAuthenticationProvider customAuthenticationProvider,
            CustomLoginSuccessHandler customLoginSuccessHandler,
            CustomLoginFailureHandler customLoginFailureHandler,
            RedisService redisService) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.customLoginSuccessHandler = customLoginSuccessHandler;
        this.customLoginFailureHandler = customLoginFailureHandler;
        this.redisService = redisService;
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
            //configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS); 이 기능을 활성화시 매 요청마다 Session 이 갱신되게 된다.
        });

        // 인증 (접근권한) 설정
        http.authorizeHttpRequests(
                (authorizeRequests) -> authorizeRequests.requestMatchers("/redis/v1/findTest")
                        .authenticated()
                        .anyRequest()
                        .permitAll()
        );

        // requestCache 설정
        http.requestCache(request -> request.requestCache(requestCache));

        // 로그인 설정
        http.formLogin(form -> form.loginPage("/redis/v1/login") // 로그인 페이지 (get)
                .loginProcessingUrl("/redis/v1/login") // 로그인 처리 url (post)
                .successHandler(customLoginSuccessHandler) // provider 를 통해서 인증에 성공한 경우 처리 handler
                .failureHandler(customLoginFailureHandler) // provider 를 통해서 인증에 실패한 경우 처리 handler
                .defaultSuccessUrl("/redis/v1/success")
                .permitAll());

        // Custom AuthenticationManager 설정
        http.authenticationProvider(customAuthenticationProvider);

        // filter 설정 ( 어차피 로그인 처리 자체가 UsernamePasswordAuthenticationFilter 에서 이뤄지므로 나중에 Redis 관련 필터를  UsernamePasswordAuthenticationFilter 앞에 추가해야한다. )
        // http.addFilterAfter(customAuthenticationFilter(redisService),
        //         UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    /*
     *  authentication manager 에 customAuthenticationProvider 추가
     * */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws
            Exception {
        ProviderManager authenticationManager = (ProviderManager)authenticationConfiguration.getAuthenticationManager(); // AuthenticationManager 생성
        authenticationManager.getProviders()
                .add(customAuthenticationProvider); // AuthenticationManager에 CustomAuthenticationProvider 등록
        return authenticationManager;
    }

    /*
     * Redis Authentication 필터
     * 이미 로그인한 유저에 대해서는 다시 인증 처리할 필요 없게 SecurityContext 를 Redis 에서 꺼내와서 사용
     * */
    // @Bean
    // public CustomAuthenticationFilter customAuthenticationFilter(RedisService redisService) throws
    //         Exception {
    //     return new CustomAuthenticationFilter(redisService); // provider 를 새로 설정한 authenticationManager 를 인자로 필터 생성
    // }

}
