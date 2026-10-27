package org.study.redis.config.swagger;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;

@OpenAPIDefinition(
        info = @Info(title = "srpingdoc-openapi",
                version = "v1",
                description = "springdoc-openapi swagger-ui 화면입니다.")
)
@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi api() {

        // /redis/v1/ 로 시작하는 API 들을 테스트 관련 API 로 그룹핑

        String[] paths = {"/redis/v1/**"};
        String[] packageToScan = {"org.study.redis.controller.redis"};

        return GroupedOpenApi.builder().group("springdoc-openapi")
                .pathsToMatch(paths)
                .packagesToScan(packageToScan)
                .build();

    }

}
