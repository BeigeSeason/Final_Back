package com.springboot.final_back.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchListDto {
    private String title;
    private String content;

    // 제목
}
