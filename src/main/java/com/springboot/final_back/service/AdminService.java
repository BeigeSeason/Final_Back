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
}
