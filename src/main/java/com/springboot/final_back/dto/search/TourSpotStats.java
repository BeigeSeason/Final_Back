package com.springboot.final_back.dto.search;

import lombok.Data;

@Data
public class TourSpotStats {
    private String contentId;
    private int reviewCount;
    private double avgRating;
    private int bookmarkCount;

    public TourSpotStats(String contentId, int reviewCount, double avgRating, int bookmarkCount) {
        this.contentId = contentId;
        this.reviewCount = reviewCount;
        this.avgRating = avgRating;
        this.bookmarkCount = bookmarkCount;
    }
}
