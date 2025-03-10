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
    private String cat1;
    private String cat2;
    private String cat3;
    private int reviewCount;
    private double avgRating;
    private int bookmarkCount;

    @Builder
    public TourSpotListDto(String spotId, String title, String addr, String thumbnail, String cat1, String cat2, String cat3, int reviewCount, double avgRating, int bookmarkCount) {
        this.spotId = spotId;
        this.title = title;
        this.addr = addr;
        this.thumbnail = thumbnail;
        this.cat1 = cat1;
        this.cat2 = cat2;
        this.cat3 = cat3;
        this.reviewCount = reviewCount;
        this.avgRating = avgRating;
        this.bookmarkCount = bookmarkCount;
    }
}
