package com.springboot.final_back.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourSpotDetailDto {
    private Long contentId;
    private String title;
    private String addr1;
    private String addr2;
    private String tel;
    private float mapX;
    private float mapY;

}
