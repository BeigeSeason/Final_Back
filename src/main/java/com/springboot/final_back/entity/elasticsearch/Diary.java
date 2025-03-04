package com.springboot.final_back.entity.elasticsearch;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;


import javax.persistence.PrePersist;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data // getter, setter, toString, equals, hashCode 등을 자동 생성
@Document(indexName = "diary") // Elasticsearch의 diary라는 인덱스에 저장
public class Diary {
    // 다이어리 구분자(자동 생성)
    @Id
    private String id;

    @Field(name = "diary_id", type = FieldType.Keyword) // 필드명 : diaryId, 검색이나 필터링 시 분석(토큰화) 없이 그대로 사용
    private String diaryId;

    // 제목
    @MultiField( // 한 필드를 여러 방식으로 인덱싱
            // FieldType.Text : 텍스트 분석(토큰화) 후 검색 가능
            // nori_analyzer_with_stopwords : 형태소 분석기(nori) + 불용어 제거 (ex. 여행 다이어리 -> [여행, 다이어리]
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_with_stopwords"),
            // nori_ngram_analyzer: N-gram으로 세분화해 부분 검색 가능 (ex. 여행 -> [여, 행, 여행]
            // 검색 시 title.ngram으로 접근
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    private String title;

    // 사용자 아이디(Long)
    @Field(type = FieldType.Keyword, name = "member_id")
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
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis, name = "created_time")
    private LocalDateTime createdTime;

    // 일정 시작일
    @Field(type = FieldType.Date, format = DateFormat.basic_date, name = "start_time")
    private LocalDate startDate;

    // 일정 종료일
    @Field(type = FieldType.Date, format = DateFormat.basic_date, name = "end_date")
    private LocalDate endDate;

    // 태그(Set 으로 중복 제거)
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    // 여행경비
    @Field(type = FieldType.Float, name = "total_cost")
    private Float totalCost;

    // 내용
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_with_stopwords"),
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    private String content;

    // 공개 여부
    @Field(type = FieldType.Boolean, name = "is_public")
    private boolean isPublic;

    @Field(type = FieldType.Float, name = "bookmark_count")
    private int bookmarkCount;


    @Builder
    private Diary(String diaryId, String title, String region, LocalDate startDate, LocalDate endDate, List<String> tags, Float totalCost, String content, Long memberId, boolean isPublic) {
        this.diaryId = diaryId;
        this.title = title;
        this.region = region;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tags = tags;
        this.totalCost = totalCost;
        this.content = content;
        this.memberId = memberId;
        this.isPublic = isPublic;
        createdTime = LocalDateTime.now();
    }
}
