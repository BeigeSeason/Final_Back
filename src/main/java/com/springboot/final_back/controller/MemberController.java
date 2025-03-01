package com.springboot.final_back.controller;

import com.springboot.final_back.dto.MemberReqDto;
import com.springboot.final_back.dto.MemberResDto;
import com.springboot.final_back.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 회원 아이디 중복 확인
    @PostMapping("/idExists/{userId}")
    public boolean memberIdDulicate(@PathVariable String userId) {
        return memberService.checkIdDuplicate(userId);
    }

    // 회원 이메일 중복 확인
    @PostMapping("/emailExists/{email}")
    public boolean memberEmailDulicate(@PathVariable String email) {
        return memberService.checkEmailDuplicate(email);
    }

    // 회원 닉네임 중복 확인
    @PostMapping("/nicknameExists/{nickname}")
    public boolean memberNicknameDulicate(@PathVariable String nickname) {
        return memberService.checkNicknameDuplicate(nickname);
    }

    // 회원 아이디 찾기
    @PostMapping("/find-id")
    public ResponseEntity<String> findMemberId(@RequestBody MemberReqDto memberReqDto) {
        String userId = memberService.findMemberId(memberReqDto.getName(), memberReqDto.getEmail());

        if (userId != null) {
            return ResponseEntity.ok(userId);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    // 회원 조회
    @GetMapping("/get-info/{userId}")
    public MemberResDto getMemberDetail(@PathVariable String userId) {
        return memberService.getMemberDetail(userId);
    }

    // 회원 비밀번호 확인
    @PutMapping("/check-pw")
    public Boolean cheackMemberPassword(@RequestBody MemberReqDto memberReqDto) {
        return memberService.cheackMemberPassword(memberReqDto);
    }

    // 회원 비밀번호 찾기
    @PostMapping("/find-pw")
    public String findMemberPassword(@RequestBody MemberReqDto memberReqDto) {
        boolean isSuccess = memberService.findMemberPassword(memberReqDto.getUserId(), memberReqDto.getEmail());
        String password = memberService.generateTempPassword();
        return isSuccess ? password : null;
    }

    // 회원 비밀번호 변경
    @PutMapping("change-pw")
    public ResponseEntity<Boolean> memberUpdatePassword(@RequestBody MemberReqDto memberReqDto) {
        boolean isSuccess = memberService.updateMemberPassword(memberReqDto);
        return ResponseEntity.ok(isSuccess);
    }
}
