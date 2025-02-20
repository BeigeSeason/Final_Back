package com.springboot.final_back.service;

import com.springboot.final_back.dto.MemberReqDto;
import com.springboot.final_back.dto.MemberResDto;
import com.springboot.final_back.entity.Member;
import com.springboot.final_back.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // 회원 전체 조회
    public List<MemberResDto> getMemberAllList() {
        List<Member> members = memberRepository.findAll();
        List<MemberResDto> memberResDtoList = new ArrayList<>();
        for (Member member : members) {
            memberResDtoList.add(convertEntityToDto(member));
        }
        return memberResDtoList;
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
