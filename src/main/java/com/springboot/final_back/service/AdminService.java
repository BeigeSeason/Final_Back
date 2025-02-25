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

        if (type != null) {
            if (type) {
                sortBy = Sort.by("banned").descending();  // banned가 true인 회원을 우선적으로 정렬
            } else {
                sortBy = Sort.by("banned").ascending();   // banned가 false인 회원을 우선적으로 정렬
            }
        }

        if (sort != null) {
            sortBy = switch (sort) {
                case "idAsc" -> sortBy.and(Sort.by("id").ascending());
                case "idDesc" -> sortBy.and(Sort.by("id").descending());
                case "userIdAsc" -> sortBy.and(Sort.by("userId").ascending());
                case "userIdDesc" -> sortBy.and(Sort.by("userId").descending());
                default -> sortBy;
            };
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
}
