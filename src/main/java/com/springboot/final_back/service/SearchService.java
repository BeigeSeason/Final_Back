package com.springboot.final_back.service;


import com.springboot.final_back.dto.TourSpotListDto;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.repository.DiaryRepository;
import com.springboot.final_back.repository.TourSpotsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class SearchService {
    private final TourSpotsRepository tourSpotsRepository;
    private final DiaryRepository diaryRepository;

    public Page<Diary> searchByTitle(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return diaryRepository.findByTitle(title, pageable);
    }

    public Page<TourSpotListDto> searchTourSpots(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TourSpots> pages = tourSpotsRepository.findByTitleOrAddr1(keyword, keyword, pageable);
        log.info(pages.toString());
        for(TourSpots tourSpots : pages) {
            log.warn(tourSpots.getFirstImage());
            log.warn(tourSpots.getFirstImage2());
        }

        return pages.map(TourSpots::convertToListDto);
    }

}
