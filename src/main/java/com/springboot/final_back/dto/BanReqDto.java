package com.springboot.final_back.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BanReqDto {
    private Long id;
    private int day;
    private String reason;
}
