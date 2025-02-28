package com.springboot.final_back.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.final_back.constant.TourConstants;
import com.springboot.final_back.dto.TourSpotDetailDto;
import com.springboot.final_back.dto.search.TourSpotStats;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TourSpotService {
    private final TourSpotsRepository tourSpotsRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisTemplate<String, TourSpotStats> redisTemplate;
    private final ReviewRepository reviewRepository;
    private final BookmarkRepository bookmarkRepository;
    @Value("${tour.api.service-key1}")
    private String serviceKey1;

    @Value("${tour.api.service-key2}")
    private String serviceKey2;

    @Value("${tour.api.service-key3}")
    private String serviceKey3;

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService1";
    private static final String INDEX_NAME = "tour_spots";

    public TourSpotDetailDto getTourSpotDetail(String tourSpotId) {
        try {
            Optional<TourSpots> tourSpotOpt = tourSpotsRepository.findByContentId(tourSpotId);

            if (tourSpotOpt.isPresent()) {
                TourSpots tourSpot = tourSpotOpt.get();
                TourSpots.Detail detail = tourSpot.getDetail();


                if (detail != null) {
                    // 썸네일을 이미지 목록에 포함시켜 전송
                    if (!tourSpot.getFirstImage().isEmpty()) detail.getImages().add(0, tourSpot.getFirstImage());
                    log.info(detail.getImages().toString());
                    return convertToDto(tourSpot, detail);
                }

                TourSpotDetailDto detailDto = fetchDetailFromApi(tourSpotId, tourSpot.getContentTypeId());
                saveDetailToElasticsearch(tourSpot.getId(), detailDto);
                // 썸네일을 이미지 목록에 포함시켜 전송
                if (!tourSpot.getFirstImage().isEmpty()) detailDto.getImages().add(0, tourSpot.getFirstImage());
                log.info(detailDto.getImages().toString());
                detailDto.setAddr1(tourSpot.getAddr1());
                detailDto.setMapX(tourSpot.getMapX());
                detailDto.setMapY(tourSpot.getMapY());
                return detailDto;
            } else {
                throw new RuntimeException("해당 관광지 데이터가 없습니다: " + tourSpotId);
            }
        } catch (Exception e) {
            log.error("상세 정보 조회 중 오류: {}", e.getMessage());
            throw new RuntimeException("상세 정보를 가져오지 못했습니다.");
        }
    }

    // 여행지 상세정보 존재하지 않을 시 Api 요청
    private TourSpotDetailDto fetchDetailFromApi(String contentId, String contentTypeId) {
        try {
            // 1. detailCommon1 호출
            String commonUrl = BASE_URL + "/detailCommon1?MobileOS=ETC&MobileApp=Final_test&_type=json" +
                    "&contentId=" + contentId + "&defaultYN=Y&overviewYN=Y&serviceKey=" + serviceKey1;
            Map<String, Object> commonItem = fetchApiData(commonUrl, "common", true);

            // 2. detailImage1 호출
            String imageUrl = BASE_URL + "/detailImage1?MobileOS=ETC&MobileApp=Final_test&_type=json" +
                    "&contentId=" + contentId + "&subImageYN=Y&serviceKey=" + serviceKey2;
            List<Map<String, Object>> imageItems = fetchApiData(imageUrl, "image", false);

            // 3. detailIntro1 호출
            String introUrl = BASE_URL + "/detailIntro1?MobileOS=ETC&MobileApp=Final_test&_type=json" +
                    "&contentId=" + contentId + "&contentTypeId=" + contentTypeId + "&serviceKey=" + serviceKey3;
            Map<String, Object> introItem = fetchApiData(introUrl, "intro", true);

            return TourSpotDetailDto.builder()
                    .contentId(contentId)
                    .title((String) commonItem.getOrDefault("title", ""))
                    .images(imageItems.stream()
                            .map(item -> (String) item.get("originimgurl"))
                            .collect(Collectors.toList()))
                    .overview((String) commonItem.getOrDefault("overview", ""))
                    .homepage((String) commonItem.getOrDefault("homepage", ""))
                    .infoCenter((String) introItem.getOrDefault("infocenter", ""))
                    .useTime((String) introItem.getOrDefault("usetime", ""))
                    .parking((String) introItem.getOrDefault("parking", ""))
                    .build();
        } catch (Exception e) {
            log.error("API 호출 중 오류 - contentId: {}: {}", contentId, e.getMessage());
            throw new RuntimeException("API 데이터를 가져오지 못했습니다.");
        }
    }

    // 공통 API 호출 메서드
    @SuppressWarnings("unchecked") // JSON 응답 구조가 Map<String, Object>로 고정돼 있어 안전함
    private <T> T fetchApiData(String url, String logLabel, boolean isSingleItem) {
        try {
            log.info("호출 URL ({}): {}", logLabel, url);

            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");

            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBody.append(line);
                }
            }
            log.info("응답 상태 ({}): {}", logLabel, conn.getResponseCode());

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = mapper.readValue(responseBody.toString(), new TypeReference<Map<String, Object>>() {
            });

            return isSingleItem ? (T) extractItem(response) : (T) extractItems(response);
        } catch (Exception e) {
            log.error("API 호출 중 오류 ({}): {}", logLabel, e.getMessage());
            throw new RuntimeException("API 호출 실패: " + logLabel);
        }
    }

    // 상세정보 엘라스틱 서치에 저장하기
    private void saveDetailToElasticsearch(String contentId, TourSpotDetailDto detailDto) {
        UpdateQuery updateQuery = UpdateQuery.builder(contentId)
                .withDocument(Document.from(Map.of(
                        "detail", Map.of(
                                "images", detailDto.getImages(),
                                "overview", detailDto.getOverview(),
                                "info_center", detailDto.getInfoCenter(),
                                "homepage", detailDto.getHomepage(),
                                "use_time", detailDto.getUseTime(),
                                "parking", detailDto.getParking()
                        )
                )))
                .build();
        elasticsearchOperations.update(updateQuery, IndexCoordinates.of(INDEX_NAME));
        log.info("관광지 {} 상세 정보 저장 완료", contentId);
    }

    @SuppressWarnings("unchecked") // JSON 응답 구조가 Map<String, Object>로 고정돼 있어 안전함
    private Map<String, Object> extractItem(Map<String, Object> response) {
        try {
            Map<String, Object> body = (Map<String, Object>) response.get("response");
            Map<String, Object> items = (Map<String, Object>) ((Map<String, Object>) body.get("body")).get("items");
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");
            return itemList.get(0);
        } catch (Exception e) {
            log.error("아이템 추출 실패: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked") // JSON 응답 구조가 Map<String, Object>로 고정돼 있어 안전함
    private List<Map<String, Object>> extractItems(Map<String, Object> response) {
        try {
            Map<String, Object> body = (Map<String, Object>) response.get("response");
            Map<String, Object> items = (Map<String, Object>) ((Map<String, Object>) body.get("body")).get("items");
            return (List<Map<String, Object>>) items.get("item");
        } catch (Exception e) {
            log.error("이미지 목록 추출 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // 비동기로 단일 관광지의 통계를 MySql 에서 가져와 Redis에 캐싱
    @Async
    public void cacheTourSpotStats(String tourSpotId) {
        TourSpotStats stats = fetchStatsFromMySQL(tourSpotId);
        String cacheKey = TourConstants.TOUR_SPOT_STATS_PREFIX + tourSpotId;
        redisTemplate.opsForValue().set(cacheKey, stats, 1, TimeUnit.HOURS);
    }

    // 단일 관광지의 리뷰/북마크 통계를 MySQL에서 조회해 TourSpotStats로 반환.
    private TourSpotStats fetchStatsFromMySQL(String tourSpotId) {
        Integer reviewCount = reviewRepository.countByReviewedId(tourSpotId);
        Double avgRating = reviewRepository.avgRatingByReviewedId(tourSpotId);
        Integer bookmarkCount = bookmarkRepository.countByBookmarkedId(tourSpotId);
        return new TourSpotStats(tourSpotId, reviewCount != null ? reviewCount : 0,
                avgRating != null ? avgRating : 0.0, bookmarkCount != null ? bookmarkCount : 0);
    }

    private TourSpotDetailDto convertToDto(TourSpots tourSpot, TourSpots.Detail detail) {
        return TourSpotDetailDto.builder()
                .contentId(tourSpot.getContentId())
                .title(tourSpot.getTitle())
                .addr1(tourSpot.getAddr1())
                .images(detail.getImages())
                .overview(detail.getOverview())
                .homepage(detail.getHomepage())
                .infoCenter(detail.getInfoCenter())
                .useTime(detail.getUseTime())
                .parking(detail.getParking())
                .mapX(tourSpot.getMapX())
                .mapY(tourSpot.getMapY())
                .build();
    }
}