package com.springboot.final_back.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("Springdoc OpenAPI 설정 호출 !!");
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList("JWT")) // JWT 보안 요구사항 추가
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    private Info apiInfo() {
        log.info("Springdoc apiInfo() 함수 호출 !!");
        return new Info()
                .title("Spring Boot Open API Test with Swagger")
                .description("설명 부분")
                .version("2.5.0");
    }
}