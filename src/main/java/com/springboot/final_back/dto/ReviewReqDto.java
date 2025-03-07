package com.springboot.final_back.dto;

import lombok.Data;

@Data
public class ReviewReqDto {
    private Long id; // 해당 리뷰 고유 아이디(수정 시 사용)
    private String memberId; // 리뷰 남긴 유저 아이디
    private int rating;  // 점수
    private String tourSpotId; // 리뷰 대상 여행지
    private String content; // 내용
}
