package com.springboot.final_back.dto.tourspot;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TourSpotListDto {
    private String spotId;
    private String title;
    private String addr;
    private String thumbnail;
    private int reviewCount;
    private double avgRating;
    private int bookmarkCount;

    @Builder
    public TourSpotListDto(String spotId, String title, String addr, String thumbnail, int reviewCount, double avgRating, int bookmarkCount) {
        this.spotId = spotId;
        this.title = title;
        this.addr = addr;
        this.thumbnail = thumbnail;
        this.reviewCount = reviewCount;
        this.avgRating = avgRating;
        this.bookmarkCount = bookmarkCount;
    }
}
