package com.springboot.final_back.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiaryReqDto {
    private String id;          // 아이디
    private String title;       // 제목
    private String region;      // 지역
    private LocalDateTime startDate; // 일정 시작일
    private LocalDateTime endDate;   // 일정 종료일
    private Set<String> tags;     // 태그 (Set)
    private Float totalCost;      // 여행 경비
    private String content;     // 내용
    private String userId;
}
