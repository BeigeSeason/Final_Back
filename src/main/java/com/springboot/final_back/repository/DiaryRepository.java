package com.springboot.final_back.repository;

import com.springboot.final_back.entity.elasticsearch.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DiaryRepository extends ElasticsearchRepository<Diary, String> {

    Page<Diary> findByTitle(String title, Pageable pageable);
    Page<Diary> findByMemberId(Long memberId, Pageable pageable);
    Optional<Diary> findByDiaryId(String diaryId);

    // 월별 일지 통계
    @Query("SELECT MONTH(d.createdTime), COUNT(d) " +
            "FROM Diary d " +
            "WHERE YEAR(d.createdTime) = :year " +
            "GROUP BY MONTH(d.createdTime) " +
            "ORDER BY MONTH(d.createdTime)")
    List<Object[]> getMonthlyDiaryCounts(@Param("year") int year);
}
