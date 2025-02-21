package com.springboot.final_back.service;


import com.springboot.final_back.repository.TourSpotsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ElasticsearchService {
    private final TourSpotsRepository tourSpotsRepository;


}
