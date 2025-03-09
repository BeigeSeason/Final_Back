package com.springboot.final_back.controller;

import com.springboot.final_back.dto.BanReqDto;
import com.springboot.final_back.dto.Auth.MemberResDto;
import com.springboot.final_back.dto.report.ReportManageReq;
import com.springboot.final_back.dto.report.ReportResDto;
import com.springboot.final_back.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {
    private final AdminService adminService;
    private final ReportService reportService;
    private final DiaryService diaryService;
    private final ReviewService reviewService;

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
                                                          @RequestParam(required = false) String reportType,
                                                          @RequestParam(required = false) String type,
                                                          @RequestParam(required = false) String sort) {
        Page<ReportResDto> reports = reportService.getReports(page, size, reportType, type, sort);

        // 응답에 필요한 데이터만 포함
        Map<String, Object> response = new HashMap<>();
        response.put("reports", reports.getContent());  // 요청된 페이지의 데이터
        response.put("totalElements", reports.getTotalElements());  // 전체 데이터 수
        return ResponseEntity.ok(response);
    }

    // 신고 관리
    @PostMapping("/report-manage")
    @Transactional
    public ResponseEntity<Boolean> reportManage(@RequestBody ReportManageReq request) {
            boolean isSuccess = adminService.reportProcess(request);
            return isSuccess ? ResponseEntity.ok(true)
                             : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
    }

    // 유저 정지
    @PostMapping("/member-ban")
    public ResponseEntity<Boolean> banManage(@RequestBody BanReqDto request) {
        boolean isSuccess = adminService.memberBan(request.getId(), request.getDay(), request.getReason());
        return ResponseEntity.ok(isSuccess);
    }

    // 월별 가입자수
    @GetMapping("/chart/{type}/{year}")
    public ResponseEntity<List<Integer>> getMonthlySignups(@PathVariable String type, @PathVariable int year) throws IllegalAccessException {
        List<Integer> signupCounts = adminService.getMonthlyStats(type, year);
        return ResponseEntity.ok(signupCounts);
    }
}
