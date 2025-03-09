package com.springboot.final_back.dto.report;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportManageReq {
    private Long reportId;
    private boolean state;
    private Long userId;
    private Integer day;
    private String reason;
    private String diaryId;
    private Long reviewId;
}
