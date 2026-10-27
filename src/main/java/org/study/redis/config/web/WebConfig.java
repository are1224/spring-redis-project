package org.study.redis.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    //private final ApplicationContext applicationContext;

    /**
     * 정적 자원 관련 핸들러 추가
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        WebMvcConfigurer.super.addResourceHandlers(registry);

        registry.addResourceHandler("/**") // 정적 자원을 제공해주기 위한 Resource Handler, 파라미터로는 아래 저장소와 연결되는 url 설정하기
                .addResourceLocations("classpath:/templates/thymeleaf/") // static 콘텐츠를 제공해야하는 하나 이상의 저장공간 설정
                .setCachePeriod(20); // 리소스 핸들러를 통해서 자원에 대해서 캐싱해야하는 구체적인 시간 (second)

    }
}
