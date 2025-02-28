package com.springboot.final_back.service;

import com.springboot.final_back.dto.MemberReqDto;
import com.springboot.final_back.dto.MemberResDto;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // 회원 상세 조회
    public MemberResDto getMemberDetail(String userId) {
        Member member = memberRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("해당 회원이 존재하지 않습니다."));
        return member.convertEntityToDto();
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

    // 회원 비밀번호 수정
    public boolean updateMemberPassword(MemberReqDto memberReqDto) {
        try {
            Member member = memberRepository.findByUserId(memberReqDto.getUserId()).orElseThrow(() -> new RuntimeException("해당 회원이 존재하지 않습니다."));
            member.setPassword(memberReqDto.getPassword());

            memberRepository.save(member);
            return true;
        } catch (Exception e) {
            log.error("비밀번호 변경: {}", e.getMessage());
            return false;
        }
    }

    // 회원 아이디 중복 확인
    public boolean checkIdDuplicate(String userId) {
        return memberRepository.existsByUserId(userId);
    }

    // 회원 이메일 중복 확인
    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 회원 닉네임 중복 확인
    public boolean checkNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 회원 아이디 찾기
    public String findMemberId(String name, String email) {
        Member member = memberRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new RuntimeException("해당 회원이 존재하지 않습니다."));
        return member != null ? member.getUserId() : null;
    }

    // 회원 비밀번호 찾기
    public boolean findMemberPassword(String userId, String email) {
        Member member = memberRepository.findByUserIdAndEmail(userId, email)
                .orElseThrow(() -> new RuntimeException("해당 회원이 존재하지 않습니다."));
        return member != null;
    }

    // 임시 비밀번호 생성 함수
    public String generateTempPassword() {
        SecureRandom random = new SecureRandom();
        // 각 조건을 만족하는 문자 그룹
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()";
        // 결과 비밀번호를 구성할 리스트
        List<Character> password = new ArrayList<>();
        // 각 그룹에서 최소 1개의 문자를 추가
        password.add(upperCase.charAt(random.nextInt(upperCase.length())));
        password.add(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.add(digits.charAt(random.nextInt(digits.length())));
        password.add(specialChars.charAt(random.nextInt(specialChars.length())));
        // 나머지 문자 채우기
        String allChars = upperCase + lowerCase + digits + specialChars;
        for (int i = 4; i < 8; i++) { // 총 8자리로 생성
            password.add(allChars.charAt(random.nextInt(allChars.length())));
        }
        // 비밀번호를 랜덤하게 섞기
        Collections.shuffle(password, random);
        // 리스트를 문자열로 변환
        StringBuilder result = new StringBuilder();
        for (char c : password) {
            result.append(c);
        }
        return result.toString();
    }


}
