package com.springboot.final_back.dto.diary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiaryReqDto {
    private String id;          // elastic 아이디
    private String diaryId;     // 다이어리 아이디
    private String title;       // 제목
    private String region;      // 지역
    private String areaCode;    // 지역 코드
    private String sigunguCode; // 시군구 코드
    private LocalDate startDate; // 일정 시작일
    private LocalDate endDate;   // 일정 종료일
    private List<String> tags;     // 태그 (Set -> List _ 순서 보장을 위해)
    private Integer totalCost;      // 여행 경비
    private String content;     // 내용
    private String userId;

    @JsonProperty("isPublic")
    private boolean isPublic;
}
