package com.springboot.final_back.config;

import com.springboot.final_back.dto.tourspot.TourSpotStats;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


import java.util.Collections;


@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
            request.getHeaders().setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    @Bean
    public RedisTemplate<String, TourSpotStats> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, TourSpotStats> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(TourSpotStats.class));
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }

}