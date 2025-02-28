package com.springboot.final_back.repository;

import com.springboot.final_back.entity.elasticsearch.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface DiaryRepository extends ElasticsearchRepository<Diary, String> {

    Page<Diary> findByTitle(String title, Pageable pageable);
    Optional<Diary> findByDiaryId(String diaryId);
}
