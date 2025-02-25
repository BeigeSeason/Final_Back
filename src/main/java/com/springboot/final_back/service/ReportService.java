package com.springboot.final_back.service;


import com.springboot.final_back.constant.Type;
import com.springboot.final_back.dto.ReportReqDto;
import com.springboot.final_back.dto.ReportResDto;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.entity.mysql.Report;
import com.springboot.final_back.entity.mysql.Review;
import com.springboot.final_back.repository.MemberRepository;
import com.springboot.final_back.repository.ReportRepository;
import com.springboot.final_back.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;

    // 신고 생성
    public boolean insertReport(ReportReqDto reportReqDto) {
        try {
            Member reporter = memberRepository.findByUserId(String.valueOf(reportReqDto.getReporter()))
                    .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));

            Member reported = memberRepository.findByUserId(String.valueOf(reportReqDto.getReported()))
                    .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));

            Report report = reportReqDto.toEntity(reportReqDto.getReason(), reporter, reported);

            reportRepository.save(report);

            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    // 신고 조회
    public Page<ReportResDto> getReports(int currentPage, int size, String reportType) {
        try {
            if (reportType == null) {
                reportType = "";
            }

            Pageable pageable = PageRequest.of(currentPage, size);
            Page<Report> page;

            if (!reportType.isEmpty()) {
                page = reportRepository.findAllByReportType(pageable, Type.valueOf(reportType.toUpperCase()));
            } else {
                page = reportRepository.findAll(pageable);
            }

            // reportType이 REVIEW일 경우, reviewId를 기반으로 Review content 가져오기
            if (reportType.equalsIgnoreCase("REVIEW")) {
                return page.map(report -> {
                    // ReportResDto로 변환
                    ReportResDto dto = ReportResDto.of(report);

                    // Review 엔티티 조회 (여기서 reviewId를 사용)
                    Review review = reviewRepository.findById(Long.valueOf(report.getReportEntity())).orElse(null);
                    if (review != null) {
                        // Review가 존재하면 content를 dto에 추가
                        dto.setReviewContent(review.getContent());
                    }

                    return dto;
                });
            }

            return page.map(ReportResDto::of);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Page.empty();  // null 대신 Page.empty()로 안정성 확보
        }
    }
}
