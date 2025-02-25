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

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final ReportService reportService;

    // 멤버 조회
    @GetMapping("/member-list")
    public ResponseEntity<Map<String, Object>> getAllMembers(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(required = false) String searchType,
                                                            @RequestParam(required = false) String searchValue,
                                                             @RequestParam(required = false) Boolean type,
                                                             @RequestParam(required = false) String sort) {
        Page<MemberResDto> members = adminService.getMemberAllList(page, size, searchType, searchValue, type, sort);

        Map<String, Object> response = new HashMap<>();
        response.put("members", members.getContent());
        response.put("totalElements", members.getTotalElements());

        return ResponseEntity.ok(response);
    }

    // 신고 조회
    @GetMapping("/report-list")
    public ResponseEntity<Map<String, Object>> getReports(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String reportType) {
        Page<ReportResDto> reports = reportService.getReports(page, size, reportType);

        // 응답에 필요한 데이터만 포함
        Map<String, Object> response = new HashMap<>();
        response.put("reports", reports.getContent());  // 요청된 페이지의 데이터
        response.put("totalElements", reports.getTotalElements());  // 전체 데이터 수

        return ResponseEntity.ok(response);
    }
}
