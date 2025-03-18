package com.springboot.final_back.controller;

import com.springboot.final_back.dto.tourspot.TourSpotDetailDto;
import com.springboot.final_back.dto.diary.DiarySearchListDto;
import com.springboot.final_back.dto.tourspot.TourSpotListDto;
import com.springboot.final_back.service.SearchService;
import com.springboot.final_back.service.TourSpotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
                                                                 @RequestParam(required = false) String keyword,
                                                                 @RequestParam(required = false) String sort,
                                                                 @RequestParam(defaultValue = "0") int minPrice,
                                                                 @RequestParam(defaultValue = "0") int maxPrice,
                                                                 @RequestParam(required = false) String areaCode,
                                                                 @RequestParam(required = false) String sigunguCode) {
        return new ResponseEntity<>(searchService.diarySearch(page, size, keyword, sort, minPrice, maxPrice, areaCode, sigunguCode), HttpStatus.OK);
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

    // 추천 사용 시 보여줄 썸네일과 여행지
    @PostMapping("/recommend-spot")
    public ResponseEntity<Map<String, List<TourSpotListDto>>> getRecommendTourSpot(@RequestBody List<String> keyword) {
        return new ResponseEntity<>(searchService.get10SpotsRecommend(keyword), HttpStatus.OK);
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

    // 내가 북마크한 다이어리 목록 조회
    @GetMapping("/my-bookmarked-diaries")
    public ResponseEntity<Page<DiarySearchListDto>> getBookmarkedDiaries(@RequestParam String userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.getBookmarkedDiaries(userId, page, size));
    }

    // 내가 북마크한 관광지 목록 조회
    @GetMapping("/my-bookmarked-tourspots")
    public ResponseEntity<Page<TourSpotListDto>> getBookmarkedTourSpots(@RequestParam String userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.getBookmarkedTourSpots(userId, page, size));
    }

}
