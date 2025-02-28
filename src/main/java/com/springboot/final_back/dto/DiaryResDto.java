package com.springboot.final_back.dto;

import com.springboot.final_back.entity.elasticsearch.Diary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryResDto {
    private String diaryId;     // 다이어리 아이디
    private String title;       // 제목
    private String region;      // 지역
    private LocalDate startDate; // 일정 시작일
    private LocalDate endDate;   // 일정 종료일
    private Set<String> tags;     // 태그 (Set)
    private Float totalCost;      // 여행 경비
    private String content;     // 내용
    private String userId;
    private boolean isPublic;

    public static DiaryResDto fromEntity(Diary diary, String userId) {
        return DiaryResDto.builder()
                .diaryId(diary.getDiaryId())
                .title(diary.getTitle())
                .region(diary.getRegion())
                .startDate(diary.getStartDate())
                .endDate(diary.getEndDate())
                .tags(diary.getTags())
                .totalCost(diary.getTotalCost())
                .content(diary.getContent())
                .isPublic(diary.isPublic())
                .userId(userId)
                .build();
    }
}
