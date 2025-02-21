package com.springboot.final_back.service;

import com.springboot.final_back.dto.MemberReqDto;
import com.springboot.final_back.dto.MemberResDto;
import com.springboot.final_back.entity.Member;
import com.springboot.final_back.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // 회원 전체 조회
    public Page<MemberResDto> getMemberAllList(int page, int size, Member.SearchType searchType, String searchValue) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());  // createdAt 기준 내림차순 정렬

        Page<Member> memberPage;

        if (searchType != null && searchValue != null && !searchValue.isEmpty()) {
            switch (searchType) {
                case NAME:
                    memberPage = memberRepository.findByNameContaining(searchValue, pageable);
                    break;
                case NICKNAME:
                    memberPage = memberRepository.findByNicknameContaining(searchValue, pageable);
                    break;
                case EMAIL:
                    memberPage = memberRepository.findByEmailContaining(searchValue, pageable);
                    break;
                default:
                    memberPage = memberRepository.findAll(pageable);  // 기본값
                    break;
            }
        } else {
            memberPage = memberRepository.findAll(pageable);  // 검색 조건이 없으면 모든 멤버 조회
        }

        return memberPage.map(this::convertEntityToDto);
    }

    // 회원 상세 조회
    public MemberResDto getMemberDetail(String userId) {
        Member member = memberRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("해당 회원이 존재하지 않습니다."));
        return convertEntityToDto(member);
    }

    // 회원 수정
    public boolean updateMember(MemberReqDto memberReqDto) {
        try {
            Member member = memberRepository.findByUserId(memberReqDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("해당 회원이 존재하지 않습니다."));
            member.setEmail(memberReqDto.getEmail());
            member.setName(memberReqDto.getName());
            member.setNickname(memberReqDto.getNickname());
            member.setImgPath(memberReqDto.getImgPath());
            memberRepository.save(member);
            return true;
        } catch (Exception e) {
            log.error("회원정보 수정 : {}", e.getMessage());
            return false;
        }
    }

    // Member Entity => MemberResDto 변환
    private MemberResDto convertEntityToDto(Member member) {
        MemberResDto memberResDto = new MemberResDto();
        memberResDto.setId(member.getId());
        memberResDto.setUserId(member.getUserId());
        memberResDto.setEmail(member.getEmail());
        memberResDto.setName(member.getName());
        memberResDto.setNickname(member.getNickname());
        memberResDto.setImgPath(member.getImgPath());
        return memberResDto;
    }
}
