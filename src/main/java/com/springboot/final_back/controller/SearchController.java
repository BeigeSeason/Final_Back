package com.springboot.final_back.controller;

import com.springboot.final_back.dto.TourSpotDetailDto;
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

    @GetMapping("/tour-list")
    public ResponseEntity<Page<TourSpotListDto>> findTourSpotList(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam(required = false) int sort,
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
}
