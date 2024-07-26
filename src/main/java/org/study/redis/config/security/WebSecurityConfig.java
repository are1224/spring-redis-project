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

        // requestCache 를 통해서 저장하는 사용자의 인증전 처음 요청 url 에 쓸데없이 추가된 continue 문 제거
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);

        http.httpBasic(HttpBasicConfigurer::disable);

        // 사이트 위변조 요청 방지
        http.csrf(AbstractHttpConfigurer::disable);

        // session 설정
        http.sessionManagement(configurer -> {
            // configurer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            configurer.maximumSessions(1);
        });

        // authentication Manager 설정
        // AuthenticationManager authenticationManager = authenticationManager(http);

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
        // http.addFilterAfter(customAuthenticationFilter(authenticationManager),
        //         UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    // @Bean
    // public AuthenticationManager authenticationManager(CustomAuthenticationProvider customAuthenticationProvider) throws
    //         Exception {
    //     return new ProviderManager(Collections.singletonList(customAuthenticationProvider));
    // }

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

    // @Bean
    // public CustomAuthenticationFilter customAuthenticationFilter(AuthenticationManager authenticationManager) throws
    //         Exception {
    //
    //     CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(
    //             authenticationManager); // provider 를 새로 설정한 authenticationManager 를 인자로 필터 생성
    //     customAuthenticationFilter.setFilterProcessesUrl("/redis/v1/login-process"); // 필터를 적용할 url
    //     customAuthenticationFilter.setAuthenticationSuccessHandler(
    //             customLoginSuccessHandler); // CustomAuthenticationFilter 를 통해 인증에 성공했을 때 적용할 SuccessHandler
    //     customAuthenticationFilter.setAuthenticationFailureHandler(
    //             customLoginFailureHandler); // CustomAuthenticationFilter 를 통해 인증에 실패했을 때 적용할 FailureHandler
    //     customAuthenticationFilter.afterPropertiesSet();
    //
    //     return customAuthenticationFilter;
    //
    // }

}
