package com.springboot.final_back.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DotEnvConfig {

    @Bean
    public Dotenv dotenv() {
        // .env 파일을 읽어서 환경변수로 사용
        return Dotenv.configure().directory("./")
                .ignoreIfMissing() // .env 파일이 없어도 에러 발생 안함
                .load();
    }
}
