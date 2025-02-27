package com.springboot.final_back.controller;

import com.springboot.final_back.dto.DiaryReqDto;
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

    // Diary id 마지막번호+1 가져오기
    @GetMapping("/new-diaryid")
    public ResponseEntity<Long> getNewDiaryId() {
        return ResponseEntity.ok(diaryService.getLastDiaryId() + 1);
    }

    @PostMapping("/post-diary")
    public ResponseEntity<Boolean> postDiary(@RequestBody DiaryReqDto reqDto) {
        boolean isSuccess = diaryService.createDiary(reqDto);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }



}
