package org.study.redis.config.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.study.redis.config.filter.CustomAuthenticationFilter;
import org.study.redis.config.handler.CustomLoginFailureHandler;
import org.study.redis.config.handler.CustomLoginSuccessHandler;
import org.study.redis.config.provider.CustomAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;

    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    private final CustomLoginFailureHandler customLoginFailureHandler;

    public WebSecurityConfig(
            CustomAuthenticationProvider customAuthenticationProvider,
            CustomLoginSuccessHandler customLoginSuccessHandler,
            CustomLoginFailureHandler customLoginFailureHandler) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.customLoginSuccessHandler = customLoginSuccessHandler;
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

        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);

        // authentication Manager 설정
        AuthenticationManager authenticationManager = authenticationManager(http);

        // 인증 (접근권한) 설정
        http.authorizeHttpRequests((authorizeRequests) ->
                authorizeRequests.requestMatchers(HttpMethod.GET, "/redis/v1/find**")
                        .hasRole("USER")
                        .anyRequest()
                        .permitAll());

        // 사이트 위변조 요청 방지
        http.csrf(AbstractHttpConfigurer::disable);

        http.requestCache(request -> request.requestCache(requestCache));

        // 로그인 설정
        http.formLogin(form -> form.loginPage("/redis/v1/login")
                //.loginProcessingUrl("/redis/v1/login-process")
                // .defaultSuccessUrl("/redis/v1/success")
                //.successHandler(new CustomAuthenticationSuccessHandler("/main"))
                // .failureUrl("/redis/v1/login-fail")
                //.failureHandler(new CustomAuthenticationFailureHandler("/login-fail")));
        );

        // filter 설정
        http.addFilterAfter(customAuthenticationFilter(authenticationManager),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws
            Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(
                AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter(AuthenticationManager authenticationManager) throws
            Exception {

        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(
                authenticationManager); // provider 를 새로 설정한 authenticationManager 를 인자로 필터 생성
        customAuthenticationFilter.setFilterProcessesUrl("/redis/v1/login-process"); // 필터를 적용할 url
        customAuthenticationFilter.setAuthenticationSuccessHandler(
                customLoginSuccessHandler); // CustomAuthenticationFilter 를 통해 인증에 성공했을 때 적용할 SuccessHandler
        customAuthenticationFilter.setAuthenticationFailureHandler(
                customLoginFailureHandler); // CustomAuthenticationFilter 를 통해 인증에 실패했을 때 적용할 FailureHandler
        customAuthenticationFilter.afterPropertiesSet();

        return customAuthenticationFilter;

    }

}
