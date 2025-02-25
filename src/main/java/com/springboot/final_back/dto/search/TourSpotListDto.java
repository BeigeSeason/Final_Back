package com.springboot.final_back.dto.search;

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


    @Builder
    public TourSpotListDto(String spotId, String title, String addr, String thumbnail) {
        this.spotId = spotId;
        this.title = title;
        this.addr = addr;
        this.thumbnail = thumbnail;
    }
}
