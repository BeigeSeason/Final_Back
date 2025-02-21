package com.springboot.final_back.dto;

import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.entity.mysql.Report;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportReqDto {
    private String content;
    private String reporter;
    private String reported;

    public Report toEntity(String content, Member reporter, Member reported) {
        return Report.builder()
                .reason(content)
                .reporter(reporter)
                .reported(reported)
                .build();
    }
}
