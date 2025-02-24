package com.springboot.final_back.controller;

import com.springboot.final_back.dto.ReportResDto;
import com.springboot.final_back.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ReportController {
    private final ReportService reportService;

    // 신고 조회
    @GetMapping
    public ResponseEntity<Page<ReportResDto>> getReports(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) String reportType) {
        Page<ReportResDto> reports = reportService.getReports(page - 1, size, reportType);
        return ResponseEntity.ok(reports);
    }
}
