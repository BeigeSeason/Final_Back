package com.springboot.final_back.controller;

import com.springboot.final_back.dto.DiaryReqDto;
import com.springboot.final_back.dto.DiaryResDto;
import com.springboot.final_back.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/diary")
@CrossOrigin(origins = "http://localhost:3000")
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping("/post-diary")
    public ResponseEntity<Boolean> postDiary(@RequestBody DiaryReqDto reqDto) {
        return new ResponseEntity<>(diaryService.createDiary(reqDto), HttpStatus.OK);
    }

    @GetMapping("/diary-detail/{diaryId}")
    public ResponseEntity<DiaryResDto> getDiaryDetail(@PathVariable String diaryId) {
        return ResponseEntity.ok(diaryService.getDiaryDetail(diaryId));
    }
}
