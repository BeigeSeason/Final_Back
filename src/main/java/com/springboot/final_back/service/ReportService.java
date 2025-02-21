package com.springboot.final_back.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
//    private final ReportRepository reportRepository;
//    private final MemberRepository memberRepository;
//
//    // 신고 생성
//    public boolean insertReport(ReportReqDto reportReqDto) {
//        try {
//            Member reporter = memberRepository.findByUserId(String.valueOf(reportReqDto.getReporter()))
//                    .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));
//
//            Member reported = memberRepository.findByUserId(String.valueOf(reportReqDto.getReported()))
//                    .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));
//
//            Report report = reportReqDto.toEntity(reportReqDto.getContent(), reporter, reported);
//
//            reportRepository.save(report);
//
//            return true;
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return false;
//        }
//    }
//
//    // 신고 조회
//    public Page<ReportResDto> getReports(int currentPage, int size, String reportType) {
//        try {
//            if (reportType == null) {
//                reportType = "";
//            }
//
//            Pageable pageable = PageRequest.of(currentPage, size);
//            Page<Report> page;
//
//            if (!reportType.isEmpty()) {
//                page = reportRepository.findAllByReportType(pageable, Report.ReportType.valueOf(reportType.toUpperCase()));
//            } else {
//                page = reportRepository.findAll(pageable);
//            }
//
//            return page.map(ReportResDto::of);
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            return Page.empty();  // null 대신 Page.empty()로 안정성 확보
//        }
//    }
}
