package com.springboot.final_back.controller;

import com.springboot.final_back.dto.TourSpotDetailDto;
import com.springboot.final_back.dto.search.DiarySearchListDto;
import com.springboot.final_back.dto.search.TourSpotListDto;
import com.springboot.final_back.service.SearchService;
import com.springboot.final_back.service.TourSpotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SearchController {
    private final SearchService searchService;
    private final TourSpotService tourSpotService;

    @GetMapping("/diary-list")
    public ResponseEntity<Page<DiarySearchListDto>> getDiaryList(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @RequestParam(required = false) String keyword) {
        return new ResponseEntity<>(searchService.searchByTitle(keyword, page, size), HttpStatus.OK);
    }

    @GetMapping("/tour-list")
    public ResponseEntity<Page<TourSpotListDto>> findTourSpotList(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam(required = false) String sort,
                                                                  @RequestParam(required = false) String keyword,
                                                                  @RequestParam(required = false) String areaCode,
                                                                  @RequestParam(required = false) String sigunguCode,
                                                                  @RequestParam(required = false) String contentTypeId) {
        return new ResponseEntity<>(searchService.searchTourSpots(page, size, sort, keyword, areaCode, sigunguCode, contentTypeId), HttpStatus.OK);
    }

    @GetMapping("/spot-detail")
    public ResponseEntity<TourSpotDetailDto> getTourSpotDetail(@RequestParam String tourSpotId) {
        return new ResponseEntity<>(tourSpotService.getTourSpotDetail(tourSpotId), HttpStatus.OK);
    }

    // 나의 다이어리 목록 조회(비공개 포함)
    @GetMapping("/my-diary-list")
    public ResponseEntity<Page<DiarySearchListDto>> getMyDiaryList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam String userId) {
        return ResponseEntity.ok(searchService.getMyDiaryList(userId, page, size));
    }

    // 다른 유저 다이어리 목록 조회(비공개 미포함)
    @GetMapping("/otheruser-diary-list")
    public ResponseEntity<Page<DiarySearchListDto>> getOtherUserDiaryList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam String userId) {
        return ResponseEntity.ok(searchService.getOtherUserDiaryList(userId, page, size));
    }

}
