package com.springboot.final_back.config;

import com.springboot.final_back.entity.elasticsearch.Diary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;


@RequiredArgsConstructor
@Configuration
@Slf4j
public class ElasticsearchIndexInitializer {
    private final ElasticsearchOperations elasticsearchOperations;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndex() {
        boolean diaryIndexExists = elasticsearchOperations.indexOps(Diary.class).exists();

        if (!diaryIndexExists) {
            log.info("Diary not exists");
        }else {
            log.info("Diary already exists");
        }
    }
}
