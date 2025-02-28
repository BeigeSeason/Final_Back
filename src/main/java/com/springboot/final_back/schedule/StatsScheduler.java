//package com.springboot.final_back.schedule;
//
//import com.springboot.final_back.constant.TourConstants;
//import com.springboot.final_back.dto.search.TourSpotStats;
//import com.springboot.final_back.entity.elasticsearch.TourSpots;
//import com.springboot.final_back.repository.BookmarkRepository;
//import com.springboot.final_back.repository.ReviewRepository;
//import com.springboot.final_back.repository.TourSpotsRepository;
//import com.springboot.final_back.service.SearchService;
//import com.springboot.final_back.service.TourSpotService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//@Component
//@RequiredArgsConstructor
//@EnableScheduling
//@Slf4j
//public class StatsScheduler {
//    private final TourSpotService tourSpotService;
//    private final TourSpotsRepository tourSpotsRepository;
//    private final ReviewRepository reviewRepository;
//    private final BookmarkRepository bookmarkRepository;
//    private final RedisTemplate<String, TourSpotStats> redisTemplate;
//
//    private Map<String, TourSpotStats> fetchStatsFromMySQL(List<String> contentIds) {
//        List<Object[]> reviewStats = reviewRepository.findStatsByTourSpotIds(contentIds);
//        List<Object[]> bookmarkStats = bookmarkRepository.findBookmarkCountsByTourSpotIds(contentIds);
//        Map<String, TourSpotStats> statsMap = new HashMap<>();
//        reviewStats.forEach(stat -> {
//            String id = (String) stat[0];
//            int reviewCount = ((Number) stat[1]).intValue();
//            double avgRating = stat[2] != null ? ((Number) stat[2]).doubleValue() : 0.0;
//            statsMap.put(id, new TourSpotStats(id, reviewCount, avgRating, 0));
//        });
//        bookmarkStats.forEach(stat -> {
//            String id = (String) stat[0];
//            int bookmarkCount = ((Number) stat[1]).intValue();
//            TourSpotStats stats = statsMap.getOrDefault(id, new TourSpotStats(id, 0, 0.0, 0));
//            stats.setBookmarkCount(bookmarkCount);
//            statsMap.put(id, stats);
//        });
//        return statsMap;
//    }
//
//    @Scheduled(fixedRate = 3600000)
//    public void refreshAllStats() {
//        int page = 0;
//        int size = 1000;
//        Page<TourSpots> tourSpotPage;
//        do {
//            Pageable pageable = PageRequest.of(page, size);
//            tourSpotPage = tourSpotsRepository.findAll(pageable);
//            List<String> contentIds = tourSpotPage.getContent().stream()
//                    .map(TourSpots::getContentId)
//                    .collect(Collectors.toList());
//            Map<String, TourSpotStats> statsMap = fetchStatsFromMySQL(contentIds);
//            Map<String, TourSpotStats> cacheData = new HashMap<>();
//            for (String tourSpotId : contentIds) {
//                TourSpotStats stats = statsMap.get(tourSpotId);
//                cacheData.put(TourConstants.TOUR_SPOT_STATS_PREFIX + tourSpotId, stats);
//            }
//            redisTemplate.opsForValue().multiSet(cacheData);
//            redisTemplate.expire(TourConstants.TOUR_SPOT_STATS_PREFIX + contentIds.get(0), 1, TimeUnit.HOURS); // TTL 대표 적용
//            page++;
//        } while (tourSpotPage.hasNext());
//        log.info("모든 관광지 통계 캐시 갱신 완료");
//    }
//}
//
