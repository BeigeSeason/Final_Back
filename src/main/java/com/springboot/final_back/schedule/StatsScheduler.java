package com.springboot.final_back.schedule;

import com.springboot.final_back.repository.TourSpotsRepository;
import com.springboot.final_back.service.SearchService;
import com.springboot.final_back.service.TourSpotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class StatsScheduler {
    private final TourSpotService tourSpotService;
    private final TourSpotsRepository tourSpotsRepository;

    @Scheduled(fixedRate = 3600000) // 1시간
    public void refreshAllStats() {
        List<String> allIds = tourSpotsRepository.findAllContentIds();
        allIds.forEach(tourSpotService::cacheTourSpotStats);
        log.info("모든 관광지 통계 캐시 갱신 완료");
    }
}
