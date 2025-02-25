package com.springboot.final_back.dto.search;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiarySearchListDto {
    private String title; // 제목
    private String contentSummary; // 내용 요약
    private String thumbnail; // 썸네일
    private String writer; // 작성자
    private String writerImg; // 작성자 프로필 이미지
    private LocalDateTime createdAt; // 작성일
}
