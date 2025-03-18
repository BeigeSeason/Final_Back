package com.springboot.final_back.dto.tourspot;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourSpotDetailDto {
    private String contentId;         // 관광지 ID
    private String title;             // 관광지 제목
    private String addr1;             // 주소
    private String infoCenter;        // 연락처 (기존 tel 재사용)
    private Float mapX;               // 경도
    private Float mapY;               // 위도
    private List<String> images;      // 상세 이미지 URL 리스트 (detailImage1에서)
    private String overview;          // 개요 (detailCommon1에서)
    private String homepage;          // 홈페이지 (detailCommon1에서)
    private String useTime;           // 운영 시간 (detailIntro1에서)
    private String parking;           // 주차 정보 (detailIntro1에서)
    private int bookmarkCount;        // 총 북마크 개수
    private List<TourSpotListDto> nearSpots; // 가까운 여행지 10개
}
