package com.springboot.final_back.service;

import com.springboot.final_back.dto.MemberResDto;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;

    // 회원 전체 조회
    public Page<MemberResDto> getMemberAllList(int page, int size, String searchType, String searchValue, Boolean type, String sort) {
        Sort sortBy = Sort.by("id").descending();

        if (sort != null) {
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
            }
        }

        if (type != null) {
            // banned 기준으로 오름차순 혹은 내림차순으로 정렬 (banned가 true인 회원을 먼저 혹은 나중에 표시)
            if (type) {
                sortBy = sortBy.and(Sort.by("banned").descending());  // banned가 true인 회원을 우선적으로 정렬
            } else {
                sortBy = sortBy.and(Sort.by("banned").ascending());   // banned가 false인 회원을 우선적으로 정렬
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("regDate").descending());  // regDate 기준 내림차순 정렬

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
}
