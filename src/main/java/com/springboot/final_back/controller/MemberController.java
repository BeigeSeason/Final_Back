package com.springboot.final_back.controller;

import com.springboot.final_back.dto.MemberResDto;
import com.springboot.final_back.entity.Member;
import com.springboot.final_back.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/list")
    public ResponseEntity<Page<MemberResDto>> getAllMembers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Member.SearchType searchType,
            @RequestParam(required = false) String searchValue
            ) {
        Page<MemberResDto> members = memberService.getMemberAllList(page - 1, size, searchType, searchValue);
        return ResponseEntity.ok(members);
    }
}
