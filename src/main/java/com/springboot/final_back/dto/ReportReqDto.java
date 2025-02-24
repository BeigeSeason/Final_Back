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
    private String reason;
    private Member reporter;
    private Member reported;

    public Report toEntity(String reason, Member reporter, Member reported) {
        return Report.builder()
                .reason(reason)
                .reporter(reporter)
                .reported(reported)
                .build();
    }
}
