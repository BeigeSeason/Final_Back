package com.springboot.final_back.entity.elasticsearch;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.Id;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Document(indexName = "diary")
public class Diary {
    // 다이어리 구분자(자동 생성)
    @Id
    @Field(name = "diaryId", type = FieldType.Keyword)
    private String id;

    // 제목
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_with_stopwords"),
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    private String title;

    // 사용자 아이디(Long, 수정 필요)
    @Field(type = FieldType.Keyword)
    private Long memberId;

    // 지역(코드 대신 직접 텍스트로)
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_with_stopwords"),
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    private String region;

    // 작성일
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time_no_millis)
    private LocalDateTime createdTime;

    // 일정 시작일
    @Field(type = FieldType.Date, format = DateFormat.basic_date)
    private LocalDateTime startDate;

    // 일정 종료일
    @Field(type = FieldType.Date, format = DateFormat.basic_date)
    private LocalDateTime endDate;

    // 태그(Set 으로 중복 제거)
    @Field(type = FieldType.Keyword)
    private Set<String> tags;

    // 여행경비
    @Field(type = FieldType.Float)
    private Float totalCost;

    // 내용
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_with_stopwords"),
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    private String content;

    @PrePersist
    public void prePersist() {
        createdTime = LocalDateTime.now();
    }

    @Builder
    private Diary(String title, String region, LocalDateTime startDate, LocalDateTime endDate, Set<String> tags, Float totalCost, String content, Long memberId) {
        this.title = title;
        this.region = region;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tags = tags;
        this.totalCost = totalCost;
        this.content = content;
        this.memberId = memberId;
    }
}
