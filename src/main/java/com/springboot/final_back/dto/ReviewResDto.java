package com.springboot.final_back.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResDto {
    private Long id; // 해당 리뷰 고유 아이디(수정 시 사용)
    private String memberId; // 리뷰 남긴 유저 아이디
    private String nickname; // 닉네임
    private String profileImg; // 이용자 프로필사진
    private LocalDateTime createdAt; // 작성일
    private float rating;  // 점수
    private String content; // 내용
}
