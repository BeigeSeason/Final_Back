package com.springboot.final_back.dto.diary;

import com.springboot.final_back.entity.elasticsearch.Diary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryResDto {
    private String diaryId;     // 다이어리 아이디
    private String title;       // 제목
    private String region;      // 지역
    private String areaCode;    // 지역 코드
    private String sigunguCode; // 시군구 코드
    private LocalDateTime createdTime; // 작성 시간
    private LocalDate startDate; // 일정 시작일
    private LocalDate endDate;   // 일정 종료일
    private List<String> tags;     // 태그 (Set)
    private Integer totalCost;      // 여행 경비
    private String content;     // 내용
    private String nickname;
    private String ownerId;
    private String profileImgPath;
    private boolean isPublic;
    private int bookmarkCount;

    public static DiaryResDto fromEntity(Diary diary, String nickname, String ownerId, String imgPath) {
        return DiaryResDto.builder()
                .diaryId(diary.getDiaryId())
                .title(diary.getTitle())
                .region(diary.getRegion())
                .areaCode(diary.getAreaCode())
                .sigunguCode(diary.getSigunguCode())
                .createdTime(diary.getCreatedTime())
                .startDate(diary.getStartDate())
                .endDate(diary.getEndDate())
                .tags(diary.getTags())
                .totalCost(diary.getTotalCost())
                .content(diary.getContent())
                .isPublic(diary.isPublic())
                .bookmarkCount(diary.getBookmarkCount())
                .nickname(nickname)
                .ownerId(ownerId)
                .profileImgPath(imgPath)
                .build();
    }
}
