package com.springboot.final_back.controller;

import com.springboot.final_back.dto.search.TourSpotListDto;
import com.springboot.final_back.service.SearchService;
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

    @GetMapping("/tour-list")
    public ResponseEntity<Page<TourSpotListDto>> findTourSpotList(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam String keyword) {
        log.info(keyword);
        return new ResponseEntity<>(searchService.searchTourSpots(page, size, keyword), HttpStatus.OK);
    }
}
