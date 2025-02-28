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
}
