package com.springboot.final_back.controller;

import com.springboot.final_back.dto.MemberResDto;
import com.springboot.final_back.dto.ReportResDto;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.service.AdminService;
import com.springboot.final_back.service.MemberService;
import com.springboot.final_back.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final ReportService reportService;

    // 멤버 조회
    @GetMapping("/member-list")
    public ResponseEntity<Page<MemberResDto>> getAllMembers(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(required = false) String searchType,
                                                            @RequestParam(required = false) String searchValue) {
        Page<MemberResDto> members = adminService.getMemberAllList(page, size, searchType, searchValue);
        return ResponseEntity.ok(members);
    }

    // 신고 조회
    @GetMapping("/report-list")
    public ResponseEntity<Page<ReportResDto>> getReports(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) String reportType) {
        Page<ReportResDto> reports = reportService.getReports(page, size, reportType);
        return ResponseEntity.ok(reports);
    }
}
