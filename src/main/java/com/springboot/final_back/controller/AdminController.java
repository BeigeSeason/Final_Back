package com.springboot.final_back.controller;

import com.springboot.final_back.dto.BanReqDto;
import com.springboot.final_back.dto.MemberResDto;
import com.springboot.final_back.dto.ReportResDto;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
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
    public ResponseEntity<Boolean> reportManage(@RequestBody Map<String, Object> payload) {
        try {
            Long reportId = Long.valueOf(payload.get("reportId").toString());
            boolean state = Boolean.parseBoolean(payload.get("state").toString());
            Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
            Integer day = payload.get("day") != null ? Integer.valueOf(payload.get("day").toString()) : null;
            String reason = (String) payload.get("reason");
            String diaryId = payload.get("diaryId") != null ? payload.get("diaryId").toString() : null;
            Long reviewId = payload.get("reviewId") != null ? Long.valueOf(payload.get("reviewId").toString()) : null;

            // 신고 처리
            boolean isSuccess = adminService.reportProcess(reportId, state);
            // 유저 정지
            if (userId != null) {
                adminService.memberBan(userId, day, reason);
            }
            // 일기 삭제
            if (diaryId != null) {
                diaryService.deleteDiary(diaryId);
            }
            // 댓글 삭제
            if (reviewId != null) {
                reviewService.deleteReview(reviewId);
            }
            return ResponseEntity.ok(isSuccess);
        } catch (Exception e) {
            e.printStackTrace();
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    // 유저 정지
    @PostMapping("/member-ban")
    public ResponseEntity<Boolean> banManage(@RequestBody BanReqDto request) {
        boolean isSuccess = adminService.memberBan(request.getId(), request.getDay(), request.getReason());
        return ResponseEntity.ok(isSuccess);
    }
}
