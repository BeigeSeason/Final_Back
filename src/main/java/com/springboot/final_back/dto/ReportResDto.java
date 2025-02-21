package com.springboot.final_back.dto;

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
    private Member reporter;
    private Member reported;
//    private Diary diaryId;
//    private Review reviewId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime checkedAt;
    private State state;

    public static ReportResDto of(Report report) {
        return ReportResDto.builder()
                .id(report.getId())
                .reportType(String.valueOf(report.getReportType()))
                .reporter(report.getReporter())
                .reported(report.getReported())
                .content(report.getReason())
                .createdAt(report.getCreatedAt())
                .checkedAt(report.getCheckedAt())
                .state(report.getState())
                .build();
    }
}
