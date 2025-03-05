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
    public ResponseEntity<Page<TourSpotListDto>> findTourSpotList(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "size", defaultValue = "20") int size,
                                                                  @RequestParam(value = "sort", required = false) String sort,
                                                                  @RequestParam(value = "keyword", required = false) String keyword,
                                                                  @RequestParam(value = "areaCode", required = false) String areaCode,
                                                                  @RequestParam(value = "sigunguCode", required = false) String sigunguCode,
                                                                  @RequestParam(value = "contentTypeId", required = false) String contentTypeId) throws Exception {
        return new ResponseEntity<>(searchService.searchTourSpots(page, size, sort, keyword, areaCode, sigunguCode, contentTypeId), HttpStatus.OK);
    }

    @GetMapping("/spot-detail")
    public ResponseEntity<TourSpotDetailDto> getTourSpotDetail(@RequestParam String tourSpotId) {
        return new ResponseEntity<>(tourSpotService.getTourSpotDetail(tourSpotId), HttpStatus.OK);
    }
}
