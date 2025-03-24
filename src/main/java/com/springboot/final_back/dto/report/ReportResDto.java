package com.springboot.final_back.dto.report;

import com.springboot.final_back.constant.State;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.entity.mysql.Report;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResDto {
    private Long id;
    private String reportType;
    private Long reporter;
    private Long reported;
    private String reportEntity;
//    private Diary diaryId;
//    private Review reviewId;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime checkedAt;
    private State state;

    private String reviewContent;

    public static ReportResDto of(Report report) {
        return ReportResDto.builder()
                .id(report.getId())
                .reportType(String.valueOf(report.getReportType()))
                .reporter(report.getReporter().getId())
                .reported(report.getReported().getId())
                .reportEntity(report.getReportEntity())
                .reason(report.getReason())
                .createdAt(report.getCreatedAt())
                .checkedAt(report.getCheckedAt())
                .state(report.getState())
                .build();
    }
}
