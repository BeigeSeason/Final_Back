package com.springboot.final_back.dto.report;

import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.entity.mysql.Report;
import com.springboot.final_back.constant.Type;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportReqDto {
    private String reason;
    private String reporter;
    private String reported;
    private String reportEntity;
    private Type reportType;

    public Report toEntity(String reason, Member reporter, Member reported, String reportEntity, Type reportType) {
        return Report.builder()
                .reason(reason)
                .reporter(reporter)
                .reported(reported)
                .reportEntity(reportEntity)
                .reportType(reportType)
                .build();
    }
}
