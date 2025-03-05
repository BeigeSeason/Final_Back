package com.springboot.final_back.service;

import com.springboot.final_back.constant.MemberRole;
import com.springboot.final_back.constant.State;
import com.springboot.final_back.dto.MemberResDto;
import com.springboot.final_back.entity.mysql.Ban;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.entity.mysql.Report;
import com.springboot.final_back.repository.BanRepository;
import com.springboot.final_back.repository.MemberRepository;
import com.springboot.final_back.repository.ReportRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;
    private final BanRepository banRepository;

    // 회원 전체 조회
    public Page<MemberResDto> getMemberAllList(int page, int size, String searchType, String searchValue, Boolean type, String sort) {
        Sort sortBy = Sort.by("id").descending();

        // type이 있을 경우 정렬
        if (type != null) {
            if (type) {
                sortBy = Sort.by("banned").descending();
            } else {
                sortBy = Sort.by("banned").ascending();
            }
            if (sort != null) {
                switch (sort) {
                    case "idAsc":
                        sortBy = sortBy.and(Sort.by("id").ascending());
                        break;
                    case "idDesc":
                        sortBy = sortBy.and(Sort.by("id").descending());
                        break;
                    case "userIdAsc":
                        sortBy = sortBy.and(Sort.by("userId").ascending());
                        break;
                    case "userIdDesc":
                        sortBy = sortBy.and(Sort.by("userId").descending());
                        break;
                    default:
                        break;
                }
            }
        }

        // type이 없고 sort만 있을 때 정렬
        if (type == null && sort != null) {
            switch (sort) {
                case "idAsc":
                    sortBy = Sort.by("id").ascending();
                    break;
                case "idDesc":
                    sortBy = Sort.by("id").descending();
                    break;
                case "userIdAsc":
                    sortBy = Sort.by("userId").ascending();
                    break;
                case "userIdDesc":
                    sortBy = Sort.by("userId").descending();
                    break;
                default:
                    break;
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortBy);

        Page<Member> memberPage;
        if (searchType != null && searchValue != null && !searchValue.isEmpty()) {
            switch (searchType) {
                case "ID":
                    memberPage = memberRepository.findByUserIdContaining(searchValue, pageable);
                    break;
                case "NAME":
                    memberPage = memberRepository.findByNameContaining(searchValue, pageable);
                    break;
                case "NICKNAME":
                    memberPage = memberRepository.findByNicknameContaining(searchValue, pageable);
                    break;
                case "EMAIL":
                    memberPage = memberRepository.findByEmailContaining(searchValue, pageable);
                    break;
                default:
                    memberPage = memberRepository.findAll(pageable);  // 기본값: 전체 조회
                    break;
            }
        } else {
            memberPage = memberRepository.findAll(pageable);  // 검색 조건이 없으면 모든 멤버 조회
        }

        return memberPage.map(Member::convertEntityToDto);
    }

    // 신고 관리 (승인, 거절)
    public boolean reportProcess(Long reportId, boolean state) {
        try {
            // 신고 처리
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("해당 신고가 존재하지 않습니다."));
            if (state) {
                report.setState(State.ACCEPT);
            } else {
                report.setState(State.REJECT);
            }
            reportRepository.save(report);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    // 유저 정지 관리
    @Transactional
    public boolean memberBan(Long id, int day, String reason) {
        try {
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
            Ban alreadyBanned = banRepository.findFirstByMemberOrderByIdDesc(member);

            LocalDateTime endDate;

            // 유저 상태 변경, 종료 날짜 설정
            if (alreadyBanned == null) {
                endDate = LocalDateTime.now().plusDays(day).with(LocalTime.of(0, 0));
                member.setBanned(true);
                member.setRole(MemberRole.BANNED);
                memberRepository.save(member);
            } else {
                endDate = alreadyBanned.getEndDate().plusDays(day).with(LocalTime.of(0, 0));
            }

            // ban 추가
            Ban ban = Ban.builder()
                    .member(member)
                    .startDate(LocalDateTime.now())
                    .endDate(endDate)
                    .reason(reason)
                    .build();
            banRepository.save(ban);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    // 월별 가입자수
    public List<Integer> getMonthlySignup(int year) {
        List<Integer> signups = new ArrayList<>(Collections.nCopies(12, 0));

        List<Object[]> rawData = memberRepository.getMonthlySignups(year);
        for (Object[] row : rawData) {
            int month = (int) row[0];
            int count = ((Number) row[1]).intValue();
            signups.set(month - 1, count);
        }

        return signups;
    }
}
