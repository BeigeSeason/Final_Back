package com.springboot.final_back.dto;

import lombok.Data;

@Data
public class ReviewResDto {
    private Long id; // 해당 리뷰 고유 아이디(수정 시 사용)
    private String memberId; // 리뷰 남긴 유저 아이디
    private String nickname;
    private String profileImg;
    private float rating;  // 점수
    private String content; // 내용
}
